# Change Log

# 0.31.0

- JWT Auth Common improvements:
    - Improved how policy is located for JAX-RS resources to ensure that policy for inherited methods is properly
      located when those methods are invoked on child resource classes which may be overriding policy at the class level
          - **BREAKING** `PolicyLocator` helper methods gained an additional `Class<?>` argument to support this, this
          is an internal implementation detail so shouldn't affect most users

# 0.30.1

- JWT Auth Common improvements:
    - Improved various utility methods on the `Policy` class based on implementation experience
    - Added `TelicentRoles` and `TelicentPermissions` classes which provide constants related to standard roles and
      permissions within the Telicent Core platform
    - Added builder API for creating `UserInfo` instances, as well as generated utility methods
    - `AuthorizationResult` now provides reasons for both clients/end users and logging purposes, logging reasons may
      divulge details of the authorization policy and **MUST** only be used for logging/audit purposes.
- Build and Test improvements:
    - Upgraded Logback to 1.5.19
    - Upgraded Lombok to 1.18.42
    - Upgraded OpenTelemetry SDK to 1.54.1
    - Various build and test dependencies upgraded to latest available.

# 0.30.0

- Kafka Event Source improvements:
    - Fixed a bug where `KafkaConfiguration.OUTPUT_TOPIC` constant had the incorrect environment variable name.
    - Fix for a bug where trying to serialize `RdfPayload` back to Kafka where the event declares a `Content-Type`
      header for an RDF serialization that does not support `DatasetGraph` serialization would fail. Now provided the
      payload only contains a default graph in the dataset that graph will be successfully serialized. If it doesn't
      then the resulting Kafka `SerializationException` now has a clearer error message.
- JWT Auth Common improvements:
    - New abstract `TelicentAuthorizationEngine` and related helper classes for enforcing roles and permissions based
      authorization policies on Telicent applications
    - New `UserInfoLookup` API with concrete `RemoteUserInfoLookup` implementation for exchanging JWTs for User Info
      from an OAuth2/OIDC servers `/userinfo`, or equivalent, endpoint
- JAX-RS Base Server improvements:
    - When Authentication is enabled new Authorization and User Info Lookup features are also enabled
    - When configured via new `USERINFO_URL` environment variable a new request filter automatically retrieves User Info
      and adds it to the request properties.
    - Authorization policies are enabled by adding annotations to JAX-RS resource classes/methods as needed so
      applications can be safely upgraded without impact and enable authorization policies once they are ready
    - Clarified documentation around both Authentication and Authorization features
    - Removed long deprecated `development` (since `0.16.0`) authentication mode constants and related code
    - Authenticated requests can now have User Info automatically retrieved from a configured OAuth2/OIDC `/userinfo`
      and added to request attributes for other filters to use
    - `MockKeyServer` improvements:
        - Fixed a bug in `MockKeyServer` (used for authentication testing) that broke test isolation and could cause
          test classes to run successfully on their own but fail when run as part of a test suite
        - `MockKeyServer` now provides a `/userinfo` endpoint (see `getUserInfoUrl()`) which creates mock User Info
          responses based on the requests authenticated JWT injecting stub default `roles`, `permissions` and
          `attributes` values
        - **BREAKING** `MockKeyServer` moved AWS ELB endpoints to `/aws/{key}`, provided you were using the
          `registerAsAwsRegion()` method this should be transparent to you. If you were manually configuring custom AWS
          regions for tests you will need to adjust URLs accordingly.
- Build and Test improvements:
    - Adjusted Surefire configuration in some modules to eliminate Mockito related warnings
    - Adjusted Surefire configuration in some modules to ensure test coverage data is gathered and enforced
    - Improved test coverage in some modules where above adjustments showed coverage had fallen below intended levels
    - **BREAKING** Various build and test dependencies upgraded to latest available. This includes a change to Jackson
      version numbering which has now diverged between Jackson Core and Jackson Annotations, requiring the introduction
      of an additional version property in Maven for Jackson Annotations.
- CLI improvements:
    - Many CLI options now have equivalent environment variables that may be used to configure them without needing to
      explicitly specify them.

# 0.29.6

- Projector Driver improvements:
    - Optionally configurable log label for applications that run multiple `ProjectorDriver` instances to help
      distinguish log output from different projections.
- Build and Test Improvements:
    - Jackson upgraded to 2.20.0
    - Lombok upgraded to 1.18.42
    - OpenTelemetry SDK upgraded to 1.54.1
    - Various build and test dependencies upgraded to latest available

# 0.29.5

- Test improvements:
    - Added `LargeMessageKafkaTestCluster` to allow testing of messages up to 6MB in size.

# 0.29.4

- Updating Telicent HEADERS:
    - adding Distribution ID & Data Source Reference.
    - fixed a bug in CORS filtering that prevented allowed methods being recognised

# 0.29.3

- Minor Kafka improvements:
    - `AbstractReadPolicy` includes Kafka Consumer Group ID (if any) in logging of partition lag
    - `-t`/`--topic` option for CLI commands now permits comma separated lists of topics when specified via the `TOPIC`
      environment variable
    - CLI commands can now provide an application controlled default value to `KafkaOptions.getConsumerGroup()` to
      specify a more useful default group ID than the CLI command name derived one
- Build improvements:
    - Addressed some build warnings
    - Apache Commons IO upgraded to 2.20.0
    - Apache Jena upgraded to 5.5.0
    - Jackson upgraded to 2.19.2
    - Various build and test dependencies upgraded to latest available

# 0.29.2

- Projectors Core improvements:
    - Added a new `CircuitBreakerSink` that can be used to insert a circuit breaker into a pipeline.
- Projector Driver improvements:
    - Can now disable processing speed warnings for `ProjectorDriver` instances that will use low throughput event
      sources where processing speed is basically guaranteed to exceed remaining events.
