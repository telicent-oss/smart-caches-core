/**
 * Copyright (C) Telicent Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.telicent.smart.cache.server.jaxrs.applications;

import io.telicent.servlet.auth.jwt.PathExclusion;
import io.telicent.servlet.auth.jwt.JwtServletConstants;
import io.telicent.smart.cache.observability.LibraryVersion;
import io.telicent.smart.cache.server.jaxrs.filters.CrossOriginFilter;
import io.telicent.smart.cache.server.jaxrs.init.ServiceLoadedServletContextInitialiser;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.ws.rs.core.Application;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.http.HttpServerFilter;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.util.MimeHeaders;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;

/**
 * Builder for creating server instances, use {@link #create()} as the entrypoint for building a new server e.g.
 * <pre>
 * ServerBuilder builder
 *  = ServerBuilder.create()
 *                 .port(12345)
 *                 .application(YourApplication.class)
 *                 .displayName("My Cool Application");
 * </pre>
 * <p>
 * When you are done building it call {@link #build()} to create a {@link Server} instance which you can then start and
 * stop as desired.
 * </p>
 * <h2>Defaults</h2>
 * <p>
 * Some default values are set and need not be explicitly specified, the above example did not call
 * {@link #hostname(String)} since the built server will listen on {@code localhost} by default.  Similarly the web
 * application will by default be deployed on the root context i.e. {@code /}, but you could deploy to an alternative
 * context by calling {@link #contextPath(String)} to set a desired context e.g. {@code /app/}
 * </p>
 */
