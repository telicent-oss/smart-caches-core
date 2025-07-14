# Action Tracker

The Action Tracker API provides a means for synchronising action state between cooperating microservices, this allows
the primary service that orchestrates actions to inform secondary services that an action is happening.  Those secondary
services can then take appropriate actions e.g. pausing activity, resetting their state etc.

This API is provided by the [`action-tracker`](#dependency) module.

It consists of three main things:

- `ActionState` - The `enum` of possible states.
- `ActionTransitionListener` - A listener to action transition events.
- [`ActionTracker`](#actiontracker) - The actual state tracker API, and concrete implementations thereof.

## `ActionState`

Action state is tracked via this `enum`, which in combination with its static `canTransition()` method defines a state
machine for the possible states of actions:

```mermaid
flowchart TD

  S[Starting]
  R[Ready]
  P[Processing]

  S -- starts up --> R
  R -- starts action --> P
  P -- finishes action --> R
```

Note that we can always transition to the Ready state from any other state, and from Ready we can always transition into
processing.

## `ActionTransitionListener`

A `ActionTransitionListener` is a listener that allows your microservices to listen to the [action state](#actionstate)
transitions that are happening, and trigger actions within your application if so desired.

For example a [secondary](#secondary) microservice might wish to pause activity while an action is in process.

## `ActionTracker`

The `ActionTracker` API is the main API by which applications use this [module](#dependency), it has a pretty
straightfoward API:

- `ActionState getState()` - Retrieves the current [state](#actionstate)
- `String getAction()` - Retrieves the current action identifier (if any).
- `startupComplete()` - Informs the tracker that startup has been completed.
- `start(String)` - Informs the tracker that an action is starting.
- `finish(String))` - Informs the tracker that an action has finished.
- `close()` - Closes the tracker.

Typically this API will only be called directly by the [primary](#primary) microservice, with [secondary](#secondary)
services having a `ActionTracker` instance available but using [listeners](#actiontransitionlistener) to react to the
automatically synchronised action state transitions.

A `SimpleActionTracker` is a pure in-memory implementation of this the API and the underlying [state
machine](#actionstate).  However microservices will want to use one of the Kafka driven implementations of the API for
production usage.

### Primary

One microservice within an application typically acts the primary which orchestrates backup/restore operations.  This
application should create an instance of the `PrimaryActionTracker` e.g.

```java
// Create a KafkaSink
// Note that as we want to guarantee action transition events are sent ASAP we specify noLinger() and noAsync()
KafkaSink<UUID, ActionTransition> sink
  = KafkaSink.<UUID, ActionTransition>create()
    .bootstrapServers("localhost:9092")
    .topic("actions")
    .keySerializer(UUIDSerializer.class)
    .valueSerializer(ActionTransitionSerializer.class)
    .noLinger()
    .noAsync()
    .build()

// Create a tracker and inform it we've started up
ActionTracker tracker 
  = PrimaryActionTracker.create()
    .application("your-app-id")
    .sink(sink)
    .build();
tracker.startupComplete();
```

Where `your-app-id` is a unique identifier for the logical application that this primary microservice forms part of.

Once you have your `ActionTracker` you can then call the relevant [methods](#actiontracker) as actions are orchestrated
e.g.

```java
try {
  tracker.start("backup");
  doBackup();
} finally {
  tracker.finish("backup");
}
```

Any [secondary](#secondary) services should have the corresponding `SecondaryBackupTracker` created and configured
appropriately.

### Secondary

Any secondary microservices within an application, i.e. those that don't orchestrate actions, **but**
need to be made aware of them should have a `SecondaryBackupManager` created e.g.

```java
EventSource<UUID, BackupTransition> source
  = KafkaEventSource.<UUID, BackupTransition>create()
    .bootstrapServers("localhost:9092")
    .topic("actions")
    .consumerGroup("your-consumer-group")
    .readPolicy(KafkaReadPolicies.fromEarliest())
    .commitOnProcessed()
    .keyDeserializer(UUIDDeserializer.class)
    .valueDeserializer(ActionTransitionDeserializer.class)
    .build();

ActionTracker tracker 
  = SecondaryActionTracker.builder()
    .application("your-app-id")
    .eventSource(source)
    .listeners(List.of(new YourCustomListener()))
    .build()
```

The secondary action tracker runs automatically on a background thread listening for transtion events sent by the
corresponding [primary](#primary).  `YourCustomListener`, and any other listeners you might configured for the tracker
will be called whenever the primary sends an action state transition event.

**IMPORTANT** Both the primary and secondary microservices must use the same `application()` ID value when constructing
their `ActionTracker` instances in order for states to be synchronised and your listeners invoked.  Any action
transition events for different application IDs are ignored and don't trigger.

This does however mean that all microservices within the Platform can share the same Kafka topic for action transition
events, as long as each logical application uses a unique application ID.

In order to avoid the secondary microservices having to always re-read past transition events then they commit their
offsets whenever they reach the Ready state.  As noted earlier the [state machine](#backuptrackerstate) defined for
backup operation states allows transitioning to Ready from any other state so the Ready state is always a safe point to
commit.

## Integrating into CLIs

For CLI driven applications the `cli-core` module provides a `ActionTrackerOptions` class that can be added to relevant
command classes e.g.

```java
// NB - If deriving from one of our provided command classes 
//      you may have a suitable Kafka options module already provided
@AirlineModule
private KafkaConfigurationOptions kafka = new KafkaConfigurationOptions();

@AirlineModule
private ActionTrackerOptions actionTrackerOptions = new ActionTrackerOptions();
```

Then in your commands actual logic you can use this to obtain a [primary](#primary) or [secondary](#secondary) instance
as necessary via the `getPrimary()` or `getSecondary()` methods without having to manually construct them yourselves e.g.

```java
ActionTracker tracker
  = this.actionTrackerOptions.getPrimary(this.kafka.bootstrapServers, 
  this.kafka,
  "your-app-id");
```

Or:

```java
ActionTracker tracker 
  = this.actionTrackerOptions.getSecondary(this.kafka.bootstrapServers, 
  this.kafka.getConsumerGroup(), 
  this.kafka,
  "your-app-id", 
  List.of(new YourCustomListener());
```

Once you have those you can utilise the [API](#actiontracker) in your application as needed.

# Dependency

This API is provided by the `action-tracker` module which can be depended on from Maven like so:

```xml
<dependency>
    <groupId>io.telicent.smart-caches</groupId>
    <artifactId>action-tracker</artifactId>
    <version>VERSION</version>
</dependency>
```

Where `VERSION` is the desired version, see the top level [README](../../README.md) in this repository for that
information.

Note that some of the other modules, e.g. [CLI Core](../cli/index.md) already depend on this module so an explicit
dependency may be unnecessary.