- New `action-tracker` module
    - Provides an `ActionTracker` API that allow coordinating microservices to be made aware when the primary service is
      processing an action and react accordingly e.g. pause operations.
- CLI improvements:
    - New `ActionTrackerOptions` module that can be incorporated into CLI commands that allows microservices to obtain
      and use `ActionTracker` instances as required.
- Kafka Event Source improvements:
    - Fixed a bug where the `KafkaEventSource` could throw an error if trying to commit after being removed from a
      consumer group due to rebalance/timeout. In this scenario a warning is now logged and consumption resumes from
      the most recently committed offset upon the next `poll()` call.
- Build and test improvements:
    - Switched to new Maven Central publishing process
    - Airline upgraded to 3.1.0
    - Apache Commons Lang upgraded to 3.18.0
    - Caffeine upgraded to 3.2.2
    - Jackson upgraded to 2.19.1
    - JWT Servlet Auth upgraded to 1.0.3
    - OpenTelemetry SDK upgraded to 1.52.0
    - Various build and test dependencies upgraded to latest available

# 0.29.1

- Projectors Core improvements:
    - Fixed a bug where calling `ThroughputTracker.itemsProcessed(int)` with irregular increments could cause reporting
      to not trigger as intended
- Build and test improvements:
    - Upgraded Apache Kafka to 3.9.1
    - Upgraded OpenTelemetry SDK to 1.50.0
    - Upgraded various build and test dependencies to latest available

# 0.29.0

- Event Source Improvements:
    - **BREAKING** Introduced an `EventHeader` interface which the existing `Header` class implements
        - Various APIs that interact with `Event` have breaking signature changes on some methods to use the
          `EventHeader` interface rather than the `Header` implementation
        - Added new `RawHeader` implementation for event sources that treat header values as byte sequences e.g. Kafka
- Build and test improvements:
    - Apache Commons Collections upgraded to 4.5.0
    - Apache Commons IO upgraded to 2.19.0
    - Apache Jena upgraded to 5.4.0
    - Jackson upgraded to 2.19.0
    - JWT Servlet Auth upgraded to 1.0.1
    - Logback upgraded to 1.5.18
    - OpenTelemetry SDK upgraded to 1.49.0
    - Various build and test dependencies upgraded to latest available

# 0.28.2

- Build improvements
    - Updating Telicent Java Base Image - v1.2.5

# 0.28.1

- Kafka Event Source Improvements:
    - `KafkaTestCluster` improves how it creates and deletes topics during tests to ensure that topics are more reliably
      created and deleted as needed
    - `KafkaTestCluster` includes additional informative logging around topic creation and deletion to aid debugging
    - Fix a bug observed when running Surefire with multiple forks where `KafkaTestCluster` logging from different forks
      could be interspersed with each others leading to incoherent logs
- Projector Improvements:
    - Added `toString()` methods to most `Sink` implementations to improve developer debugging experience
- Projector Driver Improvements:
    - Added new `StallAwareProjector` interface that can optionally be implemented instead of base `Projector` interface
      if projectors want/need to be informed when projection stalls i.e. no new events are currently available
- Build and Test Improvements:
    - `SmartCacheCommandTester.runAsExternalCommand()` now includes the environment variables being set in the logged
      output and fixes an indentation error in the logged command arguments
    - Unnecessary `Thread.sleep()`'s removed from some test classes that should result in some reduction in build times
    - Upgraded Jackson to 2.18.3
    - Upgraded JWT Servlet Auth to 0.17.6
    - Upgraded Logack to 1.5.17
    - Upgraded SLF4J to 2.0.17
    - Upgraded various build and test dependencies to latest available

# 0.28.0

- Build and Test Improvements:
    - `SmartCacheCommandTester` adds support for easily running commands as external programs to allow writing
      integration tests that more directly represent how commands are run in deployments
    - Added default Maven Failsafe Plugin configuration and version control
    - Moved Code Coverage reporting and enforcement to `verify` phase in case any integration tests record additional
      test coverage data

# 0.27.1

- Build and Test Improvements:
    - Upgraded Apache Jena to 5.3.0
    - Upgraded Jakarta Validation to 3.1.1
    - Upgraded OpenTelemetry to 1.47.0
    - Upgraded OpenTelemetry Semantic Conventions to 1.30.1-alpha
    - Upgraded RDF ABAC to 0.73.4
    - Upgraded various build and test dependencies to latest available
    - Enabled Dependabot for Dockerfiles

# 0.27.0

- Event Source improvements:
    - `RdfPayload` has new `sizeInBytes()` method that exposes the raw byte sequence size, even if the byte sequence has
      been successfully deserialised and discarded.
- Kafka Event Source improvements:
    - Added new `KafkaReadPolicies.fromExternalOffsets()` static method that allows controlling the Kafka offsets from
      an external `OffsetStore`, rather than Kafka's Consumer Group offset tracking.
    - Added new `resetOffsets()` method to `KafkaEventSource` that allows resetting the offsets to either reprocess
      previous events, or skip over events, as desired.
    - `close()` is more tolerant of errors during closure and ensures that for all managed resources appropriate closure
      actions are taken.
- Projector Driver improvements:
    - `ProjectorDriver` explicitly logs unexpected errors that cause projection to be aborted
    - `ProjectorDriver` now provides additional get methods to allow inspecting some aspects of the driver
- Build and Test Improvements:
    - Further unit and integration test cases added around various `KafkaEventSource` behaviours

# 0.26.2

- Build improvements:
    - Fix some incorrect jaxrs-base-server dependency scope changes that could still cause runtime failures in some
      deployment scenarios
    - Jersey upgraded to 3.1.10
    - OpenTelemetry SDK upgraded 1.46.0

# 0.26.1

- Build improvements:
    - Revert incorrect movement of some jaxrs-base-server dependencies into test scope that was causing runtime failures

# 0.26.0

