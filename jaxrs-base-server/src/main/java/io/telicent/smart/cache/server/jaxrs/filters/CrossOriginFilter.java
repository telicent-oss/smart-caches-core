/**
 * Copied from Jetty 11.x
 * <p>
 * Modified as follows: - Package relocated - Removed one assumption of a Jetty server runtime in the handle() method -
 * Added stub Javadoc where needed
 * <p>
 * Used under Apache 2.0 License
 */

//
// ========================================================================
// Copyright (c) 1995 Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v. 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package io.telicent.smart.cache.server.jaxrs.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.riot.web.HttpNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.Strings.CS;

/**
 * Implementation of the
 * <a href="http://www.w3.org/TR/cors/">cross-origin resource sharing</a>.
 * <p>
 * A typical example is to use this filter to allow cross-domain
 * <a href="http://cometd.org">cometd</a> communication using the standard
 * long polling transport instead of the JSONP transport (that is less efficient and less reactive to failures).
 * <p>
 * This filter allows the following configuration parameters:
 * <dl>
 * <dt>allowedOrigins</dt>
 * <dd>a comma separated list of origins that are
 * allowed to access the resources. Default value is <b>*</b>, meaning all
 * origins.    Note that using wild cards can result in security problems
 * for requests identifying hosts that do not exist.
 * <p>
 * If an allowed origin contains one or more * characters (for example
 * http://*.domain.com), then "*" characters are converted to ".*", "."
 * characters are escaped to "\." and the resulting allowed origin
 * interpreted as a regular expression.
 * <p>
 * Allowed origins can therefore be more complex expressions such as
 * https?://*.domain.[a-z]{3} that matches http or https, multiple subdomains
 * and any 3 letter top-level domain (.com, .net, .org, etc.).</dd>
 *
 * <dt>allowedTimingOrigins</dt>
 * <dd>a comma separated list of origins that are
 * allowed to time the resource. Default value is the empty string, meaning
 * no origins.
 * <p>
 * The check whether the timing header is set, will be performed only if
 * the user gets general access to the resource using the <b>allowedOrigins</b>.
 *
 * <dt>allowedMethods</dt>
 * <dd>a comma separated list of HTTP methods that
 * are allowed to be used when accessing the resources. Default value is
 * <b>GET,POST,HEAD</b></dd>
 *
 *
 * <dt>allowedHeaders</dt>
 * <dd>a comma separated list of HTTP headers that
 * are allowed to be specified when accessing the resources. Default value
 * is <b>X-Requested-With,Content-Type,Accept,Origin</b>. If the value is a single "*",
 * this means that any headers will be accepted.</dd>
 *
 * <dt>preflightMaxAge</dt>
 * <dd>the number of seconds that preflight requests
 * can be cached by the client. Default value is <b>1800</b> seconds, or 30
 * minutes</dd>
 *
 * <dt>allowCredentials</dt>
 * <dd>a boolean indicating if the resource allows
 * requests with credentials. Default value is <b>true</b></dd>
 *
 * <dt>exposedHeaders</dt>
 * <dd>a comma separated list of HTTP headers that
 * are allowed to be exposed on the client. Default value is the
 * <b>empty list</b></dd>
 *
 * <dt>chainPreflight</dt>
 * <dd>if true preflight requests are chained to their
 * target resource for normal handling (as an OPTION request).  Otherwise the
 * filter will response to the preflight. Default is <b>true</b>.</dd>
 *
 * </dl>
 * A typical configuration could be:
 * <pre>
 * &lt;web-app ...&gt;
 *     ...
 *     &lt;filter&gt;
 *         &lt;filter-name&gt;cross-origin&lt;/filter-name&gt;
 *         &lt;filter-class&gt;org.eclipse.jetty.servlets.CrossOriginFilter&lt;/filter-class&gt;
 *     &lt;/filter&gt;
 *     &lt;filter-mapping&gt;
 *         &lt;filter-name&gt;cross-origin&lt;/filter-name&gt;
 *         &lt;url-pattern&gt;/cometd/*&lt;/url-pattern&gt;
 *     &lt;/filter-mapping&gt;
 *     ...
 * &lt;/web-app&gt;
 * </pre>
 */
