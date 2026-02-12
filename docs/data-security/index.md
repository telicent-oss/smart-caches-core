# Data Security Plugins

The `data-security-core` module provides a `DataSecurityPlugin` API that can be used to implement label driven data
security in applications without hard coupling to a specific labelling schema and enforcement implementation.  To learn
more about this API and its motivations see the [Design](design.md) document.

In this repository we provide a single concrete implementation of this API - `data-security-plugin-rdf-abac` - that uses
our [RDF-ABAC][RdfAbac] libraries to enforce RDF-ABAC label expressions.

## Using a Data Security Plugin

The registered plugin is obtained via the `DataSecurityPluginLoader.load()` method.  Once a `DataSecurityPlugin`
instance is obtained then its various APIs may be used as desired.

> **IMPORTANT** 1, and **ONLY** 1, plugin may be [registered](#registration), if no/multiple plugins are registered then
> this will throw an error.

Once you have the `DataSecurityPlugin` instance then you call its various methods to return instances of the other API
as necessary to fulfill a task.  These methods either return a thread-safe singleton which can be freely shared and
reused, or a request scoped `AutoCloseable` that should have request scoped lifetime.  If the method returns an
`AutoCloseable` then this *SHOULD* be used in a `try-with-resources` block to ensure it is `close()`'d in a timely
manner.

### Applying Labels during Event Projection

To apply labels during event projection you would use the `SecurityLabelsApplicator` API:

1. Call `prepareApplicator(byte[], Graph)` to obtain a `SecurityLabelsApplicator`.
    - This is an `AutoCloseable` so *SHOULD* be used in a `try-with-resources` block
2. As you process the event call `labelForTriple(Triple)` on the applicator to find what label should be applied to a
    given triple.
3. `close()` the applicator once you've processed the event

Optionally you may also want to call `labelsValidator()` to obtain a `SecurityLabelsValidator` and use its
`validate(byte[])` method to check if the provided default label is valid at the start of processing each event.

### Enforcing Labels during Data Access

To enforce labels during data access you use a combination of the `SecurityLabelsParser` and the `DataAccessAuthorizer`
APIs:

1. Firstly `labelsParser()` to obtain a `SecurityLabelsParser`.
    - This will be a thread-safe singleton so can be reused and shared freely
2. Call `prepareAuthorizer(RequestContext context)` to obtain a `DataAccessAuthorizer` for the current users request.
    - This will be an `AutoCloseable` scoped to the current request so **MUST** only be used for the current request,
      using a `try-with-resources` block is recommended.
3. As data is retrieved use the `SecurityLabelsParser.parseSecurityLabels(byte[])` to parse stored labels into
`SecurityLabels<?>` and pass those to `DataAccessAuthorizer.canRead(SecurityLabels<?>)` to test whether labelled data
should be visible to the user.
4. `close()` the authorizer to free any resources and request state it is holding.

### Use with JAX-RS Base Server

For developers building from our [JAX-RS Base Server](../jaxrs-base-server/index.md#data-security) then some integrated
support for data security plugins is provided out of the box that should make it relatively straightforward to apply the
above described [label enforcement](#enforcing-labels-during-data-access) process.

### Registration

Plugins are dynamically registered via the standard JVM `ServiceLoader` mechanism.  As already noted only 1 plugin is
permitted to be registered and available at any one time.  Therefore Telicent applications are designed to automatically
add a specific location to the end of their classpath - `/opt/telicent/security/plugins` - if it exists at runtime. This
allows for our applications to be developed purely against the generic `data-security-core` APIs and the appropriate
concrete plugin implementation supplied at runtime.

In a production K8S deployment this location is an `emptyDir` volume shared between the application pod and its init
containers with the init container used to copy the desired plugin (and its dependencies) into that location.  See
[Deployment Notes](design.md#plugin-loading-in-deployments) for more details.

Plugin and application developers should refer to the [Dependency Notes](design.md#dependencies) for which dependencies
they should use.

[RdfAbac]: https://github.com/telicent-oss/rdf-abac/blob/main/