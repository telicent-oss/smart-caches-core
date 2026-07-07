# JAX-RS Base Server

The JAX-RS Base Server module provides a bunch of utility code for quickly building new JAX-RS servers.  It provides a
lot of building blocks around the following:

- [Error Handling](#error-handling) and Reporting via [RFC 7807 Problem][1] Responses
- [JWT Authentication](#jwt-authentication)
- [Roles and Permissions Based Endpoint Authorization](#authorization)
- [Data Security](#data-security)
- [Cross-Origin Resource Sharing (CORS)](#other-utilities)
- [Server configuration initialization](#serverconfiginit)
- [Application stubs](#creating-an-application), [servers](#serverbuilder) and [entrypoints](#creating-an-entrypoint)

## Creating an application

To create a new JAX-RS server add a [Dependency](#dependency) as detailed later and then create a new class extending
`AbstractApplication`.  This class should override `getClasses()`, ensuring that it calls the super-class method and
appends any application specific classes to it e.g.

```java
@ApplicationPath("/")
public class SearchApplication extends AbstractApplication {
    @Override
    public Set<Class<?>> getClasses() {
        // Get the super-class set of classes
        // i.e. all the common functionality the base application class provides
        Set<Class<?>> classes = super.getClasses();

        // Add any application specific functionality
        // Parameter Converters
        classes.add(SearchParamConvertersProvider.class);
        // Resources i.e. actual API paths
        classes.add(DocumentsResource.class);
        return classes;
    }

    @Override
    protected Class<? extends AbstractHealthResource> getHealthResourceClass() {
        // Health Resource, or null if you prefer not to derive from our AbstractHealthResource
        return HealthResource.class;
    }

    @Override
    protected boolean isAuthEnabled() {
      return true;
    }
}
```

If you are implementing a [`/healthz`](#healthz-endpoint) following the libraries provided pattern you can return that
class in your overridden `getHealthResourceClass()` method.  If you prefer to not provide such an endpoint, or implement
it by other means then you can return `null` here.

As seen in the above example another key consideration is whether you want authentication enabled which you can do by
overriding the `isAuthEnabled()` method from the base class.  You may wish to calculate this value based on
environment/system property variables if you want authentication to be configurable as on/off in your application.

**NB** If you don't derive from `AbstractApplication` and instead extend the JAX-RS `Application` class directly then
you won't get many of the features described here as those rely on the `AbstractApplication` registering various JAX-RS
filters, resources, exception handlers etc.

## Creating an entrypoint

To create a runnable entrypoint for our application we can extend `AbstractAppEntrypoint` to construct our `Server`
instance and implement a `main()` method on it e.g.

```java
public class SearchApiEntrypoint extends AbstractAppEntrypoint {

    public static void main(String[] args) {
        SearchApiEntrypoint entrypoint = new SearchApiEntrypoint();
        // true causes the JVM to start the server and block until the process is interrupted
        entrypoint.run(true);
    }

    @Override
    protected ServerBuilder buildServer() {
        // In this method we build our server definition
        return ServerBuilder.create()
                .allInterfaces()
                .port(8181)
                .application(SearchApplication.class)
                .withAutoConfigInitialisation()
                .displayName("Search REST API");
    }
}
```

Here we see the [`ServerBuilder`](#serverbuilder) API used to build the definition of our actual server.

## `ServerBuilder`

The `ServerBuilder` API is used to build a definition for a JAX-RS server you want to run. When you call `build()` on it
you get a `Server` instance that can be used to start/stop the server as desired.  At a bare minimum, you **MUST**
configure the Application class, Display name and Port number for the server e.g.

```java
Server server 
  = ServerBuilder.create()
        .application(YourApplication.class)
        .displayName("Example Application")
        .port(10001)
        .build();
```

Would create a server that runs `YourApplication` on `localhost:10001` with a display name of `Example Application`, the
display name is primarily used for logging though also necessary for the application context used to deploy the JAX-RS
application to the server.

By default, your application will be hosted as the root context of the server, if you want to host it under a different
context then you can do so via the `contextPath()` method.  For example `.contextPath("/api")` would host your
application under `/api` on the server.  Note that for inspection purposes the `getBaseUri()` method on the built
`Server` instance will give you the Base URI against which you can formulate your requests including the context path
(if any), this is particularly useful for unit and integration testing.

The hostname for the server is configured via the `allInterfaces()`, `hostname("some-host")` or `localhost()` methods,
`some-host` can be a hostname or an IP address.  So you could use `.hostname("0.0.0.0")` to bind the server to all
network interfaces on your host (this is effectively what `allInterfaces()` does).

The `withAutoConfigInitialisation()` method registers our
[`ServiceLoadedServletContextInitialiser`](../../jaxrs-base-server/src/main/java/io/telicent/smart/cache/server/jaxrs/init/ServiceLoadedServletContextInitialiser.java)
context listener, this automatically detects, loads and runs initialisers that conform to our
[`ServiceConfigInit`](#serverconfiginit) interface.  This module already registers listeners for [JWT
Authentication](#jwt-authentication), [User Info Lookup](#user-info-lookup) and [Server Runtime
Info](#server-runtime-info).

**NB** If the `withAutoConfigInitialisation()` method is not called then many of the features described here will not be
appropriately configured and requests **MAY** be rejected as a result.

Alternatively if you have existing listeners that do not conform to our interface, but implement the base
`ServletContextListener` interface then you can use these as well via `withListener(YourListener.class)`.

The `withVersionInfo()` method ensures that a libraries [version
information](../observability/index.md#detecting-version-information) is exposed via the applications `/version-info`
endpoint.  If this is not called then only libraries which are creating Open Telemetry meters via
[`TelicentMetrics.getMeter()`](../observability/index.md#obtaining-an-open-telemetry-meter) will be included, and only
if they have done so at the point where a user retrieves `/version-info`.  Calling this method explicitly on your 
`ServerBuilder` ensures that the libraries of relevance to your server application report their version information.

## `Server`

Once you have built a `Server` instance via [`ServerBuilder`](#serverbuilder) you can then start and stop the server via
its `start()` and `stop()` methods, generally you should only start and stop a server once.  If you want to terminate a
server immediately you can do so via the `shutdownNow()` method.

A `Server` is an `AutoCloseable` so for unit or integration tests you can use it with a `try-with-resources` block e.g.

```java
try (Server server = ServerBuilder.create()
                                  .port(1234)
                                  .applicationClass(MyApp.class)
                                  .displayName("Test")
                                  .build()) {
    server.start();

    // Test some server behaviour...

    server.shutdownNow();
}
```

Then if anything goes wrong while the test is running you are guaranteed to clean it up.

## `ServerConfigInit`

The
[`ServerConfigInit`](../../jaxrs-base-server/src/main/java/io/telicent/smart/cache/server/jaxrs/init/ServerConfigInit.java)
interface is a small extension to the `ServletContextListener` interface that adds a name and priority to the interface.
The intent of this is to allow listeners to be automatically discovered (via `ServiceLoader`) and applied in a
well-defined order without having to manually define the listeners.  By using a `ServiceLoader` driven mechanism we need
only define a single explicit listener and provided suitable `META-INF/services` files are present on the Classpath all
our listeners will be discovered and loaded.

When you use a [`ServerBuilder`](#serverbuilder) you can ensure this listener is registered via the
`withAutoConfigInitialisation()` method.  As already noted if this isn't done then some of the other features described
here will not function correctly.

The `getName()` value is used for logging purposes so should be something meaningful to a developer or system
administrator reviewing the logs.

The `priority()` value of these instances allows the initialisation order to be automatically controlled.  A higher
value is considered higher priority and will be initialised first.  When the server is stopped the initialisers are
destroyed in reverse priority order i.e. lowest priority will be destroyed first.

## JWT Authentication

The base server module includes a servlet context listener that configures JWT Authentication using the support from our
[jwt-servlet-auth][2] module.  This listener looks for an environment variable named `JWKS_URL` and uses its value to
configure a suitable [`JwtVerifier`][3] for your application.  This will also configure an authentication engine that
sends challenge responses using RFC 7807 Problem Responses for alignment with the [error handling](#error-handling)
provided by this module.

The `JWKS_URL` environment variable is used to configure the location of the public keys at server startup.  This may
either be a remote URL or a local file, for a local file the URL must be of the form `file:///path/to/jwks.json`.
Alternatively it may be one of the following special values:

- `disabled` - Disables authentication entirely.  This just means that no verifier will be registered, if your
  application does not also disable authentication in its application class definition the relevant request filter will
  still be installed and result in requests being rejected as unauthenticated.
- `aws:<region>` - Enables AWS authentication where `<region>` is the AWS region in which the server is being deployed.
  This will configure authentication to use the custom AWS headers and resolve public keys from AWS ELB.

Since `0.16.0` the following additional environment variables may be used for further configuration:

| Environment Variable | Example Value | Notes |
|----------------------|---------------|-------|
| `JWT_HEADERS_NAMES`  | `Authorization,X-Custom` | Specifies a comma separated list of HTTP Header names that will be searched to find a suitable JWT |
| `JWT_HEADERS_PREFIXES` | `Bearer,` | Specifies a comma separated list of prefixes that are present in the HTTP Headers and will be stripped as part of extracting the token |
| `JWT_USERNAME_CLAIMS` | `preferred_name,email,username` | Specifies a comma separated list of claim names that will be used in extracting the username of the authenticated user from the verified JWT |

In the example values shown in this table we allow the token to be presented in either the `Authorization` or the
`X-Custom` header.  For the `Authorization` header it is expected to have a prefix of `Bearer` present, e.g. `Bearer
<jwt>`, while the `X-Custom` header is expected to have no prefix and just contain the token, e.g. `<jwt>`.

Assuming that we successfully verify a JWT then we will search first the `preferred_name`, then the `email`, and then
the `username` claim to find the username for the user.  Note that regardless of the claims configured here if none are
present the authentication library falls back to using the JWT standard `sub` (subject) claim to detect a user identity.

### Advanced Configuration

Our JWT authentication support is based upon our [`jwt-servlet-auth`][2] library, we support most of the [upstream
configuration][6] except that the configuration keys **MUST** be converted into environment variable style, e.g.
`jwt.roles.claim` would become `JWT_ROLES_CLAIM`.

For some of the configuration we provide different default configuration than the upstream library based on suitable
defaults for our deployment environments.

There are a couple of notable differences:

1. We don't support the `jwt.jwks.url`/`jwt.aws.region` variable, instead we support a single `JWKS_URL` environment
   variable that may either take a URL, or the special value `aws:<region>` to enable AWS verification.
2. Note that we intentionally disable the `jwt.path-exclusions` configuration key, preferring application developers to
   explicitly add [exclusions](#excluding-from-authentication) in their server builder code, making exclusions a
   hard-coded decision made by developers.

### Excluding from Authentication

You can opt to exclude some paths from authentication, e.g. you might want to exclude your `/healthz` endpoint so
automated health checks don't need authentication.  This can only be done when you define your [server](#serverbuilder)
by using the `withAuthExclusion()` method e.g. `withAuthExclusion("/healthz")`.

Exclusions **MUST** be minimised, and certain wildcard based exclusions, e.g. `/*`, are expressly forbidden and will
prevent server startup.  The server will issue `WARN` level log messages when excluded endpoints are accessed, this is
useful to spot endpoints that should be protected but are not.  Note that the server uses a cache to track which
endpoints it has issued warnings for so that it only issues these periodically to avoid flooding the server logs if a
frequently accessed endpoint, e.g. `/healthz`, is excluded.

## User Info Lookup

Since 0.30.0 when [Authentication](#jwt-authentication) is enabled then we also automatically enable our User Info
Lookup feature.  This feature exchanges the authenticated users JWT for a User Info response from the OIDC compliant
authentication server that contains additional details about the user that may be used in making subsequent
[Authorization](#authorization) decisions.

In order to configure this feature you must specify the `USERINFO_URL` environment variable with a suitable OAuth2/OIDC
compliant user info endpoint.  This endpoint will be presented with the users JWT in the `Authorization` header and is
expected to return a JSON response e.g.

```json
{
  "sub": "external-idp:e1336ca8-69b2-4ad6-96d1-260a67968b5e",
  "permissions": [
    "api.knowledge.read",
    "api.knowledge.write",
    "notifications.read",
    "notifications.write",
    "preferences.read",
    "preferences.write",
    "api.ontology.read",
    "api.ontology.write",
    "api.catalog.read",
    "api.catalog.write",
    "client.read",
    "client.write",
    "attributes.write",
    "attributes.read",
    "groups.read",
    "groups.write",
    "backup.read",
    "backup.write",
    "validation.read",
    "permissions.read",
    "permissions.write",
    "roles.read",
    "roles.write",
    "users.read",
    "users.write"
  ],
  "roles": [
    "USER",
    "SUPER_USER"
  ],
  "attributes": {
    "fullName": "Test User 1",
    "lastName": "User 1",
    "firstName": "Test"
  },
  "preferred_name": "A067188"
}
```

The main keys of interest here are the `roles` and `permissions`, which are used in [Authorization](#authorization)
policy enforcement on endpoints, and the `attributes` which are used for [ABAC](#user-attribute-abac) enforcement on
data.

The resulting `UserInfo` is placed into a request attribute using the full canonical class name of the `UserInfo` class
as the key.  This allows it to be retrieved in other filters and resource methods if needed e.g.

```java
// Where request is a ContainerRequestContext instance
UserInfo userInfo = (UserInfo) request.getProperty(UserInfo.class.getCanonicalName());
```

## Authorization

Since 0.30.0 when [Authentication](#jwt-authentication) is enabled then we also automatically enable our Roles and
Permissions Based Endpoint Authorization feature.  This feature only has an effect if the JAX-RS resource classes and
methods in your application are annotated with any of the following annotations:

| Annotation           | Policy      | Description                                                                            |
|----------------------|-------------|----------------------------------------------------------------------------------------|
| `@DenyAll`            | Roles       | Denies access to endpoint(s) to all users                                              |
| `@RolesAllowed`       | Roles       | Requires that users have **at least one listed role** in order to access endpoint(s)   |
| `@PermitAll`          | Roles       | Permits access to endpoint(s) to all users                                             |
| `@RequirePermissions` | Permissions | Requires that users have **all the listed permissions** in order to access endpoint(s) |

The Roles annotations are the standard Jakarta annotations from the `jakarta.annotation.security` package that you may
already be familiar with. While the permissions annotation is a custom Telicent annotation from the package
`io.telicent.smart.caches.configuration.auth.annotations`.

If no such annotations are present on a resource then authorization does not apply to the resource and the request
continues as normal.

These annotations may be specified either at the class/method level.  Where both are specified the annotation at the
method level takes precedence.  For class level annotations these may be obtained from parent classes in your class
hierarchy, so if you have a base resource class for your application you can define your default authorization policy at
that class level, and then override it in derived classes and concrete resource methods as needed.

Note that for the 3 roles annotations, when multiple annotations are present at the same level, i.e. method/class, then
the strictest one takes precedence, consider the following example:

```java
@Path("/")
@RolesAllowed({"USER", "ADMIN"})
public class ExampleProtectedResource {

  @GET
  @Path("{key}")
  public Response get(@PathParam("key") @NotBlank String key) {
    // Get the value somehow...
    return Response.ok().entity(value).build();
  }

  @DELETE
  @Path("{key}")
  @RolesAllowed({"ADMIN"})
  public Response delete(@PathParam("key") @NotBlank String key) {
    // Delete the value
    return Response.status(Response.Status.NO_CONTENT).build();
  }

  @DELETE
  @Path("_reset")
  @RolesAllowed({"ADMIN"})
  @RequirePermissions({"admin:write", "admin:reset"})
  public Response reset() {
    // Reset your system
    return Response.status(Response.Status.NO_CONTENT).build();
  }

  @GET
  @Path("_bad")
  // If multiple roles annotations are present then the strictest applies
  @RolesAllowed("{USER}")
  @DenyAll
  public Response misconfigured() {
    response Response.ok().build();
  }
}
```

Here the effective authorization policy is as follows:

| Endpoint  | HTTP Method | Roles Based Policy | Permissions Based Policy        |
|-----------|-------------|--------------------|---------------------------------|
| `/{key}`  | `GET`       | `USER` or `ADMIN`  | None                            |
| `/{key}`  | `DELETE`    | `ADMIN`            | None                            |
| `/_reset` | `DELETE`    | `ADMIN`            | `admin:write` and `admin:reset` |
| `/_bad`   | `GET`       | Deny All           | None                            |

So we can see that the `get()` method inherited its policy from the resource class annotations.  The `delete()` method
specified its own role annotations at the method level so that took precedence.  The `reset()` method specified both
roles and permissions annotations so it has two authorization policies in place.  In this case both policies **MUST** be
satisfied in order for a request to be authorized.

Finally the `misconfigured()` method specified its own role annotations at the method level, but since it specified
multiple annotations the strictest one - `@DenyAll` - took precedence.

In order for roles based authorization policy to successfully apply your application **MUST** be configured to extract
roles information from the users JWT.  This is done by setting the `JWT_ROLES_CLAIM` environment variable to the path to
the roles claim e.g. `roles`, or `path.to.roles`.   If the configuration value contains `.` characters then this is
considered to represent a path to a nested claim within the JWT, otherwise it is considered to be a top level claim.  If
not configured then this defaults to the `roles` claim.

**IMPORTANT**: If the users JWT does not contain roles information and a resource class/method has an `@RolesAllowed`
annotation then they will not be permitted to access the protected endpoint(s) and will receive a 401 error.

In order for permissions based authorization policy to successfully apply your application **MUST** be configured for
[User Info Lookup](#user-info-lookup) and the retrieved User Info is expected to contain a `permissions` key with a list
of permissions that the user holds.

**IMPORTANT**: If the user info does not contain permissions information and a resource class/method has an
`@RequirePermissions` annotation then they will not be permitted to access the protected endpoint(s) and will receive a
401 error.

### Authorization Logging

Regardless of whether an authorization is successful or not the server will log that information, including the reasons
why a given request was/wasn't authorized.  This allows system administrators to debug why a given request was/wasn't
authorized.

For example here are some rejected requests:

```
2025-09-16 09:59:19,457 [ccadf3c6-505b-4b67-8b92-50ad7a4fb57f] [test] WARN  TelicentAuthorizationFilter - DELETE Request to http://localhost:22580/data/actions/forbidden rejected: denied to all users
2025-09-16 09:59:19,612 [1faf935f-7afc-4bd6-8cd5-f6ddab8400d2] [test] WARN  TelicentAuthorizationFilter - DELETE Request to http://localhost:22583/data/test rejected: requires roles that your user account does not hold
2025-09-16 09:59:19,650 [0d449ab1-87eb-460b-a816-4d372b253cd1] [test] WARN  TelicentAuthorizationFilter - DELETE Request to http://localhost:22584/data/actions/permissions rejected: requires permissions that your user account does not hold
```

Conversely here are some successfully requests:

```
2025-09-16 09:59:19,686 [68753f40-7710-4046-9fb4-a5a8f8331894] [test] INFO  TelicentAuthorizationFilter - GET Request to http://localhost:22585/data/actions/anyone successfully authorized: all users permitted, no permissions required
2025-09-16 09:59:19,761 [87d622fc-c3a5-48a1-a995-08aeb0590653] [test] INFO  TelicentAuthorizationFilter - DELETE Request to http://localhost:22587/data/test successfully authorized: user holds roles (ADMIN), no permissions required
```

When requests are unauthorized a 401 Unauthorized response is sent back to the client with general information about why
the request was rejected.  For example it might say "requires roles your user account does not hold", but it will not
divulge which specific roles/permissions were required for a given request as naming of roles/permissions may be
considered sensitive by the system administrator.

In more recent releases the Authorization logging was updated to include more specific failure reasons.  So while the
client receives only general information about why a request was unauthorized the server logs will detail the specific
missing roles/permissions as applicable.

### Authorization for Bad Requests

Authorization does not necessarily apply in all cases, in particular if an endpoint is requested that does not exist
(i.e. a 404 error) then that error is still returned as normal.  Similarly if an endpoint is requested using the wrong
HTTP method (i.e. a 405 Method Not Allowed error) then that error would again be returned as normal.

Assuming authorization is successful then other request validation still happens as normal, e.g. if you have validation
annotations on your resource method parameters those will apply after validation and can still result in a 400 Bad
Request if appropriate.

### Exclusion from Authorization

There are two ways to exclude a resource from authorization:

1. If it is already excluded from [authentication](#excluding-from-authentication)
2. By adding a `@PermitAll` and/or a `@RequirePermissions({})` annotation

For the former the server will issue `WARN` level messages periodically to remind you that certain paths are excluded
from authorization.  This is useful to help spot endpoints that should be protected but are not.

The latter exclusion approach is only needed if you have a resource method in a class that defines roles/permissions
based authorization policy at the class/parent class levels which you need to override, e.g. there's one endpoint in a
resource class that should be unprotected despite the class level annotations.

## Data Security

From 1.0.0 onwards support for data security is provided by integration with our [Data Security Plugin
API](../data-security/index.md).  Assuming you have derived from the [`AbstractApplication`](#creating-an-application)
base class then the relevant `DataSecurityPluginContextFilter` is automatically added to the JAX-RS request filters.
This filter will populate a request attribute with an instance of the data security plugin APIs `RequestContext` class
containing the authenticated user information.  This can then be retrieved in your resource classes and used to
construct an `DataAccessAuthorizer` as needed to make data access authorization decisions e.g.

```java
public Response someOperation(@QueryParam("example") String example) {
  SecurityPlugin plugin = SecurityPluginLoader.load();
  RequestContext context = (RequestContext) request.getProperty(DataSecurityPluginContextFilter.ATTRIBUTE);

  try (DataAccessAuthorizer authorizer = plugin.prepareAuthorizer(context)) {
    SecurityLabelsParser parser = plugin.labelsParser();

    List<SomeResult> results = runQuery();
    for (int i = 0; i < results.size(); i++) {
      byte[] rawLabels = results.get(i).getSecurityLabels();
      try {
        SecurityLabels<?> labels = parser.parseSecurityLabels(rawLabels);
        if (!authorizer.canRead(labels)) {
          results.removeAt(i);
          i--;
        }
      } catch (MalformedLabelsException e) {
        results.removeAt(i);
        i--;
      }
    }
    return Response.ok().entity(results).build();
  }
}
```
For more complex applications most likely you would pass the `DataAccessAuthorizer` down into your underlying APIs to
allow them to make appropriate data access decisions.

## Error Handling

The module provides a number of `ExceptionMapper` instances that are automatically registered when you derive from
`AbstractApplication` per [Creating an Application](#creating-an-application).  These handle the following errors,
turning them into [RFC 7807 Problem][1] responses:

- Constraint Violations i.e. when a request parameter does not pass Bean validation rules declared on its method
  parameters
- Method Not Allowed i.e. when a user attempts to use an HTTP Verb on a request path that does not accept that verb.
  For example if they attempted a `DELETE` on a request that only permits `GET`.
- Not Found i.e. 404 errors when a user provides a bad URI, or the URI does not identify a resource that exists.
- Parameter Conversion Exceptions i.e. when a user provides a value to a request parameter that cannot be converted into
  the relevant type.

Additionally, it also installs a generic fallback mapper that will turn any other errors into problem responses.  This
ensures consistent error handling behaviour of our application servers.

Problem responses are based upon [RFC 7807][1] and by default are a JSON structure and will be serialized as such if an
endpoint method that produces one as a response declares `application/json` or `application/problem+json` in its
`@Produces` annotation.  From 0.25.0 onwards we also support serializing a subset of the problem into `text/plain`
responses.  If an endpoint method does not declare one of the supported media types, or supports other media types, then
your application may need to provide its own additional `MessageBodyWriter<Problem>` for those media types.

Note that there are some edge cases where errors occur before JAX-RS request handling has taken over from normal Servlet
request handling.  From `0.33.0` onwards we install a custom error handler for these errors as well which ensures that
they are clearly visible in the server logs.  For these errors we are not able to control the `Content-Type` of the
response and such error responses will have a `text/html` body summarising the error.  In the event of encountering one
of these error responses developers should refer to the server logs to understand what request triggered the error.

## Other Utilities

There are a few other minor utilities provided by this library.

### CORS

When [building your server](#serverbuilder) you can use the `withCors()` method to provide/manipulate a
`CorsConfigurationBuilder` object that allows you to set up the necessary CORS configuration for your application.  By
default, CORS is enabled for all servers unless `withoutCors()` is explicitly called on the `ServerBuilder`.

The default CORS configuration allows all standard HTTP methods, the `Accept`, `Authorization`, `Content-Disposition`,
`Content-Type` and `Request-ID` headers to be included in pre-flight requests and the `Request-ID` header to be obtained
from pre-flight responses.

**NB:** Remember that a number of HTTP Headers are implicitly permitted by the [CORS Request safe-list][4] and the [CORS
Response safe-list][5].

If your application requires custom CORS settings then use the `withCors()` method to configure CORS as desired e.g.

```java
Server server
  = ServerBuilder.create()
                 .localhost()
                 .port(12345)
                 .application(ExampleApplication.class)
                 .withAutoConfigInitialisation()
                 .withCors(c -> c.addAllowedHeaders("X-Custom")
                                 .addExposedHeaders("X-Custom")
                                 .addAllowedOrigins("https://your-domain.com")
                                 .preflightMaxAge(30))
                 .displayName("Custom CORS API")
```

### Request IDs

The `RequestIdFilter` is automatically registered when you derive from `AbstractApplication` per [Creating an
Application](#creating-an-application).  It adds a unique `Request-ID` header containing a UUID to each incoming request
and copies this header to the outgoing response as well.  The Request ID is also copied into the logging `MDC` allowing
logging configuration to include it as part of the log format, this then allows the logs to be correlated with
individual requests.  This is particularly useful when many users are making concurrent requests to the server and the
logging for those requests may be arbitrarily interleaved.

#### Client Provided Request IDs

If a client provides a `Request-ID` header in their original request then this is used as the prefix for creating a
unique Request ID by appending an incrementing number (seeded from the server startup time) to the client provided ID.
Thus resulting in Request IDs like `test/1677839349485`, `test/1677839349486` etc.

This feature is useful if clients need to make multiple requests to perform some logical operation (from the client's
viewpoint) and wants to later correlate all those requests within the logs.  Obviously a client needs to be careful that
their supplied Request ID is sufficiently unique to start with, e.g. by generating its own UUIDs. 

Note that in order to protect the server from overly long Request IDs the server imposes a maximum length of 36
characters on client provided IDs.  This corresponds to the length of a standard UUID encoded as a string e.g.
`d34b6a52-0511-42d8-9211-819bca4db626`.  If the client provided Request ID is longer than this is will be truncated to
36 characters and then the server provided unique suffix appended as shown above.

### Result Paging

The `Paging` static class provides an `applyPaging()` method that can be used to apply limit and offset based paging to
any list of results.

Note that generally it will be better to implement paging directly in your underlying APIs wherever possible, this is
merely a stop-gap measure for the scenario where such capabilities are not supported by an API.

### `/healthz` endpoint

The `AbstractHealthResource` provides a JAX-RS resource class for implementing the `/healthz` health endpoint in your
application.  You need to derive from this class and override the `determineStatus()` method to provide a `HealthStatus`
object describing the health of your server.  

From `0.22.0` onwards you can provide your class for this via overriding the abstract `getHealthResourceClass()` method
in your application class.  Note that if you prefer not to use our `AbstractHealthResource` as a base then you should
return `null` from that method, and just register your own health resource class in your applications
[`getClasses()`](#creating-an-application) method.

### `/version-info` endpoint

The `VersionInfoResource` provides a `/version-info` endpoint to your server that exposes version information.  You can
ensure specific libraries information is exposed there by using the `withVersionInfo("library-name")` method on the
[`ServerBuilder`](#serverbuilder).  See the [Detecting Version
Information](../observability/index.md#detecting-version-information) documentation for details on how the version
information is detected.

# Dependency

This API is provided by the `jaxrs-base-server` module which can be depended on from Maven like so:

```xml
<dependency>
    <groupId>io.telicent.smart-caches</groupId>
    <artifactId>jaxrs-base-server</artifactId>
    <version>VERSION</version>
</dependency>
```

Where `VERSION` is the desired version, see the top level [README](../../README.md) in this repository for that
information.

# Testing

The `tests` classifier of this module includes some useful mock servers that can be used in setting up testing of
servers built with this framework.

## Mock Key Server

For testing that services properly enable authentication and can verify user identity there is a `MockKeyServer` that
can be instantiated and started like so:

```java
MockKeyServer keyServer = new MockKeyServer(12345);
keyServer.start();

// Pick one of the available Key IDs
String keyId = keyServer.getKeyIdsAsList().get(0);

// Create a JWT signed with a key provided by the key server
String jwt = Jwts.builder()
                 .header()
                 .keyId(keyId)
                 .and()
                 .subject("user@example.org")
                 .expiration(Date.from(Instant.now().plus(5, ChronoUnit.MINUTES)))
                 .signWith(keyServer.getPrivateKey(keyId))
                 .compact();

// Create a verifier that can verify keys from the key server
JwtVerifier verifier 
  = new SignedJwtVerifier(new CachedJwksKeyLocator(URI.create(keyServer.getJwksUrl()), 
                                                   Duration.ofMinutes(15)));

// Verify the signed JWT
Jws<Claims> jws = verifier.verify(jwt);
```

It provides a `getJwksUrl()` method that provides a JWKS URL that can be used to configure the JWT Authentication
Filter, see [JWT Authentication](#jwt-authentication) for configuration details.

If you want to mock an AWS deployment you can call `registerAsAwsRegion("test")` and then configure the filter with the
`test` region to have it resolve keys AWS style from the key server.  You should remember to call
`AwsElbKeyUrlRegistry.reset()` after your tests to remove the custom configuration.

From 0.30.0 onwards it also contains a mock `/userinfo` endpoint that can be used for [User Info
Lookup](#user-info-lookup).  This endpoint just generates a User Info response from the incoming JWT, so provided you
create a JWT signed by one of the `MockKeyServer`'s keys you can get back whatever User Info response you need for the
purposes of your tests e.g.

```java
String keyId = this.keyServer.getKeyIdsAsList().get(0);
        Key key = this.keyServer.getPrivateKey(keyId);
        return Jwts.builder()
                   .header()
                   .keyId(keyId)
                   .and()
                   .subject(username)
                   .expiration(Date.from(Instant.now().plus(1, ChronoUnit.MINUTES)))
                   .signWith(key)
                   .claims()
                   .add(Map.of(
                     "roles", List.of("USER", "SUPER_USER"), 
                     "permissions", List.of("api:read", "api:write"),
                     "attributes", Map.of(
                       "clearance", "TS",
                       "nationality", "GBR"
                     )))
                   .and()
                   .compact();
```

If this token were submitted using a `GET` request to the mock key servers `getUserInfoUrl()` via a `Authorization:
Bearer <jwt>` header then it would echo back a User Info response populated with the `roles`, `permissions` and
`attributes` you specified.  See `UserInfoResource` in the `tests` classifier for more details on this.

Finally you should always call `stop()` in your test class teardown to stop the mock key server.

[1]: https://datatracker.ietf.org/doc/html/rfc7807
[2]: https://github.com/Telicent-io/jwt-servlet-auth
[3]: https://github.com/Telicent-io/jwt-servlet-auth#verifiers
[4]: https://developer.mozilla.org/en-US/docs/Glossary/CORS-safelisted_request_header
[5]: https://developer.mozilla.org/en-US/docs/Glossary/CORS-safelisted_response_header
[6]: https://github.com/telicent-oss/jwt-servlet-auth?tab=readme-ov-file#configuration-reference