public class CrossOriginFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(CrossOriginFilter.class);

    // Request headers
    private static final String ORIGIN_HEADER = "Origin";
    /**
     * HTTP Access-Control-Request-Method header
     */
    public static final String ACCESS_CONTROL_REQUEST_METHOD_HEADER = "Access-Control-Request-Method";
    /**
     * HTTP Access-Control-Request-Headers header
     */
    public static final String ACCESS_CONTROL_REQUEST_HEADERS_HEADER = "Access-Control-Request-Headers";
    // Response headers
    /**
     * HTTP Access-Control-Allow-Origin header
     */
    public static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
    /**
     * HTTP Access-Control-Allow-Methods header
     */
    public static final String ACCESS_CONTROL_ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";
    /**
     * HTTP Access-Control-Allow-Headers header
     */
    public static final String ACCESS_CONTROL_ALLOW_HEADERS_HEADER = "Access-Control-Allow-Headers";
    /**
     * HTTP Access-Control-Max-Age header
     */
    public static final String ACCESS_CONTROL_MAX_AGE_HEADER = "Access-Control-Max-Age";
    /**
     * HTTP Access-Control-Allow-Credentials header
     */
    public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER = "Access-Control-Allow-Credentials";
    /**
     * HTTP Access-Control-Expose-Headers header
     */
    public static final String ACCESS_CONTROL_EXPOSE_HEADERS_HEADER = "Access-Control-Expose-Headers";
    /**
     * HTTP Timing-Allow-Origin header
     */
    public static final String TIMING_ALLOW_ORIGIN_HEADER = "Timing-Allow-Origin";
    // Implementation constants
    /**
     * Filter init parameter for allowed origins
     */
    public static final String ALLOWED_ORIGINS_PARAM = "allowedOrigins";
    /**
     * Filter init parameter for allowed timing origins
     */
    public static final String ALLOWED_TIMING_ORIGINS_PARAM = "allowedTimingOrigins";
    /**
     * Filter init parameter for allowed methods
     */
    public static final String ALLOWED_METHODS_PARAM = "allowedMethods";
    /**
     * Filter init parameter for allowed headers
     */
    public static final String ALLOWED_HEADERS_PARAM = "allowedHeaders";
    /**
     * Filter init parameter for preflight max age
     */
    public static final String PREFLIGHT_MAX_AGE_PARAM = "preflightMaxAge";
    /**
     * Filter init parameter for allowed credentials
     */
    public static final String ALLOW_CREDENTIALS_PARAM = "allowCredentials";
    /**
     * Filter init parameter for exposed headers
     */
    public static final String EXPOSED_HEADERS_PARAM = "exposedHeaders";

    /**
     * Filter init parameter for forwarding of pre-flight requests
     */
    public static final String CHAIN_PREFLIGHT_PARAM = "chainPreflight";
    private static final String ANY_ORIGIN = "*";
    private static final String DEFAULT_ALLOWED_ORIGINS = "*";
    private static final String DEFAULT_ALLOWED_TIMING_ORIGINS = "";
    private static final List<String> SIMPLE_HTTP_METHODS = Arrays.asList("GET", "POST", "HEAD");
    private static final List<String> DEFAULT_ALLOWED_METHODS = Arrays.asList("GET", "POST", "HEAD");
    private static final List<String> DEFAULT_ALLOWED_HEADERS =
            Arrays.asList("X-Requested-With", "Content-Type", "Accept", "Origin");

    private boolean anyOriginAllowed;
    private boolean anyTimingOriginAllowed;
    private boolean anyHeadersAllowed;
    private final Set<String> allowedOrigins = new HashSet<>();
    private final List<Pattern> allowedOriginPatterns = new ArrayList<>();
    private final Set<String> allowedTimingOrigins = new HashSet<>();
    private final List<Pattern> allowedTimingOriginPatterns = new ArrayList<>();
    private final List<String> allowedMethods = new ArrayList<>();
    private final List<String> allowedHeaders = new ArrayList<>();
    private final List<String> exposedHeaders = new ArrayList<>();
    private int preflightMaxAge;
    private boolean allowCredentials;
    private boolean chainPreflight;

    @Override
    public void init(FilterConfig config) {
        String allowedOriginsConfig = config.getInitParameter(ALLOWED_ORIGINS_PARAM);
        String allowedTimingOriginsConfig = config.getInitParameter(ALLOWED_TIMING_ORIGINS_PARAM);

        anyOriginAllowed = generateAllowedOrigins(allowedOrigins, allowedOriginPatterns, allowedOriginsConfig,
                                                  DEFAULT_ALLOWED_ORIGINS);
        anyTimingOriginAllowed =
                generateAllowedOrigins(allowedTimingOrigins, allowedTimingOriginPatterns, allowedTimingOriginsConfig,
                                       DEFAULT_ALLOWED_TIMING_ORIGINS);

        String allowedMethodsConfig = config.getInitParameter(ALLOWED_METHODS_PARAM);
        if (allowedMethodsConfig == null) {
            allowedMethods.addAll(DEFAULT_ALLOWED_METHODS);
        } else {
            allowedMethods.addAll(Arrays.asList(StringUtils.split(allowedMethodsConfig, ",")));
        }

        String allowedHeadersConfig = config.getInitParameter(ALLOWED_HEADERS_PARAM);
        if (allowedHeadersConfig == null) {
            allowedHeaders.addAll(DEFAULT_ALLOWED_HEADERS);
        } else if ("*".equals(allowedHeadersConfig)) {
            anyHeadersAllowed = true;
        } else {
            allowedHeaders.addAll(Arrays.asList(StringUtils.split(allowedHeadersConfig, ",")));
        }

        String preflightMaxAgeConfig = config.getInitParameter(PREFLIGHT_MAX_AGE_PARAM);
        if (preflightMaxAgeConfig == null) {
            preflightMaxAgeConfig = "1800"; // Default is 30 minutes
        }
        try {
            preflightMaxAge = Integer.parseInt(preflightMaxAgeConfig);
        } catch (NumberFormatException x) {
            LOG.info("Cross-origin filter, could not parse '{}' parameter as integer: {}", PREFLIGHT_MAX_AGE_PARAM,
                     preflightMaxAgeConfig);
        }

        String allowedCredentialsConfig = config.getInitParameter(ALLOW_CREDENTIALS_PARAM);
        if (allowedCredentialsConfig == null) {
            allowedCredentialsConfig = "true";
        }
        allowCredentials = Boolean.parseBoolean(allowedCredentialsConfig);

        String exposedHeadersConfig = config.getInitParameter(EXPOSED_HEADERS_PARAM);
        if (exposedHeadersConfig == null) {
            exposedHeadersConfig = "";
        }
        exposedHeaders.addAll(Arrays.asList(StringUtils.split(exposedHeadersConfig, ",")));

        String chainPreflightConfig = config.getInitParameter(CHAIN_PREFLIGHT_PARAM);
        if (chainPreflightConfig == null) {
            chainPreflightConfig = "true";
        }
        chainPreflight = Boolean.parseBoolean(chainPreflightConfig);

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    "Cross-origin filter configuration: " + ALLOWED_ORIGINS_PARAM + " = {}, " + ALLOWED_TIMING_ORIGINS_PARAM + " = {}, " + ALLOWED_METHODS_PARAM + " = {}, " + ALLOWED_HEADERS_PARAM + " = {}, " + PREFLIGHT_MAX_AGE_PARAM + " = {}, " + ALLOW_CREDENTIALS_PARAM + " = {}," + EXPOSED_HEADERS_PARAM + " = {}," + CHAIN_PREFLIGHT_PARAM + " = {}",
                    allowedOriginsConfig, allowedTimingOriginsConfig, allowedMethodsConfig, allowedHeadersConfig,
                    preflightMaxAgeConfig, allowedCredentialsConfig, exposedHeadersConfig, chainPreflightConfig);
        }
    }

    private boolean generateAllowedOrigins(Set<String> allowedOriginStore, List<Pattern> allowedOriginPatternStore,
                                           String allowedOriginsConfig, String defaultOrigin) {
        if (allowedOriginsConfig == null) {
            allowedOriginsConfig = defaultOrigin;
        }
        String[] allowedOrigins = StringUtils.split(allowedOriginsConfig, ",");
        for (String allowedOrigin : allowedOrigins) {
            if (!allowedOrigin.isEmpty()) {
                if (ANY_ORIGIN.equals(allowedOrigin)) {
                    allowedOriginStore.clear();
                    allowedOriginPatternStore.clear();
                    return true;
                } else if (allowedOrigin.contains("*")) {
                    allowedOriginPatternStore.add(Pattern.compile(parseAllowedWildcardOriginToRegex(allowedOrigin)));
                } else {
                    allowedOriginStore.add(allowedOrigin);
                }
            }
        }
        return false;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        handle((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        response.addHeader(HttpNames.hVary, HttpNames.hOrigin);
        String origin = request.getHeader(ORIGIN_HEADER);
        // Is it a cross origin request ?
        if (origin != null && isEnabled(request)) {
            if (anyOriginAllowed || originMatches(allowedOrigins, allowedOriginPatterns, origin)) {
                if (isSimpleRequest(request)) {
                    LOG.debug("Cross-origin request to {} is a simple cross-origin request", request.getRequestURI());
                    handleSimpleResponse(request, response, origin);
                } else if (isPreflightRequest(request)) {
                    LOG.debug("Cross-origin request to {} is a preflight cross-origin request",
                              request.getRequestURI());
                    handlePreflightResponse(request, response, origin);
                    if (chainPreflight) {
                        LOG.debug("Preflight cross-origin request to {} forwarded to application",
                                  request.getRequestURI());
                    } else {
                        return;
                    }
                } else {
                    LOG.debug("Cross-origin request to {} is a non-simple cross-origin request",
                              request.getRequestURI());
                    handleSimpleResponse(request, response, origin);
                }

                if (anyTimingOriginAllowed || originMatches(allowedTimingOrigins, allowedTimingOriginPatterns,
                                                            origin)) {
                    response.setHeader(TIMING_ALLOW_ORIGIN_HEADER, origin);
                } else if (LOG.isDebugEnabled()) {
                    LOG.debug("Cross-origin request to {} with origin {} does not match allowed timing origins {}",
                              request.getRequestURI(), origin, allowedTimingOrigins);
                }
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Cross-origin request to {} with origin {} does not match allowed origins {}",
                          request.getRequestURI(), origin, allowedOrigins);
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Checks whether CORS should be enabled for a given HTTP Request
     *
     * @param request HTTP Request
     * @return True if enabled, false otherwise
     */
    protected boolean isEnabled(HttpServletRequest request) {
        // WebSocket clients such as Chrome 5 implement a version of the WebSocket
        // protocol that does not accept extra response headers on the upgrade response
        for (Enumeration<String> connections = request.getHeaders("Connection"); connections.hasMoreElements(); ) {
            String connection = connections.nextElement();
            if ("Upgrade".equalsIgnoreCase(connection)) {
                for (Enumeration<String> upgrades = request.getHeaders("Upgrade"); upgrades.hasMoreElements(); ) {
                    String upgrade = upgrades.nextElement();
                    if ("WebSocket".equalsIgnoreCase(upgrade)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean originMatches(Set<String> allowedOrigins, List<Pattern> allowedOriginPatterns, String originList) {
        if (originList.trim().isEmpty()) {
            return false;
        }

        String[] origins = originList.split(" ");
        for (String origin : origins) {
            if (origin.trim().isEmpty()) {
                continue;
            }

            if (allowedOrigins.contains(origin)) {
                return true;
            }

            for (Pattern allowedOrigin : allowedOriginPatterns) {
                if (allowedOrigin.matcher(origin).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    private String parseAllowedWildcardOriginToRegex(String allowedOrigin) {
        String regex = CS.replace(allowedOrigin, ".", "\\.");
        return CS.replace(regex, "*",
                                   ".*"); // we want to be greedy here to match multiple subdomains, thus we use .*
    }

    private boolean isSimpleRequest(HttpServletRequest request) {
        String method = request.getMethod();
        if (SIMPLE_HTTP_METHODS.contains(method)) {
            // TODO: implement better detection of simple headers
            // The specification says that for a request to be simple, custom request headers must be simple.
            // Here for simplicity I just check if there is a Access-Control-Request-Method header,
            // which is required for preflight requests
            return request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER) == null;
        }
        return false;
    }

    private boolean isPreflightRequest(HttpServletRequest request) {
        String method = request.getMethod();
        if (!"OPTIONS".equalsIgnoreCase(method)) {
            return false;
        }
        return request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER) != null;
    }

    private void handleSimpleResponse(HttpServletRequest request, HttpServletResponse response, String origin) {
        response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);
        if (allowCredentials) {
            response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        }
        if (!exposedHeaders.isEmpty()) {
            response.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS_HEADER, commify(exposedHeaders));
        }
    }

    private void handlePreflightResponse(HttpServletRequest request, HttpServletResponse response, String origin) {
        boolean methodAllowed = isMethodAllowed(request);

        if (!methodAllowed) {
            return;
        }
        List<String> headersRequested = getAccessControlRequestHeaders(request);
        boolean headersAllowed = areHeadersAllowed(headersRequested);
        if (!headersAllowed) {
            return;
        }
        response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);
        if (allowCredentials) {
            response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        }
        if (preflightMaxAge > 0) {
            response.setHeader(ACCESS_CONTROL_MAX_AGE_HEADER, String.valueOf(preflightMaxAge));
        }
        response.setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, commify(allowedMethods));
        if (anyHeadersAllowed) {
            response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, commify(headersRequested));
        } else {
            response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, commify(allowedHeaders));
        }
    }

    private boolean isMethodAllowed(HttpServletRequest request) {
        String accessControlRequestMethod = request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER);
        LOG.debug("{} is {}", ACCESS_CONTROL_REQUEST_METHOD_HEADER, accessControlRequestMethod);
        boolean result = false;
        if (accessControlRequestMethod != null) {
            result = allowedMethods.contains(accessControlRequestMethod);
        }
        LOG.debug("Method {} is{} among allowed methods {}", accessControlRequestMethod, result ? "" : " not", allowedMethods);
        return result;
    }

    private List<String> getAccessControlRequestHeaders(HttpServletRequest request) {
        String accessControlRequestHeaders = request.getHeader(ACCESS_CONTROL_REQUEST_HEADERS_HEADER);
        LOG.debug("{} is {}", ACCESS_CONTROL_REQUEST_HEADERS_HEADER, accessControlRequestHeaders);
        if (accessControlRequestHeaders == null) {
            return Collections.emptyList();
        }

        List<String> requestedHeaders = new ArrayList<>();
        String[] headers = StringUtils.split(accessControlRequestHeaders, ",");
        for (String header : headers) {
            String h = header.trim();
            if (!h.isEmpty()) {
                requestedHeaders.add(h);
            }
        }
        return requestedHeaders;
    }

    private boolean areHeadersAllowed(List<String> requestedHeaders) {
        if (anyHeadersAllowed) {
            LOG.debug("Any header is allowed");
            return true;
        }

        boolean result = true;
        for (String requestedHeader : requestedHeaders) {
            boolean headerAllowed = false;
            for (String allowedHeader : allowedHeaders) {
                if (requestedHeader.equalsIgnoreCase(allowedHeader.trim())) {
                    headerAllowed = true;
                    break;
                }
            }
            if (!headerAllowed) {
                result = false;
                break;
            }
        }
        LOG.debug("Headers [{}] are{} among allowed headers {}", requestedHeaders, result ? "" : " not", allowedHeaders);
        return result;
    }

    private String commify(List<String> strings) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strings.size(); ++i) {
            if (i > 0) {
                builder.append(",");
            }
            String string = strings.get(i);
            builder.append(string);
        }
        return builder.toString();
    }

    @Override
    public void destroy() {
        anyOriginAllowed = false;
        anyTimingOriginAllowed = false;
        allowedOrigins.clear();
        allowedOriginPatterns.clear();
        allowedTimingOrigins.clear();
        allowedTimingOriginPatterns.clear();
        allowedMethods.clear();
        allowedHeaders.clear();
        preflightMaxAge = 0;
        allowCredentials = false;
    }
}