# Security Features in JAX-RS Base Server

The `jaxrs-base-server` module provides a number of features related to authenticating and authorizing requests:

- [JWT Authentication](#jwt-authentication)
- [User Info Lookup](#user-info-lookup)
- [Authorization](#authorization)
- [Data Security](#data-security)

## JWT Authentication

The base server module includes a servlet context listener that configures JWT Authentication using the support from our
[jwt-servlet-auth][2] module.  This listener looks for an environment variable named `JWKS_URL` and uses its value to
configure a suitable [`JwtVerifier`][3] for your application.  This will also configure an authentication engine that
sends challenge responses using [RFC 7807 Problem Responses][1] for alignment with the [error
handling](index.md#error-handling) provided by this module.

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

### Advanced JWT Configuration

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
automated health checks don't need authentication.  This can only be done when you define your
[server](index.md#serverbuilder) by using the `withAuthExclusion()` method e.g. `withAuthExclusion("/healthz")`.

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
policy enforcement on endpoints, and the `attributes` which are used for [Data Security](#data-security) enforcement on
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
For more complex applications most likely you would pass either the `RequestContext`, or the `DataAccessAuthorizer` down
into your underlying APIs to allow them to make appropriate data access decisions.

[1]: https://datatracker.ietf.org/doc/html/rfc7807