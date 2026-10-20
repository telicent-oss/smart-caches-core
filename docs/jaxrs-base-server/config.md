# Configuration in the JAX-RS Base Server

The `jaxrs-base-server` module provides a number of features related to configuring the server and providing application
specific configuration.  We would suggest you read the [Creating an Application](index.md#creating-an-application)
documentation first before reading this documentation:

- [Configuring at Startup/Shutdown](#serverconfiginit)
- [Requiring Configuration to accept requests](#context-requirements)

## `ServerConfigInit`

The
[`ServerConfigInit`](../../jaxrs-base-server/src/main/java/io/telicent/smart/cache/server/jaxrs/init/ServerConfigInit.java)
interface is a small extension to the `ServletContextListener` interface that adds a name and priority to the interface.
The intent of this is to allow listeners to be automatically discovered (via `ServiceLoader`) and applied in a
well-defined order without having to manually define the listeners.  By using a `ServiceLoader` driven mechanism we need
only define a single explicit listener and provided suitable `META-INF/services` files are present on the Classpath all
our listeners will be discovered and loaded.

When you use a [`ServerBuilder`](index.md#serverbuilder) you can ensure this listener is registered via the
`withAutoConfigInitialisation()` method.  As already noted if this isn't done then some of the other features described
in this documentation will not function correctly.

The `getName()` value is used for logging purposes so should be something meaningful to a developer or system
administrator reviewing the logs.

The `priority()` value of these instances allows the initialisation order to be automatically controlled.  A higher
value is considered higher priority and will be initialised first.  When the server is stopped the initialisers are
destroyed in reverse priority order i.e. lowest priority will be destroyed first.

## Context Requirements

From `1.3.0` the module introduces a new `@RequireContextAttribute` annotation and associated `RequireContextFilter`
that is automatically registered by [`AbstractApplication`](index.md#creating-an-application).  This annotation and
filter are used to simplify a commonly observed application pattern:

1. An application uses one/more [Init](#serverconfiginit) instances to configure shared objects used throughout the
   application.
2. Application resources then retrieve this attribute, check that it is present, and if not abort the request with a 503
   Service Unavailable.

This pattern ends up with boilerplate code like the following throughout an applications resources e.g.

```java
public class ExampleResource {

  public Response example(@Context ServletContext servletContext) {
    // Check if the shared object is properly configured
    MySharedObject thing = (MySharedObject) servletContext.getAttribute("some-attribute");
    if (thing == null) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }
    // Do actual API implementation and return real response...
  }
}
```

If you have many resource methods in your API then this is a lot of duplicated code that adds little value to the
readability of the code and often detracts from actual method implementation, especially for simple methods.

With this feature this becomes instead:

```java
@RequireContextAttribute(
  value = "some-attribute", 
  type = MySharedObject.class, 
  errorTitle = "Service Unavailable",
  errorDetail = "Required shared object not correctly configured"
)
public class ExampleResource {

  public Response example(@Context ServletContext servletContext) {
    // Get the shared object, if we've reached the method its guaranteed to be correctly configured
    MySharedObject thing = (MySharedObject) servletContext.getAttribute("some-attribute");
    // Do actual API implementation and return real response...
  }
}
```

This has a couple of advantages:

1. We can define the annotation at the class, and parent class, level in order to apply the requirement
   to many resources within our APIs.
2. You can define multiple of these annotations in order to enforce multiple requirements for complex APIs that require
   multiple shared objects to function.

The filter discovers annotations at the resource method, resource class and parent class levels and applies all of them
independently.  So if you have one method in a resource class that requires more than one shared object, while most only
require one shared object you can have the common one defined at the class level and the specialised one defined at the
method level.

Additionally the annotation supports `forbiddenTypes`, for example if you defined your `type` to be some interface type,
but know your application uses some special marker implementation(s) of that type to indicate misconfigurations, then
you can explicitly forbid those marker implementation(s) like so:

```java
@RequireContextAttribute(
  value = "some-attribute", 
  type = MySharedInterace.class, 
  forbiddenTypes = { Marker.class }, 
  errorTitle = "Service Unavailable", 
  errorDetail = "Required MySharedInterface not correctly configured"
)
```