- Build improvements:
    - Revert upgrade to JAX-RS 4.0 as was causing cryptic build/runtime compatibility issues on some downstream projects
    - New `analyze.failOnWarnings` property can be overridden in modules/projects to skip `dependency:analyze-only` goal
      if the module/project is not yet ready for stricter dependency analysis.

# 0.25.2

- Build improvements
    - Minor dependency cleanup

# 0.25.1

- JAX-RS Base Server improvements:
    - Fixed a bug where `Problem` responses would default to `application/octet-stream` unexpectedly in some cases, it
      should now make best effort to conform to an appropriate `Content-Type` based on clients `Accept` header and any
      content negotation the JAX-RS framework has done prior to the problem being generated as a response.
        - A new `toResponse(HttpHeaders)` overload is added to facilitate this behaviour

# 0.25.0

- Projector improvements:
    - Added new `RejectSink` for explicitly rejecting items that don't match a filter condition.
- Event Source improvements:
    - Added new `EventHeaderSink` for modifying events by adding headers based upon generator functions, including
      built-in support for generating Telicent standard headers.
    - `TelicentHeaders` adds constants for more Telicent standard headers.
    - Adds missing documentation for the existing and new event sinks
- JAX-RS Base Server improvements:
    - Added support for serializing `Problem` response as plain text

# 0.24.2

- Projector improvements:
    - Added new `CleanupSink` to help with scenarios where a projection pipeline may require some `Closeable` resources
      that are not encapsulated in the pipeline itself, e.g. they're only used for initial setup/health probes. This
      new sink allows those to be encapsulated into a pipeline step so that however a pipeline exits it guarantees to
      `close()` those resources.
- Live Reporter improvements:
    - `LiveReporter` now logs a warning if destination sink fails to accept a heartbeat rather than aborting
      unexpectedly.
    - `LiveErrorReporter` now logs a warning if destination sink fails to accept an error report rather than erroring
      unexpectedly.
- Observability improvements:
    - `RuntimeInfo.printRuntimeInfo()` now includes available processors information.
- CLI improvements:
    - `LiveReporterOptions` now offers safer `teardown()` method that handles any unexpected teardown errors such that
      they don't confuse/hide the actual causes of the application termination. Old teardown methods are marked as
      `@Deprecated` with pointers to use the new method.

# 0.24.1

- Kafka Event Source improvements:
    - Improved `KafkaSink` error handling so any Kafka producer errors are reliably surfaced via `SinkException`'s when
      interacting with the sink
- CLI improvements:
    - `AbstractProjectorCommand` more proactively cancels the `ProjectorDriver` on receiving an interrupt, this mainly
      affected scenarios where a command was run in the background for integration tests
- Build improvements:
    - Apache Kafka upgraded to 3.9.0
    - Jackson upgraded to 2.18.1
    - Logback upgraded to 1.5.12
    - OpenTelemetry SDK upgraded to 1.44.1
    - Various build and test plugins upgraded to latest available

# 0.24.0

- Kafka Event Source improvements:
    - `KafkaTestCluster` abstract class now has a `getClientProperties()` method to supply a default set of additional
      client properties needed to connect to the Kafka cluster type being tested. This simplifies writing unit and
      integration tests against secure clusters in particular
- CLI improvements:
    - Fix several cases where extra Kafka configuration is not passed into ancillary components - Live Reporter and DLQs
    - `debug` CLI commands modified to work with secured Kafka configuration
- Build improvements:
    - Apache Jena upgraded to 5.2.0
    - Jersey upgraded to 3.1.9
    - JWT Servlet Auth upgraded to 0.17.4
    - Logback upgraded to 1.5.11
    - OpenTelemetry SDK upgraded to 1.43.0
    - RDF ABAC upgraded to 0.72.0
    - Various build and test dependencies upgraded to latest available

# 0.23.2

- JAX-RS Base Server improvements:
    - Improved error messages when API parameters are defined via `@BeanParam` annotated parameters
- Build improvements:
    - JWT Servlet Auth upgraded to 0.17.3
    - Various build and test dependencies upgraded to latest available

# 0.23.1

- Build improvements:
    - Addressing Trivy OOM errors in pipeline

# 0.23.0

- Kafka Event Source improvements:
    - **BREAKING:** `KafkaTestCluster` is now an abstract class, use `BasicKafkaTestCluster` for the default
      implementation of the interface
    - New `MutualTlsKafkaTestCluster` can be used to create a single node mTLS Authenticated Kafka Cluster provided
      that suitable Key and Trust Stores are generated
    - New `certs-helper` artifact provides helper scripts to enable generating these in other modules and test
      environments
- CLI improvements:
    - Added new `--kafka-login-type` option to select between different SASL mechanisms
    - Added new `--kafka-property` option to supply custom Kafka configuration properties directly at command line
    - Added new `--kafka-properties` option to supply Kafka properties file where more complex configuration is
      necessary, or users don't want to expose sensitive values in the CLI arguments
- Build improvements:
    - Excluded `protobuf-java` from transitive dependencies due to CVE-2024-7254 and adding explicit dependency on
      4.28.2
    - Removed `apache-jena-libs` from dependency management in favour of explicit dependencies on specific Jena
      libraries we use
    - JWT Servlet Auth upgraded to 0.17.0
    - Logback upgraded to 1.5.8
    - OpenTelemetry SDK upgraded to 1.42.1
    - RDF ABAC upgraded to 0.71.8
    - Various build and test dependencies upgraded to latest available

# 0.22.0

