# CLI Health Probe Server

The `cli-probe-server` module provides a minimalist Health Probe Server based upon our [JAX-RS Base
Server](../jaxrs-base-server/index.md).  This is intended for incorporation into CLI applications that wouldn't normally
have a HTTP interface and allows them to easily expose liveness and readiness probes for consumption by container
runtimes and other monitoring.

# HTTP API

The Health Probe Server provides a very simple HTTP API with just two endpoints:

## `/version-info`

The `/version-info` endpoint is intended for use by liveness probes, it always responds with a HTTP 200 OK and returns a
JSON payload containing version information for the application so doubles as a way to check deployed application
versions.  Example output is as follows:

```json
{
  "search-api": {
    "buildEnv": "Mac OS X 26.0 (aarch64)",
    "groupId": "io.telicent.smart-caches.search",
    "name": "Telicent Smart Caches - Search - Core API",
    "artifactId": "search-api",
    "version": "0.18.1-SNAPSHOT",
    "revision": "86b197ff24003a7bdd92d64b6e5f802a217dd63b",
    "timestamp": "2025-10-01 09:46:43"
  },
  "projectors-core": {
    "buildEnv": "Linux 6.11.0-1018-azure (amd64)",
    "groupId": "io.telicent.smart-caches",
    "name": "Telicent Smart Caches - Projectors - Core API",
    "artifactId": "projectors-core",
    "version": "0.29.6",
    "timestamp": "2025-09-23 13:11:25",
    "revision": "d6a380b03a54c025cc5f325a798e1e2721b37c11"
  },
  "cli-elastic-index": {
    "buildEnv": "Mac OS X 26.0 (aarch64)",
    "groupId": "io.telicent.smart-caches.search",
    "name": "Telicent Smart Caches - CLI - ElasticSearch Index",
    "artifactId": "cli-elastic-index",
    "version": "0.18.1-SNAPSHOT",
    "revision": "86b197ff24003a7bdd92d64b6e5f802a217dd63b",
    "timestamp": "2025-10-01 09:47:27"
  },
  "cli-probe-server": {
    "buildEnv": "Linux 6.11.0-1018-azure (amd64)",
    "groupId": "io.telicent.smart-caches",
    "name": "Telicent Smart Caches - CLI - Health Probe Server",
    "artifactId": "cli-probe-server",
    "version": "0.29.6",
    "timestamp": "2025-09-23 13:12:31",
    "revision": "d6a380b03a54c025cc5f325a798e1e2721b37c11"
  },
  "event-source-kafka": {
    "buildEnv": "Linux 6.11.0-1018-azure (amd64)",
    "groupId": "io.telicent.smart-caches",
    "name": "Telicent Smart Caches - Event Sources - Kafka",
    "artifactId": "event-source-kafka",
    "version": "0.29.6",
    "timestamp": "2025-09-23 13:11:51",
    "revision": "d6a380b03a54c025cc5f325a798e1e2721b37c11"
  },
  "search-index-elastic": {
    "buildEnv": "Mac OS X 26.0 (aarch64)",
    "groupId": "io.telicent.smart-caches.search",
    "name": "Telicent Smart Caches - Search - ElasticSearch",
    "artifactId": "search-index-elastic",
    "version": "0.18.1-SNAPSHOT",
    "revision": "86b197ff24003a7bdd92d64b6e5f802a217dd63b",
    "timestamp": "2025-10-01 09:47:03"
  }
}
```

The number of components for which version information is reported will depend upon the configuration of the
application.

## `/healthz`

The `/healthz` endpoint is intended for use by readiness probes, it responds with either an HTTP 200 OK if the
application is ready, or a 503 Service Unavailable if the application is unready.  It is the responsibility of the
application using the health probe server to provide a useful readiness supplier function that returns meaningful
readiness information.

The response in either case is a JSON payload like the following:

```json
{
  "healthy": true
}
```

It may potentially contain additional information, particularly if the service is reporting that it is unhealthy e.g.

```json
{
  "healthy": false,
  "reasons": [
    "Failed to obtain readiness status from underlying Index Manager ElasticIndexManager{connection=localhost:9200}"
  ]
}
```

# Usage 

A Health Probe server requires 3 pieces of information to run:

1. A display name for the server used in logging
2. A port upon which the server is exposed
3. A readiness supplier that can calculate the applications current readiness, used by the server in responding to the
   `/healthz` requests.

## Automatic Integration

If you are using our [`cli-core`](index.md) framework and have derived from `AbstractProjectorCommand` then the
health probe server is automatically integrated into your command per [Health Probe
Server](index.md#health-probe-server-support).

You can override the following methods to configure various aspects of the health probe server behaviour:

1. Override `getHealthProbeDisplayName()` to set the servers display name for logging purposes.
2. Override `getHealthProbeSupplier()` to provide a `Supplier<HealthStatus>` that can calculate a health status for your
   application to power the `/healthz` reponse.
3. Override `getHealthProbeLibraries()` if you want to expose version information for additional components in the
   `/version-info` response.

End users can control the port of the server via the `--health-probe-port` option and enable/disable it via the
`--health-probes`/`--no-health-probes` options.  Equivalent environment variables - `HEALTH_PROBES_PORT` and
`ENABLE_HEALTH_PROBES` - are also provided for this.

## Manual Integration

To manually integrate this into a command ensure that `HealthProbeServerOptions` is added as an `@AirlineModule` in your
command class, then from your commands run logic call `setupHealthProbeServer()` with suitable options:

```java

public class ExampleHealthProbeIntegration extends SmartCacheCommand {
  @AirlineModule
  private HealthProbeServerOptions healthProbes = new HealthProbeServerOptions();

  public int run() {
    this.healthProbes.setupHealthProbeServer("Example", () -> checkHealthy(), "some-custom-component");
    try {
      // Real application logic goes here...
    } finally {
        this.healthProbes.teardownHealthProbeServer();
    }

    return 0;
  }

  private HealthStatus checkHealthy() {
    // Determine and return a HealthStatus here
  }

  public static void main(String[] args) {
    SmartCacheCommand.runAsSingleCommand(ExampleHealthProbeIntegration.class, args);
  }
}
```

# Dependency

The `cli-probe-server` module provides the health probe server machinery, it can be depended on from Maven like so:

```xml
<dependency>
    <groupId>io.telicent.smart-caches</groupId>
    <artifactId>cli-core</artifactId>
    <version>VERSION</version>
</dependency>
```

Where `VERSION` is the desired version, see the top level [README](../../README.md) in this repository for that
information.