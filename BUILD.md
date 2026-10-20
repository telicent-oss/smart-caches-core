# Building Smart Caches Core

Assuming you have the [requirements](#requirements) installed you can build via Maven i.e.

```
$ mvn clean install
```

## Requirements

This directory and its subdirectories are a multimodule Maven project, therefore you will require the following software
to develop and build this code:

- JDK 21 (or higher)
- [Apache Maven](https://maven.apache.org) (3.8.1 or higher)
- Docker for container based tests
    - Can be opted out of by disabling the `docker` profile e.g. by adding `-P-docker` to the Maven arguments

**NB** These minimum requirements are enforced as part of the Maven build using the Maven Enforcer plugin.

## Development

It is recommended to use an IDE of your choice that supports Maven projects, most such IDEs have the option to import an
existing Maven project which you should use with this directory.

### Code Licensing

The build enforces that all code files **MUST** have an appropriate license header, this may be found in the
`header.txt` file in this directory.

You can automatically apply the header to new code by running `mvn license:format` in this directory.

If you introduce new code that can't have a license header for any reason you will need to edit the `pom.xml` to add an
`<exclude>` to the license plugin configuration.

If you introduce a new module you need to define `license.header.path` property in the `<properties>` section of your
modules `pom.xml` that points back to the top level directory so that the plugin can correctly locate the `header.txt`
file from any module.

### Test Coverage

The build is set up with the [JaCoCo](https://www.eclemma.org/jacoco/trunk/index.html) Maven plugin to calculate test
coverage as part of all builds. It is also set up to enforce minimum levels of code coverage. You can add the following
`<plugin>` definition to the `pom.xml` of any module where you want to enable this enforcement:

```xml

<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>${plugin.jacoco}</version>
</plugin>
```

There's no need for specific configuration since that is handled by the `<pluginManagement>` section of the top level
`pom.xml` in this directory. By default, the plugin is configured to require 80% code coverage.

If you want to increase/decrease the desired minimum coverage level you can add a `coverage.minimum` property to the
`<properties>` section of your module and set the minimum coverage level e.g. `0.9` would increase required coverage to
90%.

### SNAPSHOT Builds

Maven Snapshots are only published from `main`, or when a developer manually runs `mvn deploy` from their machine.

### Release Builds

Release builds should be carried out using the normal Maven release process from a developers local machine.

Firstly make sure `main` is up-to-date and then create a new branch from there for the release:

```bash
$ git checkout main
$ git pull
$ git checkout -B release/<version>
$ git push -u origin release/<version>
```

Then you can prepare a Maven release, following the prompts to supply the desired git tag and next development version:

```bash
$ mvn release:clean release:prepare -DreleaseVersion=<version>
```

Make sure you have pushed your release preparation to the remote repository:

```bash
$ git push
$ git push --tags
```

The tag created by the `mvn release:prepare` will trigger an automatic GitHub Actions build that includes releasing the
libraries to Maven Central.  Please check the Actions tab for a build with the tag you just created and ensure that the
build passes.

Finally, please go to GitHub and open a PR to merge the release branch you created back into `main`.

## RDF-ABAC Plugin Image

The `telicent-data-security-rdf-abac-plugin` image is a data-only image for Kubernetes Image Volumes. Its base is `scratch`,
and its root contains only the RDF-ABAC registration JAR, implementation JAR and non-provided runtime dependency JARs.
There is no entrypoint, shell or JVM. The consuming application supplies `data-security-core` and the other dependencies
marked `provided` in Maven. Packaging also excludes a number of libraries that are otherwise are supplied by 
applications through `data-security-core` and its runtime dependencies in order to cut down the size of the produced JAR. 
Graph's `scg-system`, Search's `search-api-server` and Documents' services declare the relevant Core/server dependencies.
Other runtime dependencies are retained, including the RDF-ABAC libraries, Fuseki Kafka module, storage libraries,
RocksDB JNI and their supporting libraries. This is a minimal filesystem, not a small pure-Java plugin: RocksDB JNI
alone is about 80 MiB. Some remaining supporting libraries can overlap with applications.

The image and host must use compatible Core, storage, Jena and Fuseki Kafka APIs. Prefer the same Core release as the
host; appending the plugin last does not make a newer plugin compatible with an older host. In particular, a plugin
built against Core 1.8.x is not certified for Core 1.7.x. Adoption must validate the chosen version pair.

Runtime JAR collection deliberately runs on every `package`: the shared publishing workflow runs a fixed Maven
`verify -DskipTests` command, so this ensures its image context is ready without a special build profile. Use a clean
build when changing dependencies so obsolete JARs are removed from `target/lib`.

Build the JARs and runtime dependencies from the repository root:

```bash
mvn -pl data-security-parent/data-security-plugin-rdf-abac-registration -am clean package -DskipTests -Dgpg.skip=true
```

Then build the image, using the Maven project version as `PROJECT_VERSION`:

```bash
docker build --target telicent-data-security-rdf-abac-plugin \
  --build-arg PROJECT_VERSION="$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)" \
  -f docker/Dockerfile -t telicent-data-security-rdf-abac-plugin:local .
```

`./docker-build.sh [tag] [registry] [image]` builds both images by default, and pushes them when a registry is supplied.
To build only the plugin after the Maven command above, run
`./docker-build.sh local "" telicent-data-security-rdf-abac-plugin`; this does not require debug CLI artifacts.
Building both images requires a full Maven build first. In CI, `verify-rdf-abac-image` checks the payload before the
`rdf-abac-image` publishing job uses the shared workflow, with the same version tagging and registry configuration
as the debug tools.

Consumers should mount the image root read-only at `/opt/telicent/security/plugins` and append
`/opt/telicent/security/plugins/*` to their application classpath. Deployment and Helm chart adoption is handled
separately; this image is not intended to run as a container. `cli-common.sh` and the base server do not yet add this
entry automatically. The adoption follow-up must cover both launch paths and the deployment/chart mount, and verify
that the mounted plugin loads with the application dependency versions.

CI also runs `docker/PluginSmokeTest.java` with the image JARs appended to a classpath of `data-security-core` and its
runtime dependencies (collected by the `plugin-smoke-test` profile of that module). It links every plugin class, loads
the plugin via `DataSecurityPluginLoader`, and exercises the authorizer, labels applicator, Fuseki module, Fuseki operations, lifecycle filters and dataset
adapters (the last against a non-ABAC dataset, as no real ABAC dataset is built), so a dependency wrongly
excluded from the image fails the build rather than a consuming application.

CI validates the image before publishing with `python3 docker/verify-rdf-abac-image.py IMAGE VERSION`. The check
inspects the image archive without running it and requires only the expected read-only root JARs, exactly one
RDF-ABAC service registration, no application-supplied or test JARs, and no entrypoint or command.

The shared publishing workflow runs Trivy and Grype with the strict deployed-image policy, including unfixed high and
critical findings. Exceptions require an assessed VEX through the shared workflow, not a blanket ignore. On release
tags its Trivy job generates an image-derived CycloneDX SBOM, uploads it as a workflow artifact and attaches it to the
GitHub release. Branch builds do not emit that SBOM. `DOWNLOAD_SBOM: false` only disables importing an existing Maven
SBOM; the JAR-only image intentionally has no `/opt/telicent/sbom` directory.