- JAX-RS Base Server improvements:
    - `ServerBuilder` now defaults to listening on `0.0.0.0`, i.e. all interfaces, rather than `localhost` which reduces
      inadvertent configuration errors where the server only listens on `localhost` and isn't accessible in some
      deployment scenarios e.g. running in a container.
    - `ServerBuilder` adds new `allInterfaces()` method to make it explicit that you want to listen on all interfaces
      when building your server.
    - New `RandomPortProvider` in `tests` module to make it easier to manage generating a sequence of psuedo-random port
      numbers to avoid port collisions between tests.
    - `AbstractHealthResource` now caches the computed `HealthStatus` for 30 seconds to remove the ability for a
      malicious user to DoS attack a server (and its underlying services where those are probed as part of computing the
      health status) via its `/healthz` endpoint.
    - **BREAKING:** `AbstractApplication` **MUST** now override a new `getHealthResourceClass()` method to supply either
      a resource class derived from `AbstractHealthResource` or `null` if it does not want to provide a `/healthz`
      endpoint. This change is designed to ensure that all application developers at least consider whether a health
      resource is needed and to ensure they receive the DoS attack mitigation also included in this release.
- Build improvements:
    - Removed unnecessary `logback.xml` from some library modules as these could conflict with application provided
      logging configurations
    - Removed Jetty dependency since only a tiny piece of utility code was being used and was replaced by usage of
      Apache Commons Lang
    - Apache Commons Lang upgraded to 3.17.0
    - Jersey upgraded to 3.1.8
    - JWT Servlet Auth upgraded to 0.16.0
    - Logback upgraded to 1.5.7
    - OpenTelemetry Agent upgraded to 1.33.6
    - OpenTelemetry SDK upgraded to 1.41.0
    - OpenTelemetry Semantic Convetions upgraded to 1.27.0-alpha
    - SLF4J upgraded to 2.0.16
    - Various build and test dependencies upgraded to latest available

# 0.21.2

- Build improvements:
    - Apache Commons Lang upgraded to 3.15.0
    - Apache Jena upgraded to 5.1.0
    - Apache Kafka upgraded to 3.8.0
    - Jetty upgraded to 12.0.12
    - JWT Servlet Auth upgraded to 0.15.3
    - OpenTelemetry Agent upgraded to 1.33.5
    - OpenTelemetry Semantic Conventions upgraded to 1.26.0-alpha
    - RDF ABAC upgraded to 0.71.4
    - Various build and test dependencies upgraded to latest available

# 0.21.1

- CLI improvements:
    - `HealthProbeServer` now runs on a background daemon thread with a more minimal thread pool footprint to improve
      health probe responsiveness and reduce resource consumption
    - `HealthProbeServer` now listens on `0.0.0.0` so is more reliably usable in containerised environments
- General improvements:
    - Several background threads that may be run by various APIs now explicitly set the thread name where possible to
      make it easier to debug applications using these features.

# 0.21.0

- CLI improvements:
    - New `cli-probe-server` module provides an embeddable `HealthProbeServer` that allows adding a minimalist HTTP
      server into CLI apps to provide suitable endpoints for Liveness and Readiness checks.
    - New `HealthProbeServerOptions` class in `cli-core` for adding this capability into your commands
    - **BREAKING** `AbstractProjectorCommand` now automatically includes `HealthProbeServerOptions` and sets up the
      server, derived classes need to implement a new `Supplier<HealthStatus> getHealthProbeSupplier()` method to supply
      a function that computes their applications readiness status.
- JAX-RS Base Server Improvements:
    - `HealthStatus` now provides a builder API
- Build Improvements:
    - Debug tools image now runs as `telicent-service` user instead of `root`
    - Jackson upgraded to 2.17.2
    - Jetty upgraded to 12.0.11
    - JWT Servlet Auth upgraded to 0.15.1
    - Apache Kafka upgraded to 3.7.1
    - Lombok upgraded to 1.18.34
    - OpenTelemetry upgraded to 1.40.0
    - Servlet API upgraded to 6.1.0
    - Various build plugins upgraded to latest available

# 0.20.2

- Build Improvements:
    - Nexus Staging plugin is now defined in a profile `telicent-oss` allowing it to be disabled for projects using this
      as a parent which don't wish to deploy via Sonatype OSS repositories

# 0.20.1

- JAX-RS Base Server Improvements:
    - `FallbackExceptionMapper` explicitly logs the stack trace for otherwise unhandled exceptions to aid in diagnosis
      of the underlying issues
- Build Improvements:
    - Upgraded various plugins to latest available

# 0.20.0

- Build improvements:
    - Reverted target JDK to 17 to increase portability
    - Upgraded JWT Servlet Auth to 0.15.0

# 0.19.0

- First public release to Maven Central
- Build improvements:
    - Added Maven GPG Plugin and Sonatype Nexus Plugin to facilitate release to Maven Central

# 0.18.1

- Build improvements:
    - Add some missing metadata towards meeting Maven Central release requirements
    - Jersey upgraded to 3.1.7
    - Jetty upgraded to 12.0.10
    - JWT Servlet Auth upgraded to 0.14.0
    - OpenTelemetry Agent upgraded to 1.33.3
    - OpenTelemetry upgraded to 1.39.0
    - RDF ABAC upgraded to 0.71.2
    - Various build plugins upgraded to latest available

# 0.18.0

- Bug Fixes:
    - Fixed a bug with how `PropertiesSource` resolves configuration values in some cases that could cause configuration
      to not be picked up correctly
- Build improvements:
    - Target JDK is now 21
    - JWT Servlet Auth upgraded to 0.12.0

# 0.17.0

- New `jwt-auth-common` module abstracts some constants and common configuration functionality out of
  `jaxrs-base-server` module to make it easier to reuse this in services that aren't using our JAX-RS base server.
    - **BREAKING** `AuthConstants` class moved to a new package in the `jwt-auth-common` module to avoid package overlap
      between this and the `jaxrs-base-server` module
- Build improvements:
    - Jetty upgraded to 12.0.8
    - JWT Servlet Auth upgraded to 0.11.1
    - OpenTelemetry upgraded to 1.38.0
    - Various test dependencies upgraded to latest available

# 0.16.1

