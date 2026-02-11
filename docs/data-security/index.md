# Data Security Plugins

The `data-security-core` module provides a `DataSecurityPlugin` API that can be used to implement label driven data
security in applications without hard coupling to a specific labelling schema and enforcement implementation.  To learn
more about this API and its motivations see the [Design](design.md) document.

In this repository we provide a single concrete implementation of this API - `data-security-plugin-rdf-abac` - that uses
our [RDF-ABAC][RdfAbac] libraries to enforce RDF-ABAC label expressions.

## Using a Data Security Plugin

The registered plugin is obtained via the `DataSecurityPluginLoader.load()` method.  Once a `DataSecurityPlugin`
instance is obtained then its various APIs may be used as desired.

> **IMPORTANT** 1, and **ONLY** 1, plugin may be [registered](#registration), if no/multiple plugins are registered then this will throw
> an error.

### Use with JAX-RS Base Server

For developers building from our [JAX-RS Base Server](../jaxrs-base-server/index.md#data-security) then some integrated
support for data security plugins is provided out of the box.

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