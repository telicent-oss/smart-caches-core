# Rate Limits in JAX-RS Base Server

In `1.10.0` the `jaxrs-base-server` module added a new rate limiting feature that allows rate limits to be easily
applied onto JAX-RS applications via a new `@RateLimit` annotation.  This annotation may be applied to either resource
classes or resource methods.  Our `RateLimitFilter` that is automatically registered by our
[`AbstractApplication`](index.md#creating-an-application) discovers these annotations at the resource method, resource
class and parent class levels and applies all of them independently so a request can be subject to multiple rate limits.
If any rate limit is exceeded then an [error](#customising-the-error-response) is produced.

# Minimal Rate Limit

The following is a minimal `@RateLimit` annotation:

```java
@RateLimit(
  name = "data-access", 
  window = 1000, 
  requestsPerWindow = 10
)
```

The above example shows only the mandatory fields for the annotation.  Firstly the `name` field provides a unique name
for a rate limit which is used internally for tracking the Resilience4j `RateLimiter` instances used to enforce rate
limits.

The `window` and `requestsPerWindow` fields define the actual rate limit.  The `window` represents the period of time
(in milliseconds) over which a rate limit is enforced while `requestsPerWindow` define the maximum number of requests
permitted within that window.  So the above example allows 10 requests/1 second.

## Unique Names

> **IMPORTANT:** The name **MUST** be unique within an application. You **MAY** have many different rate limits defined
**BUT** each one **MUST** be uniquely named.

Additionally you **MUST** ensure that no rate limit name is a prefix of another rate limit name.  This is because when
[per-user](#per-user-rate-limits) limits are used the name of the limit is used as a prefix for naming the `RateLimiter`
instance for that limit and user combination.  Therefore, a user with a sufficiently generic username could
inadvertently have a different limit applied to them if combining their username with the limit name resulted in a
limiter name that matched another limit.

## Fixed Rate Limit Window

Rate limits are calculated using a fixed window.  Therefore, if you specify a long window it's possible for a user to
exceed that limit early on, and then have to wait a long time before their requests are permitted again.  As a result
developers should consider carefully their choices for these values.

Too short a window potentially allows a malicious user/attacker to submit just enough requests to keep a service busy
without tripping the rate limit.  On the other hand a long window may restrict legitimate users if the
`requestsPerWindow` is not appropriately tuned for the desired/expected average request frequency.

Developers also need to consider whether they want rate limits to be enforced globally (the default) or
[per-user](#per-user-rate-limits).

## Blocking Requests

By default rate limits apply immediately, either you are within the rate limit and your request proceeds, or you exceed
the rate limit and your request is rejected.  The optional `waitTime` field of the `@RateLimit` annotation allows you to
specify a wait time for the rate limit in milliseconds.  When this is configured with a non-zero value then requests
that would exceed the rate limit will block for up to the `waitTime` to see if they can proceed.  This allows
temporarily delaying the request which may potentially then proceed if the window rolls over to the next period e.g.

```java
@RateLimit(
  name = "data-access", 
  window = 1000, 
  requestsPerWindow = 10,
  waitTime = 100
)
```

In the above example we set a `waitTime` of 100 milliseconds, therefore if the rate limit was exceeded near the end of
the 1 second window it's possible it could succeed after waiting.

>  This **MUST** be used with caution, a waiting request is blocking a request thread on the server.
>
> If you expect your application may come under denial of service (DoS) attacks then it is better to leave this at the
default of `0` and reject requests as soon as rate limits are exceeded.  Otherwise a malicious user can tie up all
request threads waiting for their request to be allowed to proceed.

## Customising the Error Response

When a rate limit is exceeded then the `RateLimitFilter` aborts offending requests with a `429 Too Many Requests`
response with a generic [RFC 7807 Problem][1] response.

The optional `errorTitle` and `errorDetail` fields allow customising the error message that is returned to users to
provide more specific detail about what rate limit they exceeded.  If these are not explicitly specified on your
`@RateLimit` annotation then generic defaults are used.

> **NB** Avoid mentioning specific parameters of the rate limit in the error title/detail.  As we provide [configuration
> override](#overriding-rate-limits) functionality the actual limit that applies at runtime **MAY** be different than
> what you configure in your annotation.  Please try to keep errors generic, e.g. "Too many write requests".

## Per-User Rate Limits

By default rate limits are applied globally, this means that all requests from all users contribute to the rate limit
calculation.  For example given the earlier example if one user makes 10 requests no other user can make any requests
until the `window` has elapsed.

To make rate limits be enforced per-user instead add the `perUser` field to your `@RateLimit` annotation e.g.

```java
@RateLimit(
  name = "data-access", 
  window = 1000, 
  requestsPerWindow = 10,
  perUser = true
)
```

This limit is now 10 requests per 1 second per user i.e. each unique user can make up to 10 requests per 1 second.

## Rate Limit Configuration

In addition to the `RateLimitFilter` we also provide a `RateLimitInit` that is automatically discovered when using
[`withAutoConfigInitialisation()`](config.md#serverconfiginit) on your [`ServerBuilder`](index.md#serverbuilder).  This
configures certain aspects of the rate limiting implementation, primarily it configures several internal caches that are
used to track the rate limiting machinery.  Their configuration can be controlled via the following configuration
variables:

| Variable                          | Default | Purpose |
|-----------------------------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `DISABLE_RATE_LIMITS`             | `false` | Set to `true` to explicitly disable rate limit enforcement.                                                                                                                                                       |
| `RATE_LIMIT_CACHE_SIZE`           | `1000`  | Sets the cache size for tracking rate limiters.  This should be sized based on expected number of active users.                                                                                                   |
| `RATE_LIMIT_CACHE_DURATION`       | `PT5M`  | Sets the cache duration for rate limiters.  This should be higher than the maximum rate limit window used otherwise users can exceed rate limits because their rate limiters will be forgotten during the window. |
| `RATE_LIMIT_DISCOVERY_CACHE_SIZE` | `100`   | Sets the cache size for Resource to Rate Limits mappings that will be cached.  This should be sized based on the number of unique resource class and method combinations in your application.                     |
| `RATE_LIMIT_CONFIG_CACHE_SIZE`    | `10`    | Sets the cache size for mapping Rate Limits to rate limiter configurations.  This should be sized based on the number of `@RateLimit` annotations used in your application.                                       |

### Overriding Rate Limits

As developers may not always know in advance what appropriate rate limits are, and using an annotation - `@RateLimit` -
effectively hardcodes default rate limit configuration into an application we also provide a mechanism for overriding
limits at runtime.

In order to override a rate limit you need to know its declared `name` from its `@RateLimit` annotation, you can then
set one/more of the following configuration variables.  In these examples `<name>` is replaced with the name of the
limit transformed into environment variable style, e.g. a limit named `my-limit` becomes `MY_LIMIT` when used in this
way.

- `RATE_LIMIT_OVERRIDE_<name>_WINDOW`: Overrides the window milliseconds for a limit.
- `RATE_LIMIT_OVERRIDE_<name>_REQUESTS`: Overrides the requests per window for a limit.
- `RATE_LIMIT_OVERRIDE_<name>_TIMEOUT`: Overrides the [waitTime](#blocking-requests) for a limit.

> **NB** If any of these overrides are configured with invalid values, e.g. negative window, then this is treated as a
> [misconfiguration](#misconfiguration) and a 500 Internal Server Error is generated.

### Misconfiguration

There are a couple of scenarios in which the `RateLimitFilter` may fail requests with a `500 Internal Server Error`
instead of allowing them to proceed.

Firstly, if the application **DOES NOT** configure the rate limiting feature correctly, in this case the filter does not
know whether or not rate limiting should apply and fails safe by rejecting requests.  As noted earlier if you use our
standard [`AbstractApplication`](index.md#creating-an-application) and [`ServerBuilder`](index.md#serverbuilder) then
this feature will be enabled and configured automatically.  If you use non-standard configuration then you will need to
ensure you explicitly register our `RateLimitInit` which configures the underlying `RateLimitingEngine` and/or add the
`RateLimitFilter` to your application classes.

If rate limits are misconfigured for a resource, e.g. duplicate [names](#unique-names), bad
[overrides](#overriding-rate-limits), then again the filter doesn't know what rate limits should apply.  In this case
the developer **MUST** ensure that each `@RateLimit` annotation in the application has a unique name, and the operator
should ensure any overrides supplied are valid.

### Disabling Rate Limits

There are two ways to disable rate limits:

1. Set the `DISABLE_RATE_LIMITS` configuration variable explicitly to `true`.
2. Override the `AbstractApplication.isRateLimitingEnabled()` method to return `false`

Either way this means the `RateLimitFilter` is not registered in the application classes by `AbstractApplication` and
thus no rate limits will apply.

## Metrics

When rate limiting is in-use the `RateLimitingEngine` emits various OpenTelemetry metrics that can be used to observe
rate limiting in action.

Firstly there are four counters:

- `rate.limit.not-applicable` - Count of requests for which no rate limits applied.
- `rate.limit.permitted` - Count of requests that were permitted by rate limits.
- `rate.limit.exceeded` - Count of requests that exceeded rate limits and were rejected with a `429 Too Many Requests`
  response.
- `rate.limit.invalid` - Count of requests that has [invalid](#misconfiguration) rate limits and were rejected with a
  `500 Internal Server Error` response.

Additionally we provide a gauge - `rate.limit.exceeding-users` - that tracks exceeded rate limits by user.  Metrics
emitted for this gauge have the `user` attribute set to the offending users username, or `<anonymous>` if the rate limit
was exceeded on a resource not protected by authentication.

[1]: https://datatracker.ietf.org/doc/html/rfc7807