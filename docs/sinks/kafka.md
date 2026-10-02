# Kafka Sink

The `KafkaSink<TKey, TValue>` provides the ability to write [`Event<TKey, TValue>`](../event-sources/index.md#event)
instances back to a Kafka topic.  This allows writing events back to Kafka as the final step of a pipeline.

Note that this sink is provided by the [`event-source-kafka` module](../event-sources/kafka.md) not the
`projectors-core` module so requires a dependency on the `event-source-kafka` module to be available.

## Behaviours

- Terminal
- Transforming: No
- Batching: No

## Parameters

The primary parameters are the Kafka bootstrap servers, topic and serializers for the key and value types.

Optionally you may also configure the linger milliseconds which controls how much latency the Kafka producer trades off
for improved batching of sent events.

Depending on the use case you may wish to configure [Asynchronous or Synchronous
Sends](#asychronous-send-and-error-handling) as appropriate, the default behaviour is asynchronous sends.

## Example Usage

In this example we configure a sink with our desired Kafka destination and serialisers:

```java
try (KafkaSink<Integer, String> sink 
        = KafkaSink.<Integer, String>create()
                   .bootstrapServer("localhost:9092")
                   .topic("your-topic")
                   .keySerializer(IntegerSerializer.class)
                   .valueSerializer(StringSerializer.class)
                   .lingerMs(5)
                   .build()) {
    for (Event<Integer, String> input : someDataSource()) {
        sink.send(input);
    }
}
```

Here we are using the Kafka built-in `IntegerSerializer` and `StringSerializer` classes but, you can use your own custom
Kafka `Serializer` implementations if you want to support your own types.

A `lingerMs()` of 5 milliseconds means that the underlying `KafkaProducer` will wait for up to 5 milliseconds for
additional events to be received before sending them onwards to Kafka.  This trades off a small amount of latency in
favour of reducing the number of requests that are sent to the Kafka brokers.

## Asychronous Send and Error Handling

By default a `KafkaSink` uses the asynchronous `KafkaProducer.send()` API so in most cases calling `send()` on the
`KafkaSink` will return immediately.  The one exception to this is upon the first send attempt where the call may block
while the `KafkaProducer` obtains the topic and partition metadata for the topic you are writing to.

Any asynchronous errors that occur are captured and will be surfaced via a `SinkException` on subsequent
`send()`/`close()` calls on the sink.  In asynchronous mode all asynchronous Kafka errors received by the time of next
`send()`/`close()` call are grouped into a single `SinkException`, these Kafka errors may be accessed and inspected via
the `getSuppressed()` method on that exception.

Alternatively the caller can provide their own callback function via the `async(Callback)` method on the builder.  When
that is used then the callers provided callback is responsible for collecting up any asynchronous errors that occur and
acting upon them accordingly.

### Synchronous Send

You can also put the sink into synchronous mode by calling the `noAsync()` on the builder, in this case a call to
`send()` on the sink will block until the `KafkaProducer` has either successfully produced the message, or an error
occurs.  This mode may be useful for tests where you want to guarantee that your events are successfully produced before
running tests that consumes those events.  This mode is **NOT** recommended for production usage.

In synchronous send mode only a single error at a time is surfaced via the resulting `SinkException` so the underlying
Kafka error is populated as the cause of that exception, thus accessible via the `getCause()` method.

## Retry Handling

From `1.5.0` onwards we introduced a `KafkaRetryHandler` interface that may be configured on a `KafkaSink` via the
`retryHandler()` builder method.  A retry handler allows the sink to retry sending events that Kafka APIs report as
failed, it consists of 3 methods:

- `isRetryable(Exception e)` - Allows the retry handler to check whether the exception is recoverable and thus send
  should be retried.
- `maxRetries()` - Indicates the maximum number of retries to send the event that will be made, this is in addition to
  the initial send attempt i.e. if maximum retries is `3` then the event may attempt to be sent `4` times before the
  sink gives up and produces an error.
- `prepareEventForRetry(Event, Exception)` - Prepares the event for retry, modifying it if desired, or returning `null`
  if no further retries should be made.

When a retry handler is configured if a `send()` operation on the sink fails (whether
[asynchronous](#asychronous-send-and-error-handling) or [synchronous](#synchronous-send)) then the sink automatically
retries the send if, and only if, the retry handler indicates `isRetryable()`, the number of retry attempts is less than
or equal to `maxRetries()` and `prepareEventForRetry()` returns a non-null event.

There is only one concrete implementation provided, the `DlqRetryHandler`, this retries only when a
`RecordTooLargeException` is produced and prepares the event by stripping the value. This handles an edge case of DLQs
that occurs when input events are at/near the maximum permitted event size and adding the DLQ headers pushes the event
over that limit.  By stripping the value the event with all the DLQ header information can be successfully sent, and the
DLQ headers will normally provide pointers back to the original input event that can be used to diagnose the root cause.
The `KafkaSink` builder provides a convenience `forDlq()` method that configures this as the retry handler.

> **NB** For [asynchronous sending](#asychronous-send-and-error-handling) (the default behaviour) the retry handling is
> implemented via an internal Kafka producer `Callback`, therefore configuring a `KafkaSink` with both a retry handler
> and a custom `Callback` is not permitted and will produce an `IllegalArgumentException` at sink `build()` time.
