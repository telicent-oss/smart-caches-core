# Security Plugins

The `SecurityPlugin` interface provides an abstraction around the Telicent Core Platform's label based security model
allowing applications to enforce the model without needing any direct knowledge of how the labels are interpreted and
enforced.

## Design

In this section we first outline the design motivation behind the interfaces that are defined, see
[Interfaces](#interfaces) for outlines of the various interfaces and their usages.  As with everything else in our Core
Libraries this follows our general [Design Ethos](../design.md#design-ethos).

### Label Format & Syntax

As background, remember that all events flowing through Kafka within the Core Platform are expected to have a
`Security-Label` header present that indicates the labels that apply to the data.  Additionally, RDF format events on
the `knowledge` topic *MAY* optionally include a labels graph that provides fine grained labels for individual triple(s)
within the event.  Currently these labels are expressed as [RDF-ABAC Attribute Expressions][1] which are strings
declaring an Attribute Based Access Control (ABAC) policy in the form of attribute expressions.

As part of evolving the platform towards a more flexible Policy Based Access Control (PBAC) model the new plugin APIs
introduced from `0.25.0` onwards instead treat the `Security-Label` as opaque byte sequences.  These byte sequences have
an optional 4 byte schema prefix to allow plugins to detect whether they support a particular label schema and reject
labels they don't support.  This also potentially provides the capability for future plugins to provide support for
multiple label schemas coexisting within the platform.

When present the schema prefix consists of the 4 byte sequence `0x1e ? ? 0x1e`, where `0x1e` is the record separator
byte, and the 2nd and 3rd bytes are an encoded `short` denoting a schema identifier. This allows for up to ~65k unique
label schemas, which even allowing for versioning of schemas should be more than we ever expect to use.  The
`SecurityPlugin` interface offers static `encodeSchemaPrefix()` and `decodeSchemaPrefix()` methods for writing and
reading this prefix.  Note that for `decodeSchemaPrefix()` the return type is the reference `Short` type allowing `null`
to be returned when the prefix is not present.

Making this schema prefix optional is an intentional choice, with the schema identifier assumed to be `0` in this case.
This allows us to make this security API evolution backwards compatible with existing usage of RDF-ABAC labels within
our deployments as any label we see without the prefix byte sequence can be assumed to be an RDF-ABAC label.

Within a label schema the remainder of the byte sequence for a label can be used to encode labels in whatever format an
implemention sees fit.  So taking RDF-ABAC, which will become our default plugin, as an example then it treats the byte
sequence as a UTF-8 encoded string.  However plugin implementions are free to use whatever label format and/or encoding
scheme they see fit since the API will treat the byte sequence opaquely.  For example, a plugin implementation with a
more complex PBAC labelling approach might choose to encode the label in a binary format, e.g. [Protobuf][Protobuf],
[Apache Avro][Avro], [Apache Thrift][Thrift] etc.

The `SecurityLabels<T>` interface treats labels in abstract terms, allowing API users to be treat the labels in entirely
abstract terms, while a plugin can internally decode the encoded byte sequence into whatever data structures it needs to
operate over.  A plugins `SecurityLabelsParser` converts from the opaque byte sequences into a plugins specific
`SecurityLabels` implementation, note that there is no corresponding encoding interface needed as access to the opaque
byte sequence is always provided by the `SecurityLabels` interface so applications that need to store labels for later
evaluation can simply store the opaque byte sequence directly.

### Entitlements

Our existing security model closely couples RDF-ABAC labels with the User Attributes returned by the [Telicent
Access][TcAccess] service where user attributes are returned in a specific JSON object schema.  Applications are also
strongly coupled to Access in that they need to be directly aware of configuring themselves to talk to it in order to
retrieve user attributes to provide label enforcement decisions.

Therefore as part of this new API we adopt a similar approach to [Labels](#label-format--syntax) in that we move to
treat user entitlements as opaque byte sequences with responsibility for retrieving user entitlements left to plugins
via their `EntitlementsProvider` implementation.  This means that the burden of configuring any necessary supporting
services, e.g. connectivity to Access, becomes an internal implementation detail of a plugin, rather than an application
concern. Entitlements use the same optional schema prefix as part of the byte sequences to allow plugins to distinguish
between different entitlement schemas they may support.

As with labels the `Entitlements<T>` interface treats entitlements in abstract terms from an application perspective
while allowing a plugin implementation to decode the entitlements into whatever data structure(s) are appropriate. Again
there is an `EntitlementsParser` interface so a plugin can supply whatever decoding logic it needs for its
implementation.

### Data Access Enforcement

Data Access Enforcement is done by combining the users retrieved [Entitlements](#entitlements) with the
[Labels](#label-format--syntax) present on the data.  This is done by first preparing an `Authorizer` via the
`prepareAuthorizer()` method passing in the users entitlements.  `Authorizer` instances are intended to be scoped to the
lifetime of a single user request so they may cache any access decisions if they encounter the same labels repeatedly.

Once an application has an `Authorizer` it calls the `canAccess()` method passing in the labels for the data it needs an
access decision for.  This returns either `true` for accessible, or `false` for forbidden.  In the event of any
problem/ambiguity in computing an access decisions plugins **MUST** fail-safe by defaulting to returning `false` if they
can't make an access decision.

You can see the complete lifecycle outlined later in [Example Usage](#example-usage).

### Plugin Loading

Plugins are loaded by applications by calling `SecurityPluginLoader.load()` which uses the JVM `ServiceLoader` to locate
registered plugins.  The loaded plugin is cached for the lifetime of the JVM so subsequent calls just return the
previously loaded instance.

The expectation is that there is `1`, and **ONLY** `1` plugin registered in this way.  If no plugin is registered, more
than 1 plugin is registered, or loading the registered plugin throws an error, then the system defaults to a fail-safe
mode and instead loads the unregistered `FailSafePlugin`. In fail-safe mode the system treats all labels and
entitlements as invalid and defaults all access decisions to forbidden.  This will be clearly and explicitly noted in
the logs, and an explicit `Error` is thrown upon the first, and all subsequent load attempts to make it clear to the
application that it has been misconfigured.

Plugins will be loaded into applications by mounting them into the containers as a volume at a well known path -
`/opt/telicent/security/plugins/` - that all applications **MUST** add to their classpath if it exists.  Plugins
**SHOULD** be mounted as read-only files to minimize the possibility of any tampering.

The proposed plugin mounting mechanism is that us, or the customer, builds a container image that contains their plugin
(and its dependencies) and uses a K8S init container to copy these into an `emptyDir` volume that will be shared with
the main application container.  This init container can be injected into all relevant application manifests by way of
common `kustomize` patches.

Telicent will provide a default patch that injects our [Default Plugin](#default-plugin).

## Interfaces

### `SecurityPlugin`

`SecurityPlugin` is the top level entrypoint API by which applications interact with the Security Plugin API.  Primarily
this is just a place for applications to obtain instances of the other interfaces they need to carry out data processing
and access control tasks.

An implementation should generally make the instances of the other interfaces singletons where the API allows it e.g.
there should be a single shared `SecurityLabelsParser`.  However implementations **MUST** be aware that they will be
used in multi-threaded environments thus any singleton instance they return **MUST** be thread-safe.

Note that some of the interfaces a plugin provides instances of have specific lifecycles associated with them, e.g. `Authorizer`, so for those implementations **MUST** ensure they respect the defined lifecycle of that interface.

### `IdentityProvider`

TODO

### `EntitlementsProvider`

TODO

### `EntitlementsParser`

TODO

### `SecurityLabelsParser`

TODO

### `SecurityLabelsValidator`

TODO

### `SecurityLabelsApplicator`

TODO

### `Authorizer`

TODO

## Example Usage

In the following example we show the complete lifecycle of how an application would make an access decision:

```java
// Load the plugin, this is cached for the lifetime of the JVM so repeated calls are cheap
SecurityPlugin plugin = SecurityPluginLoader.load();

// Get the Users Entitlements and prepare an Authorizer based upon those
Entitlements<?> entitlements = plugin.entitlementsProvider().entitlementsForUser("some-user");
Authorizer authorizer = plugin.prepareAuthorizer(entitlements);

// Get the Labels Parser
SecurityLabelsParser parser = plugin.labelsParser();

// Get results from the underlying data store
List<SomeResult> results = runQuery();
for (int i = 0; i < results.size(); i++) {
  // Filter the results based on the labels present
  byte[] rawLabels = results.get(i).getSecurityLabels();
  try {
    SecurityLabels<?> labels = parser.parseSecurityLabels(rawLabels);
    if (!authorizer.canAccess(labels)) {
        results.removeAt(i);
        i--;
    }
  } catch (MalformedLabelsException e) {
    // Could be thrown if the labels are not supported by this plugin, or otherwise malformed
    results.removeAt(i);
    i--;
  }
}

// Return the filtered results
return results;
```

## Default Plugin

Telicent will provide a default plugin implementation that wraps our existing RDF-ABAC security model.  This allows
existing deployments to continue to function as before as applications are migrated onto the new Security APIs.

## Implementation Concerns

### Separating Logic and Registration

As described [earlier](#plugin-loading) the system expects `1`, and only `1`, plugin to be registered.  At the same time
it is also designed to allow for multiple label and entitlement schemas to potentially co-exist in a single plugin.

This means that generally implementations should be done as two modules:

- A Logic module that provides the actual interface implementations
- A Registration module that depends on the logic module and provides the appropriate `META-INF/services` file to
  register the plugin.

By implementing things in this way existing implementations of different label schemas can be combined by creating a new
plugin that depends on the logic modules of one/more existing plugin implementations, with its implementations wrapping
those existing implementations.  This can then be registered as a combined plugin via its own registration module.

### Caching for Performance

In real deployments the actual pool of distinct labels is often relatively small.  Therefore implementations can
generally make intelligent caching decisions, e.g. caching the results of parsing label byte sequences into
implementation specific data structures.  Over the lifetime of an application which may make hundreds of thousands/millions of access decisions this can offer significant performance benefits.

Similarly within an `Authorizer` instance an implementation may wish to cache access decisions so frequently seen labels can be rapidly evaluated.

By placing such concerns onto plugin implementations we aim to allow applications to remain unaware of this concern and
avoid littering each application with their own caching logic.


[1]: https://github.com/telicent-oss/rdf-abac/blob/main/docs/abac-specification.md#attribute-expressions
[Protobuf]: https://protobuf.dev
[Avro]: https://avro.apache.org
[Thrift]: https://thrift.apache.org
[TcAccess]: https://github.com/telicent-oss/telicent-access