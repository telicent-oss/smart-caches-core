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

```

## `/healthz`

The `/healthz` endpoint is intended for use by readiness probes, it responds with either a HTTP 200 OK if the
application is ready, or a 503 Service Unavailable if the application is unready.  It is the responsibility of the
application using the health probe server to provide a useful readiness supplier function that returns meaningful
readiness information.

The response in either case is a JSON payload like the following:

```json

```

# Usage 

A Health Probe server requires 3 pieces of information to run:

1. A display name for the server used in logging
2. A port upon which the server is exposed
3. A readiness supplier that can calculate the applications current readiness, used by the server in responding to 

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