public class ServerBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerBuilder.class);
    /**
     * The default root context path used for built servers unless customised via {@link #contextPath(String)}
     */
    public static final String ROOT_CONTEXT = "/";

    private String hostname = "localhost", displayName, contextPath = ROOT_CONTEXT;
    private int port = Integer.MIN_VALUE;
    private Class<? extends Application> applicationClass;
    private final List<Class<? extends ServletContextListener>> listeners = new ArrayList<>();
    private final List<PathExclusion> authExclusions = new ArrayList<>();
    private CorsConfigurationBuilder corsBuilder = new CorsConfigurationBuilder();

    private Integer maxHttpHeaderSize, maxRequestHeaders, maxResponseHeaders;

    /**
     * Creates a new builder
     *
     * @return Server builder
     */
    public static ServerBuilder create() {
        return new ServerBuilder();
    }


    /**
     * Creates a new builder
     */
    private ServerBuilder() {
    }

    /**
     * Specifies the hostname/IP address to listen on
     *
     * @param hostname Hostname/IP Address
     * @return Builder
     */
    public ServerBuilder hostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    /**
     * Specifies that the server should listen on {@code localhost}.  This is the default so need not be explicitly
     * specified.
     *
     * @return Builder
     */
    public ServerBuilder localhost() {
        return this.hostname("localhost");
    }

    /**
     * Specifies the port that the server should listen on
     *
     * @param port Port number
     * @return Builder
     */
    public ServerBuilder port(int port) {
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Not a valid port in range 1-65535");
        }
        this.port = port;
        return this;
    }

    /**
     * Specifies the JAX-RS Application class for the server
     *
     * @param appClass Application class
     * @return Server
     */
    public ServerBuilder application(Class<? extends Application> appClass) {
        this.applicationClass = appClass;
        return this;
    }

    /**
     * Specifies the display name for the web application context that will be deployed to the server
     *
     * @param name Display name
     * @return Builder
     */
    public ServerBuilder displayName(String name) {
        this.displayName = name;
        return this;
    }

    /**
     * Specifies the context path for the web application context that will be deployed to the server.  The default is
     * {@code /} i.e. the web application will be deployed as the root context.
     *
     * @param path Context path
     * @return Builder
     */
    public ServerBuilder contextPath(String path) {
        if (Objects.equals(path, ROOT_CONTEXT)) {
            // A lone / is always a valid context path and indicates that the root context is used
            // This branch of the if is needed to avoid the checks in the subsequent branches rejecting this case
        } else if (!StringUtils.startsWith(path, ROOT_CONTEXT)) {
            throw new IllegalArgumentException("Context path must start with a forward slash e.g. / or /app NOT app");
        } else if (StringUtils.endsWith(path, ROOT_CONTEXT)) {
            throw new IllegalArgumentException("Context path must not end with a forward slash e.g. /app NOT /app/");
        }
        this.contextPath = path;
        return this;
    }

    /**
     * Specifies that the context path for the web application should be the root context.  This is equivalent to
     * calling {@link #contextPath(String)} with a value of {@code /}.
     *
     * @return Builder
     */
    public ServerBuilder rootContextPath() {
        return contextPath(ROOT_CONTEXT);
    }

    /**
     * Adds servlet context listener classes that will be deployed in the web application context
     *
     * @param listenerClass Listener class
     * @return Builder
     */
    public ServerBuilder withListener(Class<? extends ServletContextListener> listenerClass) {
        this.listeners.add(listenerClass);
        return this;
    }

    /**
     * Enables automatic configuration initialisation via {@link ServiceLoadedServletContextInitialiser}
     *
     * @return Builder
     */
    public ServerBuilder withAutoConfigInitialisation() {
        return withListener(ServiceLoadedServletContextInitialiser.class);
    }

    /**
     * Specifies an authentication exclusion i.e. a path (pattern) to which authentication should not apply, this can be
     * used to configure some URLs as not requiring authentication, e.g. {@code /healthz}, even when your application as
     * a whole may have authentication configured
     *
     * @param pathPattern Path pattern
     * @return Builder
     */
    public ServerBuilder withAuthExclusion(String pathPattern) {
        return withAuthExclusion(new PathExclusion(pathPattern));
    }

    /**
     * See {@link #withAuthExclusion(String)}
     *
     * @param exclusion Path exclusion
     * @return Builder
     */
    public ServerBuilder withAuthExclusion(PathExclusion exclusion) {
        this.authExclusions.add(exclusion);
        return this;
    }

    /**
     * Specifies multiple authentication exclusions, see {@link #withAuthExclusion(String)} for more detail
     *
     * @param pathPatterns Path patterns
     * @return Builder
     */
    public ServerBuilder withAuthExclusions(String... pathPatterns) {
        return withAuthExclusions(Arrays.stream(pathPatterns).map(PathExclusion::new).toList());
    }

    /**
     * See {@link #withAuthExclusions(String...)}
     *
     * @param exclusions Path exclusions
     * @return Builder
     */
    public ServerBuilder withAuthExclusions(PathExclusion... exclusions) {
        return withAuthExclusions(Arrays.stream(exclusions).toList());
    }

    /**
     * See {@link #withAuthExclusions(String...)}
     *
     * @param exclusions Path exclusions
     * @return Builder
     */
    public ServerBuilder withAuthExclusions(List<PathExclusion> exclusions) {
        if (exclusions == null || exclusions.isEmpty()) {
            return this;
        }
        this.authExclusions.addAll(exclusions);
        return this;
    }

    /**
     * Tells the Server to attempt to make available the version information for the given library via its
     * {@code /version-info} endpoint
     *
     * @param library Library
     * @return Builder
     */
    public ServerBuilder withVersionInfo(String library) {
        LibraryVersion.getProperties(library);
        return this;
    }

    /**
     * Tells the Server to attempt to make available the version information for the given libraries via its
     * {@code /version-info} endpoint
     *
     * @param libraries Libraries
     * @return Builder
     */
    public ServerBuilder withVersionInfo(String... libraries) {
        for (String library : libraries) {
            LibraryVersion.getProperties(library);
        }
        return this;
    }

    /**
     * Configures the server to enable CORS (Cross Origin Resource Sharing) filtering
     *
     * @param builder CORS Builder
     * @return Builder
     */
    public ServerBuilder withCors(CorsConfigurationBuilder builder) {
        this.corsBuilder = builder;
        return this;
    }

    /**
     * Configures the server to enable CORS (Cross Origin Resource Sharing) filtering
     *
     * @param builderFunction Function that manipulates the CORS Builder as desired
     * @return Builder
     */
    public ServerBuilder withCors(Function<CorsConfigurationBuilder, CorsConfigurationBuilder> builderFunction) {
        if (this.corsBuilder == null) {
            this.corsBuilder = new CorsConfigurationBuilder();
        }
        this.corsBuilder = builderFunction.apply(this.corsBuilder);
        return this;
    }

    /**
     * Configures the server to not enable CORS (Cross Origin Resource Sharing) filtering
     *
     * @return Builder
     */
    public ServerBuilder withoutCors() {
        this.corsBuilder = null;
        return this;
    }

    /**
     * Sets the maximum number of HTTP Headers that are permitted on a request
     * <p>
     * This defaults to {@value MimeHeaders#MAX_NUM_HEADERS_DEFAULT}, setting it to lower values may restrict valid
     * requests.  Conversely setting it to higher values, or unlimited ({@code -1}), will make the built server
     * vulnerable to Denial of Service attacks from malicious clients so should be done with care.
     * </p>
     *
     * @param max Maximum number of HTTP Request headers, or {@code -1} for unlimited
     * @return Builder
     */
    public ServerBuilder maxHttpRequestHeaders(int max) {
        this.maxRequestHeaders = max;
        return this;
    }

    /**
     * Sets the maximum number of HTTP Headers that are permitted on a response
     * <p>
     * See {@link #maxHttpRequestHeaders(int)} for notes on default value and advice on appropriate values.
     * </p>
     *
     * @param max Maximum number of HTTP Response headers, or {@code -1} for unlimited
     * @return Builder
     */
    public ServerBuilder maxHttpResponseHeaders(int max) {
        this.maxResponseHeaders = max;
        return this;
    }

    /**
     * Sets the maximum size in bytes of a single HTTP Header
     * <p>
     * This defaults to {@value HttpServerFilter#DEFAULT_MAX_HTTP_PACKET_HEADER_SIZE} bytes.  You should only adjust
     * this value if the server is deployed in an environment where it might reasonably expect to receive larger
     * headers.  For example some authentication solutions involve sending very large {@code Cookie} headers that can be
     * rejected by the server in its default configuration.
     * </p>
     * <p>
     * Similar to the notes on {@link #maxHttpRequestHeaders(int)} changing this value should be done carefully as to
     * not restrict valid requests, but also not make the server vulnerable to denial of service attacks.  Unlike those
     * settings a value of {@code -1} here serves to make the server use its default value of
     * {@value HttpServerFilter#DEFAULT_MAX_HTTP_PACKET_HEADER_SIZE} so there is no unlimited setting available.
     * </p>
     *
     * @param bytes Maximum size in bytes, or {@code -1} for server default.
     * @return Server builder
     */
    public ServerBuilder maxHttpHeaderSize(int bytes) {
        this.maxHttpHeaderSize = bytes <= 0 ? -1 : bytes;
        return this;
    }

    /**
     * Attempts to build the actual server instance
     *
     * @return Built server
     */
    public Server build() {
        // Validate all required parameters are set
        if (this.applicationClass == null) {
            throw new IllegalStateException("Failed to specify an application class for the server");
        }
        if (this.port <= 0) {
            throw new IllegalStateException(
                    "Failed to specify a port for the server");
        }
        if (StringUtils.isBlank(this.displayName)) {
            throw new IllegalStateException("Failed to specify a display name for the server");
        }

        WebappContext context = new WebappContext(this.displayName, this.contextPath);

        // Add the JAX-RS application servlet
        ServletRegistration registration =
                context.addServlet(ServletContainer.class.getCanonicalName(), ServletContainer.class);
        registration.addMapping("/*");
        registration.setInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS,
                                      this.applicationClass.getCanonicalName());

        // Configure CORS
        if (this.corsBuilder != null) {
            FilterRegistration corsRegistration = context.addFilter("CORS", CrossOriginFilter.class);
            corsRegistration.setInitParameters(this.corsBuilder.buildInitParameters());
            corsRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
        } else {
            LOGGER.warn(
                    "ServerBuilder has explicitly disabled CORS, browsers will not be able to interact with this server as a result!");
        }

        // Add our context listeners that initialize the application
        for (Class<? extends ServletContextListener> listener : this.listeners) {
            context.addListener(listener);
        }

        // Add authentication exclusions (if any)
        // We intentionally dynamically configure this because we don't want to rely on configuring authentication
        // exclusions from the environment as we do with JwtAuthInitializer rather we want this to be explicit
        // configuration on the part of the application developer in knowing what paths should and shouldn't have
        // authentication applied.
        if (!this.authExclusions.isEmpty()) {
            final List<PathExclusion> builtExclusions = new ArrayList<>(this.authExclusions);
            context.addListener(new ServletContextListener() {
                @Override
                public void contextInitialized(ServletContextEvent sce) {
                    sce.getServletContext().setAttribute(JwtServletConstants.ATTRIBUTE_PATH_EXCLUSIONS, builtExclusions);
                }

                @Override
                public void contextDestroyed(ServletContextEvent sce) {
                    sce.getServletContext().removeAttribute(JwtServletConstants.ATTRIBUTE_PATH_EXCLUSIONS);
                }
            });
        }

        try {
            URI baseUri = new URI(String.format("http://%s:%d", this.hostname, this.port));
            HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, false);

            // As most of our APIs are dealing with data that is expressed as RDF knowledge the identifiers will be URIs
            // meaning that there's a strong chance we'll see encoded slashes - %2F - in our URIs where they are used
            // as path parameters.  Therefore, we need to enable this explicitly as the default server behaviour forbids
            // this.
            server.getHttpHandler().setAllowEncodedSlash(true);

            // In some deployment scenarios we can see very large headers, so we expose some controls to customise the
            // acceptable header quantities and sizes.  These are applied to the server being built now
            server.getListeners()
                  .forEach(l -> {
                      if (this.maxHttpHeaderSize != null) {
                          l.setMaxHttpHeaderSize(this.maxHttpHeaderSize);
                      }
                      if (this.maxRequestHeaders != null) {
                          l.setMaxRequestHeaders(this.maxRequestHeaders);
                      }
                      if (this.maxResponseHeaders != null) {
                          l.setMaxResponseHeaders(this.maxResponseHeaders);
                      }
                  });

            return new Server(server, baseUri, context, this.displayName);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(
                    "Failed to create a server as the hostname and port provided did not produce a valid URI", e);
        }
    }
}
