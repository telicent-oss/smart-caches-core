# Security Plugins

The `SecurityPlugin` interface provides an abstraction around the Telicent Core Platform's label based security model
allowing applications to enforce the model without needing any direct knowledge of how the labels are interpreted and
enforced.

## Design

In this section we first outline the design motivation behind the interfaces that are defined, see
[Interfaces](#interfaces) for outlines of the various interfaces and their usages.

### Label Format & Syntax

As background, remember that all events flowing through Kafka within the Core Platform are expected to have a
`Security-Label` header present that indicates the labels that apply to the data.  Additionally, RDF format events on
the `knowledge` topic *MAY* optionally include a labels graph that provides fine grained labels for individual triple(s)
within the event.  Historically these labels were expressed as [RDF-ABAC Attribute Expressions][1] which are strings
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
treat user entitlements as opaque byte sequences with responsibility for retrieving user entitlements left to
`SecurityPlugin` implementations.  This means that the burden of configuring any necessary supporting services, e.g.
connectivity to Access, becomes an internal implementation detail of a plugin, rather than an application concern.
Entitlements use the same optional schema prefix as part of the byte sequences to allow plugins to distinguish between
entitlements they support.

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

You can see the complete lifecycle outlined later in [Example Usage](#example-usage)

## Interfaces

TODO

## Example Usage

In the following example we show the complete lifecycle of how an application would make an access decision:

```java
// Load the plugin, this is cached for the lifetime of the JVM so repeated calls are effectively a no-op
SecurityPlugin<?, ?> plugin = SecurityPluginLoader.load();

// Get the Users Entitlements and an Authorizer
Entitlements<?> entitlements = plugin.entitlementsProvider().entitlementsForUser("some-user");
Authorizer<?> authorizer = plugin.prepareAuthorizer(entitlements);

// Get the Labels Parser
SecurityLabelsParser<?> parser = plugin.labelsParser();

// Get results from the underlying query
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

```

## Default Plugin

TODO


[1]: https://github.com/telicent-oss/rdf-abac/blob/main/docs/abac-specification.md#attribute-expressions
[Protobuf]: https://protobuf.dev
[Avro]: https://avro.apache.org
[Thrift]: https://thrift.apache.org
[TcAccess]: https://github.com/telicent-oss/telicent-access