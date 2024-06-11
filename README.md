# Smart Caches Core (Java)

This repository provides Core Libraries intended for rapidly building Smart Cache implementations in Java. It provides
a lightweight functional API for defining pipelines as well as more admin/operations oriented APIs for invoking and
running Smart Cache pipelines and API Servers.

## Build

This is a Java project built with Maven, please see [BUILD](BUILD.md) for detailed build and development instructions.
Provided you meet the basic requirements the following should work:

```bash
$ mvn clean install
```


## Usage

Usage of this repository is primarily by declaring [dependencies](#depending-on-these-libraries) on one/more of the
library modules provided and then using their APIs for your own Smart Cache development.  Please see the
[Documentation](docs/index.md) for introductions to the various libraries and APIs provided.

### Modules

The following modules are currently available:

- `cli` - Provides a CLI for exercising this code base and running the Projectors.
    - `cli-core` - Provides the Core CLI API for implementing new commands.
    - `cli-debug` - Provides commands for various tools that do not constitute direct functionality, e.g. dumping the
      RDF from a Kafka topic, but which are useful for debugging. This can be invoked via the `debug` script in this
      module.
- `configurator` - Provides a lightweight configuration API for obtaining application configuration.
- `event-sources` - Provides Event Sources.
    - `event-sources-core` - Provides an API for representing and accessing Event Sources.
    - `event-source-kafka` - Provides a Kafka backed implementation of the Event Source API.
- `jaxrs-base-server` - Provides a base JAX-RS server template to build server applications from.
- `live-reporter` - Provides the ability to report heartbeat status to Telicent Live.
- `observability-core` - Provides utilities around integrating Open Telemetry metrics into Smart Caches.
- `projector-driver` - Provides the ability to connect together an Event Source and a Projector.
- `projectors-core` - Provides an API for defining Projectors and the processing of their output(s) via Sinks.

Please refer to the [Documentation](docs/index.md) for more details about the functionality of each module and the APIs
it provides.

### Run

This repository does not really provide runnable code, rather it provides libraries that are used as building blocks to
create runnable code in other repositories.

There are however some debugging tools found in the `cli/cli-debug` module that can be run via the included
`cli/cli-debug/debug.sh` script.  See the [`debug.sh`](docs/cli/debug.md) documentation for more details.

### Depending on these Libraries

The current stable version of these libraries is `0.19.0`, and the development version is `0.19.1-SNAPSHOT`.

Please see [CHANGELOG](CHANGELOG.md) to track any changes to the APIs.  Since we are currently on pre 1.x versions 
breaking changes **MAY** happen in any release.

# License

This code is Copyright (C) Telicent Ltd and licensed under the Apache License 2.0

See [LICENSE](LICENSE) and [NOTICE](NOTICE) for more details.