- Build improvements:
    - Fix wrong dependency scope on some dependencies in the configurator module

# 0.16.0

- JAX-RS Base Server improvements:
    - `JwtAuthInitializer` refactored to utilise automated configuration support in `jwt-servlet-auth` libraries
    - JWT Authentication now supports additional configuration:
        - Default configuration amended to support common deployments better
        - Header Names and Prefixes used to detect and extract tokens may now be configured
        - Username claims may now be configured
    - New `MockKeyServer` in the `tests` module for creating a key server that can mock JWKS and AWS ELB key lookup and
      provide private keys for signing tokens in tests
    - Removed `development` mode authentication and authorization as it was fundamentally insecure and could lead to a
      false sense of security.
- Build improvements:
    - JWT Servlet Auth upgraded to 0.10.0
    - Logback upgraded to 1.5.6
    - OpenTelemetry upgraded to 1.33.2
    - RDF-ABAC upgraded to 0.71.0
    - Various plugins and test dependencies upgraded to latest available

# 0.15.1

- Logging improvements:
    - Standardised logging configuration for CLI tools
    - Reduced test logging to minimalise unnecessary build output
    - Added optional `logback-debug.xml` configuration file for use during tests via
      `-Dlogback.configurationFile=logback-debug.xml`
- Build improvements:
    - Various plugins and test dependencies upgraded to latest available

# 0.15.0

- Event Source improvements:
    - Malformed `RdfPayload`'s can also be written back to a Kafka topic as-is allowing them to be pushed to a DLQ for
      further analysis.
- CLI improvements:
    - **BREAKING** Type signature for `AbstractProjectorCommand.prepareDeadLetterSink()` changed to better reflect
      intended API usage and permit for creating multiple sinks for different points in the pipeline where desired.
- Build improvements:
    - Various dependencies upgraded:
        - Logback upgraded to 1.5.5
        - OpenTelemetry upgraded to 1.37.0
        - SLF4J upgraded to 2.0.13
        - Various plugins and test dependencies upgraded to latest available

# 0.14.1

- Event Source improvements:
    - `RdfPayload`'s are now lazily deserialized to avoid Kafka Head-of-Line blocking.
- Internal improvements:
    - Micro-optimisation of some internal logic to use more optimal code paths
- CLI improvements:
    - Some debugging scripts renamed to have explicit `.sh` extensions
- JAX-RS Base Server improvements:
    - `ServerBuilder` now exposes some ability to provide configuration around HTTP Header limits (quanity and size)
- Build improvements:
    - Various dependencies upgraded:
        - Apache Jena upgraded to 5.0.0
        - Jackson upgraded to 2.17.0
        - Jersey upgraded to 3.1.6
        - Jetty upgraded to 12.0.8
        - Kafka upgraded to 3.7.0
        - Logback upgraded to 1.5.3
        - OpenTelemetry upgraded to 1.36.0
        - Various plugins upgraded to latest available
        - Various test dependencies upgraded to latest available

# 0.14.0

- Build improvements:
    - Various dependencies upgraded:
        - Apache Jena upgraded to 5.0.0-RC1
        - Jetty upgraded to 12.0.6
        - JWT Servlet Auth upgraded to 0.8.0
        - OpenTelemetry upgraded to 1.35.0
        - RDF ABAC upgraded to 0.70.0
        - SLF4J upgraded to 2.0.12
        - Various test dependencies upgraded to latest available

# 0.13.4

- Minor non-breaking additional factory methods on metric events.

# 0.13.3

- Build improvements:
    - Various dependencies upgraded:
        - Jackson upgraded to 2.16.1
        - Jersey upgraded to 3.1.5
        - Jetty upgraded to 12.0.5
        - JWT Servlet Auth upgraded to 0.7.0
        - OpenTelemetry upgraded to 1.34.0
        - resilience4j upgraded to 2.2.0
        - SLF4J upgraded to 2.0.10
        - Various test dependencies upgraded to latest available
        - Various build plugins upgraded to latest available

# 0.13.2

- Build improvements:
    - Reverted maven-source-plugin due to a bug when doing a mvn release:perform

# 0.13.1

**NOTE** Due to a Maven plugin issue that causes mvn release:perform to fail 0.13.1 is not published as a release

- Build improvements:
    - Various dependencies upgraded:
        - resilience4j upgraded to 2.1.0
        - OpenTelemetry upgraded to 1.33.0
        - Airline upgraded to 3.0.0
        - Various test dependencies upgraded to latest available
        - Various build plugins upgraded to latest available

# 0.13.0

- Build improvements:
    - Various dependencies upgraded:
        - Apache Commons Lang upgraded to 3.14.0
        - Jackson upgraded to 2.16.0
        - Jersey upgraded to 3.1.4
        - Jetty upgraded to 12.0.4
        - Servlet API upgraded to 6.0.0
        - RDF ABAC upgraded to 0.50.0
        - Kafka upgraded to 3.6.1
        - Logback Classic upgraded to 1.4.14
        - OpenTelemetry upgraded to 1.32.0
        - Various build plugins upgraded to latest available
    - Added Cyclone-DX Plugin for generating Bill of Materials (BOM) for each module
- Event Source improvements:
    - Reverted `AbstractKafkaEventSourceBuilder.topic()` method to be consistent with pre 0.12.4 behaviour of setting a
      singular topic for the source
- CLI improvements:
    - Added new `-dlq`/`--dlq-topic` option for configuring a Dead Letter Topic for suitably enabled Kafka processing
      commands
- JAX-RS Base Server improvements:
    - Added new `MockAttributesServer` into the `tests` artifact for simpler testing of server applications that require
      an external authorisation source

# 0.12.5

- JAX-RS Base Server improvements:
    - Adds a new default filter into `AbstractApplication` declared classes so that any request failure (status >= 400)
      is explicitly logged with the failure reason (if available)
