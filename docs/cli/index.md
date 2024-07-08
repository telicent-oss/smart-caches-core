# CLI

The CLI modules provide machinery that aids in creating CLI entrypoints for Smart Cache pipelines. There is a
`cli-core` module that provides the actual API for this, and a `cli-debug` module which provides a `debug` CLI with a
couple of example commands available.

# API

The actual CLI parsing machinery is provided by the [Airline][1] library, the `cli-core` module basically handles the
dependency management for this and provides useful base classes.

All commands should be derived from the `SmartCacheCommand` class, this automatically adds the `-h/--help` option to
these commands, and defines an abstract `int run()` method that commands must implement. It also has some useful static
helper methods:

- `SmartCacheCommand.runAsSingleCommand(Class, String[])` - Can be called to automate creating a parser, invoking it and
  running the command. This is for CLIs with a single entrypoint i.e. a single command.
- `SmartCacheCommand.handleParseResult(ParseResult)` - Can be called with a `ParseResult` to automate either displaying
  help, parser errors or running the command that parsing selected. This is for use with CLIs with multiple entrypoints
  i.e. a CLI with multiple sub-commands (`git` being an obvious example developers will be familiar with).

# Usage

Assuming you have a command class that derives from `SmartCacheCommand`, and is suitably annotated with the Airline
[`@Command`][2], then you can add a simple `main()` method like so:

```java
import com.github.rvesse.airline.annotations.Command;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;

@Command(name = "example", description = "An example command")
public class Example extends SmartCacheCommand {

    // Any command options/arguments you need are defined via annotated fields here

    // A simple main method that just calls the relevant static method from SmartCacheCommand
    public static void main(String[] args) {
        SmartCacheCommand.runAsSingleCommand(Example.class, args);
    }

    @Override
    public int run() {
        // Actual command logic goes here

        // Return the desired exit code based on command success/failure
        return 0;
    }
}
```

This will generate a parser for your command, parse the users inputs and populate an instance of your command class, in
this case `Example`, before calling its `run()` method where your actual command logic happens. If the user has
requested help then that would be shown. Or if the user failed to provide options/arguments that your command declared
as required (or with other restrictions upon their values) then relevant error messages are shown instead.

Any command derived from `SmartCacheCommand` will automatically have the following options available:

- `-h`/`--help` - Requests that help for a command be displayed.
- Several options related to reconfiguring application logging which are detailed under [Logging
  Options](#logging-options).
- Several options related to [Live Reporter](../live-reporter/index.md) which are detailed under [Live Reported
  Support](#live-reporter-support).

If you invoke your actual command via the static helper methods shown in the above example then these options are
automatically processed and acted upon as appropriate.

A number of abstract base classes are also provided for common patterns e.g. a command that applies a projector to an
event source:

- `AbstractProjectorCommand` - Abstract base class for commands that apply a projector
    - `AbstractKafkaProjectorCommand` - Abstract base class for commands that apply a projector and support Kafka as an
      event source
       - `AbstractKafkaRdfProjectionCommand` - Abstract base class for commands that apply a projector to a Kafka
         knowledge topic whose event values are RDF Datasets

Using these base classes will add a bunch of additional options to your commands relevant to those commands e.g.
`--bootstrap-server` for specifying Kafka bootstrap servers.

Note that it is also possible to write unit tests against your commands, see the later section on
[Testing](#testing-commands) for how to do this.

## Logging Options

The `SmartCacheCommand` class includes the `LoggingOptions` module which provides the following three options:

- `--quiet` - Reduces log verbosity to only `WARN` and `ERROR` messages.
- `--verbose` - Increases log verbosity to include `DEBUG` messages.
- `--trace` - Increases log verbosity to include `TRACE` messages.

If multiple of these options are supplied then the most verbose logging level would apply.

When these options are used they cause the root logger level to be adjusted as noted above.  Since **ONLY** the root
logger level is modified if your application wants to enforce a certain log level on a specific logger it can do that
via its Logback configuration file.  This will be honoured regardless of whether a user supplies a logging option
because only the root loggers level is modified.

## Live Reporter support

The `cli-core` module integrates [Live Reporter](../live-reporter/index.md) support, meaning that commands are able to
generate heartbeats that are reported to Telicent Live to help in visualizing the state of the Telicent Core Platform.

Several options are provided to all commands derived from `SmartCacheCommand` to allow end users to configure Live
Reporter as desired:

- `--live-reporter`/`--no-live-reporter` - Enables/Disables the live reporter functionality.
- `--live-reporter-topic <topic>` - Specifies the topic to which live heartbeats are sent, defaults to `provenance.live`
- `--live-report-interval/--live-reporter-interval <seconds>` - Specifies the heartbeat interval in seconds, defaults to
  15 seconds.
- `--live-bootstrap-server/--live-bootstrap-servers <bootstrap-servers>` - Specifies the Kafka cluster to which
  heartbeats are sent.

In order to actually configure the live reporter a command must override the `setupLiveReporter()` method and in that
implementation call `this.liveReporter.setupLiveReporter()` passing in suitable parameters for your application.
Assuming you are calling `SmartCacheCommand.runAsSingleCommand()` as suggested above then the `setupLiveReporter()`
method will be called for you so provided you override it appropriately then the reporter will be setup.  Conversely
provided you have called `this.liveReporter.setupLiveReporter()` the base implementation will also make a best effort to
terminate the live reporter appropriately when the application exits.

For an example implementation of this take a look at `AbstactKafkaProjectionCommand`.

## Health Probe Server support

The `cli-core` module can optionally provide a [Health Probe Server](health-probes.md) meaning that commands that
wouldn't normally have an HTTP interface can provide useful liveness and readiness probes with minimal additional coding.

To enable this support you need to add the `HealthProbeServerOptions` to your command class e.g.

```java
@AirlineModule
private HealthProbeServerOptions healthProbes = new HealthProbeServerOptions();
```

If your command class derives from `AbstractProjectorCommand` then it will have these options available by default and
there's no need to manually add them.  You merely need to override the `Supplier<HealthStatus> getHealthProbeSupplier()`
method to return a supplier function that can calculate the readiness of your application.

This options class provides a `setupHealthProbeServer()` method that is used to configure and start the health probe
server, it requires a display name, a health status supplier and optionally a list of library version information you
wish to expose via the liveness probe.  Again, if deriving from `AbstractProjectorCommand` then this is automatically
called for you.

By default, the health probe server runs on port `10101`. This can be configured by the user via the
`--health-probe-port` option.  The user can also choose to disable health probes entirely via the `--no-health-probes`
option.

There is also a corresponding `teardownHealthProbeServer()` method that should be used to terminate the server when you
are done with it, again commands derived from `AbstractProjectorCommand` will call this automatically.  Note that a
health probe server always registers a JVM Shutdown Hook so attempts to shut itself down when the JVM exits gracefully
so it isn't essential to explicitly call the teardown method.

# Dependency

The `cli-core` module provides the base command classes and machinery, it can be depended on from Maven like so:

```xml
<dependency>
    <groupId>io.telicent.smart-caches</groupId>
    <artifactId>cli-core</artifactId>
    <version>VERSION</version>
</dependency>
```

Where `VERSION` is the desired version, see the top level [README](../../README.md) in this repository for that
information.

# Implementations

## `debug` CLI

The `cli-debug` module provides an example CLI with developer debugging tools, see the [`debug`](debug.md) documentation
for more details.

## Other Implementations

Other implementations based on `cli-core` may be found in other repositories, for example the [Search Smart Cache][3]
repository provides an `elastic-index` command.

## Testing Commands

The `cli-core` module also includes the ability to write unit tests around command implementations by using the `tests`
dependency i.e.

```xml
<dependency>
    <groupId>io.telicent.smart-caches</groupId>
    <artifactId>cli-core</artifactId>
    <version>VERSION</version>
    <classifier>tests</classifier>
    <scope>test</scope>
</dependency>
```

With this module as a test dependency you can use the `SmartCacheCommandTester` class to help in writing unit tests.
The key thing is to call the `setup()`, `resetTestState()` and `teardown()` methods in your test class e.g.

```java
public class TestExampleCommand {

    @BeforeClass
    public void setup() {
        // Enables command testing mode including output stream capture
        SmartCacheCommandTester.setup();
    }

    @AfterMethod
    public void testCleanup() {
        // Resets test state by discarding previously captured state
        SmartCacheCommandTester.resetTestState();
    }

    @AfterClass
    public void teardown() {
        // Disables command testing mode including restoring original output streams
        SmartCacheCommandTester.teardown();
    }

    @Test
    public void example_01() {
        // Invoke your commands main() method with any arguments you want to parse it to test
        ExampleCommand.main(new String[] { "--flag", "--option", "value"});

        // Get the parse result and make assertions about it...
        ParseResult<ExampleCommand> result = SmartCacheCommandTester.getLastParseResult();
        Assert.assertTrue(result.wasSuccessful());

        // Get the exit status and make assertions about it...
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 2);

        String lastStdOut = SmartCacheCommandTester.getLastStdOut();
        // Make some assertions about standard output contents...
        String lastStdErr = SmartCacheCommandTester.getLastStdErr();
        // Make some assertions about standard error contents...
    }
```

Note that if your tests don't need any additional setup you can choose to derive your test class from
`AbstractCommandTests` in this module and those setup and teardown methods will already be done for you.


[1]: https://github.com/rvesse/airline
[2]: http://rvesse.github.io/airline/guide/annotations/command.html
[3]: https://github.com/Telicent-io/smart-cache-knowledge-search
