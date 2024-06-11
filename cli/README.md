# Fake Platform

The `fake-platform` script in this directory spins up several fake applications using the Debug CLIs `fake-reporter`
command. Each of these applications generates Live Heartbeats and Errors to the `provenance.live` and `provenance.
errors` Kafka topics. This can be consumed by the Telicent Live stack to visualise a simple fake platform of linked
applications.

## Run

The script needs to know the Kafka server to talk to, this can be supplied either via the `BOOTSTRAP_SERVERS`
environment variable, or as the first argument to this script e.g.

```bash
$ ./fake-platform.sh localhost:9092
```

## Build

The relevant Java code is all built as part of the normal top level `mvn` build, see instructions in the top level
[README](../README.md).

The fake platform can also be built into a Docker container like so:

```bash
$ cd cli
$ docker build -t fake-platform:latest -f Dockerfile --build-arg PROJECT_VERSION=0.15.2-SNAPSHOT .
```

Where the value of `PROJECT_VERSION` is based upon the version of the codebase you're building the image from. This
is useful because it allows developers who don't develop with Java day to day to run up this particular instance of
fake data generation for Telicent Live.

## Running the Docker Container

Once the Docker container is built you can run it as follows:

```bash
$ docker run -it --rm -e BOOTSTRAP_SERVERS=kafka1:19092 --network=core fake-platform:latest
```

The above assumes that you're running the local deployment version of CORE with docker compose since it connects to
the `core` network and specifies the URL for connecting to the Kafka instance running in its container.

You'll see a bunch of output and then the following:

```bash
4 fake applications are running
Waiting for jobs to run, interrupt this process to abort...
```

As the output says you can stop/interrupt the container to tear down the fake applications and thus the container
itself.