- Add CLI configuration parameters for DLQ topic, and provide a default implementation sink for indexing errors, when
  configured.

# 0.12.4

- Event Source Improvements:
    - New `OffsetStore` API for external offset storage
    - `KafkaEventSource`'s can now be configured with an external `OffsetStore` for use as offset storage in addition to
      the existing usage of Kafka consumer groups
    - A `KafkaEventSource` can now read from multiple topics
- Build Improvements:
    - Various dependencies updated to latest releases as appropriate

# 0.12.3

- Observability
    - Add events model to allow components' behaviour to be observed.
    - Add metrics model so components' processing can be observed as metrics.
    - Create `OpenTelemetryMetricsAdapter` to bridge our observable metrics to [Open
      Telemetry](https://opentelemetry.io/).

# 0.12.2

- Add dead letter queue message header, `TelicentHeaders.DEAD_LETTER_REASON`

# 0.12.1

- JAX-RS Base Server improvements:
    - Fix a bug where `ServerBuilder.contextPath()` could not be called with a plain `/` as the context path even though
      that is the default value indicating the root context.
    - Added a new `ServerBuilder.rootContextPath()` method to explicitly configure `/` as the desired context path.
- Build Improvements:
    - When `-DskipTests` is used on a build code coverage enforcement is now also skipped

# 0.12.0

- General Improvements:
    - Builder APIs that provide base classes now use self-referential type parameters to make their APIs more fluent and
      remove compiler constraints on calling the derived and base methods in any particular order.
    - `Sink` is now a Java `@FunctionalInterface` and has a default no-op `close()` implementation allowing simple sinks
      to be defined using lambdas.
    - Various dependencies upgraded to latest versions as appropriate.
- Event Source Improvements:
    - New `consumerConfig()` and `producerConfig()` methods on relevant builders to allow injecting arbitrary Kafka
      configuration properties.
    - New `plainLogin()` methods on relevant builders to allow providing credentials for Kafka clusters that enable
      authentication via Plaintext SASL.
    - New `SecureKafkaTestCluster` test harness for writing unit and integration tests against Kafka with Plaintext SASL
      login enabled.
    - `KafkaEvent` now exposes a `getConsumerRecord()` which provides access to the underlying Kafka `ConsumerRecord`
      instance where necessary.
- CLI Improvements:
    - `SmartCacheCommand` now provides `--verbose`, `--trace` and `--quiet` options that can reconfigure the root
      logging level at runtime. These are automatically provided to and honoured by any command that uses the
      `SmartCacheCommand` API to run itself. Since only the root logger level is changed any application logging
      configuration that explicitly sets the level for another logger remains honoured.
    - `SmartCacheCommand` processing will now dump runtime environment information (Memory, Java Version and OS) during
      command startup unless disabled via new `--no-runtime-info` option.
    - Added `--kafka-username` and `--kafka-password` options for supplying Kafka credentials for use with Plaintext
      SASL authentication.
    - **BREAKING** Deprecated `Defaults` class in favour of new `Configurator` API
- JAX-RS Base Server improvements:
    - Added `Accept`, `Content-Disposition` and `Content-Type` into `Access-Control-Allowed-Headers` response to CORS
      requests. While `Accept` and `Content-Type` were implicitly permitted due to the CORS safe-list not all values for
      those headers were permitted which prevented some APIs being called from a browser.
    - **BREAKING** Deprecated `CorsResponseFilter` in favour of `CrossOriginFilter` (from Jetty)
    - Added new `CorsConfigurationBuilder` for configuring CORS as desired.  `ServerBuilder` gains new `withCors()` and
      `withoutCors()` methods for passing in/manipulating this configuration as needed.
    - Default automatic server initialisation now includes printing runtime environment information (Memory, Java
      Version and OS).
- Debugging Improvements:
    - Packaged the `debug` CLI into a Docker image and provides some wrapper scripts for invoking common debug
      functionality needed by developers.
- New `configurator` module
    - Provides a minimalist lightweight abstraction for obtaining configuration values
    - Makes it much easier for developers to write unit and integration tests that require specific configurations

# 0.11.2

- Fix `KafkaEventSource` shutdown interruption being overly aggressive preventing actual graceful shutdown
- JaCoCo Code Coverage plugin configuration in parent `pom.xml` sets `append` to true so modules that run their tests
  via multiple independent Maven SureFire/FailSafe invocations have all their coverage collected and reported

# 0.11.1

- Event Source improvements:
    - `KafkaEventSource`:
        - Now handles a background thread calling `processed()` correctly by delaying offset commits until the thread
          that owns the underlying `KafkaConsumer` next accesses it.
        - Logging has been toned down to be less noisy at default log level (`INFO`) and some warnings are now issued
          less frequently
        - Topic existence checking used to avoid flooding the logs with Unknown Topic or Partition errors
        - Blocking `poll()` calls should be promptly interrupted when an application is shutting down
- Projector Driver changes:
    - **BREAKING**: Removed `pollWarnThreshold()` and associated logging from `ProjectorDriver` and its builder since
      this logging has proved to be unnecessary noise.
    - Toned down various aspects of the logging and made some statements be issued less frequently
- CLI Improvements:
    - If a projector command dies with an unexpected `Throwable` we now print the stack trace to standard error

# 0.11.0

- New `live-reporter` module
    - Provides a `LiveReporter` for reporting application status heartbeats for use by our Telicent Live monitoring
      application.
    - Provides a `LiveErrorReporter` for reporting application errors for use by our Telicent Live monitoring
      application.
- Event Source changes:
    - **BREAKING**: `event-source-file` module has refactored the internal implementation of `YamlEventSource` to reuse
      the Kafka serializers and deserializers to remove special case code. This involved some breaking changes to the
      internal APIs and implementations that are reflected elsewhere in these libraries.
        - Added a new `SingleFileEventSource` for special case of just ingesting a single file
        - Added a new `RdfFileEventSource` for ingesting plain RDF files (only `Content-Type` header derived from file
          format and no key support)
        - Added new `FileEventFormatProvider` interface driven by JVM `ServiceLoader` with `FileEventFormats` registry
- Projector Driver changes:
    - **BREAKING**: `ProjectorDriver` no longer has a public constructor and must now be built using a Builder pattern
      via `ProjectorDriver.create()`
    - **BREAKING**: Moved all driver related classes into `driver` package to avoid split package between this module
      and `projectors-core`
- CLI improvements:
    - Added new `--live-reporter`/`--no-live-reporter` option to relevant commands for enabling/disabling the new Live
      Reporter feature.
    - Added `--live-reporter-topic` and `--live-reporter-interval` options for configuring the Kafka topic to which
      status heartbeats are sent and how frequently they are sent.
    - Added `--live-bootstrap-servers` option for configuring the Kafka bootstrap topic for commands that wouldn't
      normally involve a Kafka connection.
    - Improved the restrictions related to configuring the event source, this should make it easier to use file event
      sources for testing when desired.
    - Added new `--source-file` option for providing a single file as an event source
    - Improved `--source-format` and `--capture-format` options to more actively reflect supported event file formats
- Base Server improvements:
    - Added new `RequestIdFilter` to the base application specification. This adds a `Request-ID` header to all HTTP
      requests and places it into the Logging `MDC` so logging configuration can include it in their patterns.
    - `CorsResponseFilter` now permits additional headers in pre-flight requests and responses.
    - When running in blocking mode on a background thread ensures that the server is stopped when that background
      thread is cancelled/interrupted
- **BREAKING**: `entity-collector` and `ies-configuration` modules removed:
    - These module have been migrated into the Smart Cache Search repository
- Dependencies
    - Various dependencies upgraded to latest minor/patch versions as appropriate

## 0.10.6

- Upgrade to jwt-servlet-auth 0.4.0
- JAX-RS Base Server improvements:
    - `JwtAuthEngineWithProblemChallenges` now supports configuring multiple possible authentication header sources
    - `JwtAuthInitializer` now defaults to supporting both `Authorization` and `X-Amzn-OIDC-Data` headers even when AWS
      token verification is not configured. AWS mode continues to only support `X-Amzn-OIDC-Data` header.

## 0.10.5

- **NB** Due to incorrect merge of 0.11.0-SNAPSHOT features into branch this release was removed

## 0.10.4

- Upgrade to rdf-abac 0.8.0

## 0.10.3

- Entity Collector changes:
    - Added new `PrimaryImageConverter`

## 0.10.2

- Entity Collector changes:
    - `MetadataConverter` can now exclude the usage of `GeneratedAtConverter` which can lead to making every generated
      document unique leading to unnecessary document processing

## 0.10.1

- Entity Collector changes:
    - `Ies4DocumentProviderV1` and `Ies4DocumentProviderV2` documentation updated to reflect some practical experience
      of their usage.

## 0.10.0

- Dependencies
    - Bump various dependencies and Maven plugins to latest available releases
- Event Source improvements:
    - Added new `RdfPayload` container type that can hold either an additive RDF Dataset or a mutative RDF Patch
    - Corresponding Kafka serializer/deserializer support for the new type
    - New `KafkaRdfPayloadSource` and corresponding builder
- Entity Collector improvements:
    - `EntityCentricProjector` now operates over events with `RdfPayload` value
    - `EntityToMapSink` now preserves the input `Entity` value as the key of the output event
    - New `EntityDocumentFormatProvider` interface for providing `ServiceLoader` driven selection of desired output
      document format expressed as a set of `EntityMapOutputConverter` instances
    - New `EntityToMapOutputConverter` implementations for adding metadata to generated documents
- CLI improvements:
    - `AbstractKafkaRdfProjectionCommand` now defined in terms of `RdfPayload` instead of `DatasetGraph`
    - `SmartCacheCommandTester` now captures standard output and error for each test to files, or optionally tee's them
      directly to actual standard output and error to aid in debugging failing tests

## 0.9.0

- Removed Prometheus Metrics in favour of Open Telemetry, added a new `observability-core` module to provide some helper
  utilities around this
- Added a new `/version-info` endpoint to the `jaxrs-base-server`
- `entity-collector` improvements:
    - Added `hasAnyLiterals()` method to `Entity` class
    - Added new `EntityToMapOutputConverter` implementations that produce more upsert friendly document structures
- CLI improvements:
    - `cli-core` tests classifier artifact now provides a framework for writing unit test cases around CLI commands via
      `SmartCacheCommandTester`
    - Projector commands now have a configurable `--poll-timeout` option available to configure how long to wait on each
      `poll()` call to the underlying `EventSource`
- `event-source-kafka` improvements:
    - Added a `KafkaSink` for writing `Event` instances back to Kafka
    - Added a `KafkaTestCluster` into the tests classifier artifact that provides a simple test Kafka cluster via Test
      Containers
    - Fixed a bug where offsets were not correctly committed on `close()` if a `KafkaEventSource` was in auto-commit
      mode and the source was closed precisely when its internal events buffer was empty

## 0.8.7

- Fix bug with automatic configuration of Hierarchy Lookup URL by `UserAttributesInitializer`

## 0.8.6

- Fix bug with default labels not being fully realised in entity to document conversions which can potentially result in
  subsequent overwriting of default labels
    - Also ensures `PrimaryNameConverter` includes fine-grained security labels in its output

## 0.8.5

- Add new `PrimaryNameConverter` for outputting top level `primaryName` field in entity to document pipelines

## 0.8.4

- The `AbstractHealthResource` in `jaxrs-base-server` module now protects against bad derived implementations that fail
  to generate a suitable `HealthStatus` object generating an appropriate 503 Service Unavailable when this is the case
- Upgrade rdf-abac dependency to 0.6.0

## 0.8.3

- Upgrade rdf-abac dependency to 0.5.0
- Added support for plaintext event capture format

## 0.8.2

- Fix `--capture-format` option not generating the correct file extension on event files when using `YAML_GZ` as the
  format

## 0.8.1

- Simplified some APIs around event capture
    - `FileEventReader` and `FileEventWriter` merged into a single interface
    - Added a `GZipEventReaderWriter` as a decorator that compresses and decompresses events with GZip
    - New `CapturingEventSource` as a decorator around another `EventSource` to more cleanly introduce the event capture
      sink

## 0.8.0

- Add Fluent `SinkBuilder` API to make defining pipelines easier
- In `jaxrs-base-server` the `HealthStatus` and `Problem` model classes are now fully deserializable
- `EventSource` now has a `processed()` method that can be called to indicate when events have actually been processed
  allowing implementations to delay relevant state storage e.g. committing Kafka offsets
    - New `EventProcessedSink` as a terminal sink that calls this
- `Event` can now optionally provide a reference back to its `EventSource`
- Fixed some corner case bugs with `KafkaEventSource` and `KafkaReadPolicy` implementations:
    - If re-assigned the same partition multiple times during application lifetime don't seek multiple times
    - Ensure underlying `KafkaConsumer` is explicitly closed when the event source is closed
- Added a `FileEventSource` that supports replaying events from files on disk
    - Added an `EventCapturingSink` for capturing events for later replay
    - Added a `--source-directory` and `--capture-directory` option to abstract commands in `cli-core` that enables this
      as an event source and/or a capture target

## 0.7.2

- Bug fix around handling of namespace prefixes in relation to security labels graph processing

## 0.7.1

- Ensure default security labels are stored with each portion of the document so if defaults change in future events the
  previously applied labels are not overridden

## 0.7.0

- Entity Collector Changes
    - `EntityData` now stores values as `SecurityLabelledNodes` that allow attaching a security label to each node
    - Various `EntityToMapOutputConverter` instances are now able to include security labels in their output
- `ServerBuilder` can now optionally exclude some paths from authentication e.g. `/healthz`

## 0.6.1

- Fixed JAX-RS Base Server not supporting encoded slashes in URIs which is needed as often our IDs are URIs which we
  percent encode for use in an API call and this bug was preventing that from working

## 0.6.0

- Added a new `jaxrs-base-server` module for rapidly spinning up new JAX-RS Server applications
    - Provides a `ServiceLoadedServletContextInitialiser` which means only a single `ServletContextListener` need be
      registered in an application to pull in all the common configuration listeners
        - Includes listeners for setting up JWT Authentication and User Attribute Authorization
    - Provides `Problem` and a selection of base `ExceptionMapper`'s for consistently reporting errors as RFC 7807
      Problem data structures
    - Provides base classes for easily creating JAX-RS server instances for testing and runtime deployment
- Improved Kafka lag instrumentation and reporting
    - `EventSource` can now report its remaining events (if known and/or calculable)
    - `KafakEventSource` and its subclasses will now regularly report current read positions for all assigned partitions
      at a configurable interval (default 1 minute)
    - `ProjectorDriver` checks and reports remaining events when encountering a stall (no events received after
      blocking)
    - New `PeriodicAction` class to avoid flooding the logs with low lag warnings when an application is caught up
- Added Prometheus metrics support to relevant APIs
    - `ThroughputTracker` now reports items received, processed and processing rate
    - `KafkaEventSource` (and derived types) report poll timings, fetch sizes (in number of events) and current lag
    - `cli-core` module now includes `PrometheusOptions` on `SmartCacheCommand` that allows any CLI to opt into exposing
      Prometheus metrics
    - `jaxrs-base-server` module has opt-in support for collecting HTTP request metrics and exporting Prometheus metrics
      from the server
- Fluent API Improvements
    - `ThroughputTracker`'s are now defined via a fluent builder interface
    - `KafkaEventSource` (and derived types) are now defined via a fluent builder interface
- Entity Collector Improvements
    - All `EntityToMapOutputConverter` implementations omit their output field if they generate no output for a given
      entity.
    - `IesTypeSelector` handles some special cases that come up when working with IES knowledge

## 0.5.0

- Introduced an `Event` interface that is now returned from an `EventSource`
    - This provides access to event headers as well as both the key and value
    - `EventSource` must now take both a key and value type parameter
    - Various other types have additional type parameters in order to satisfy this new contract
    - Added `EventKeySink` and `EventValueSink` for transforming from an event to just its key/value
- Added new `KafkaDatasetGraphSource` and associated `DatasetGraphDeserializer`
    - Deprecated the existing `KafkaGraphSource`
- Entity Collection APIs refactored to operate over `Event` instances
    - `Entity` may now have a security labels associated with it
    - `EntityCentricProjector` now operates over `Event<TKey, DatasetGraph>` and projects `Event<TKey, Entity>`
    - `EntityToMapSink` now operates over `Event<TKey, Entity>` and outputs `Event<TKey, Map<String, Object>>`
        - `SecurityLabelsConverter` for outputting security labels
    - `EntitySelector.select()` now takes in security labels as well as the graph to select entities from

## 0.4.0

- Renamed `EventSource.availableInFuture()` to `EventSource.isExhausted()` and clarified associated documentation and
  usage examples

## 0.3.1

- Improved type safety of some `Sink` implementations by providing explicit generic type parameters

## 0.3.0

- Search API moved out of this repository
- Initial documentation provided

## 0.2.0

- Split out into separate repository
- Removed ElasticSearch implementation from this repository

## 0.1.0

- Initial API definitions for common Smart Cache functionality
    - Event Sources
    - Projectors and Sinks
    - Entity Collection
    - Search index management, indexing and search
    - Configurability for all the above
- ElasticSearch Indexing pipeline built upon these APIs
