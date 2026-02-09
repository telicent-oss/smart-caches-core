#
# Copyright (C) Telicent Ltd
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# syntax=docker/dockerfile:1.7

# Application Base Image
FROM telicent/telicent-java21:1.2.40 AS app-base

USER root


# Define arguments with default values to prevent build failures
ARG PROJECT_VERSION

# Define common directories
ENV APP_DIR="/app" \
    SBOM_DIR="/opt/telicent/sbom"

# Create necessary directories and set permissions in a single step
RUN mkdir -p $APP_DIR/lib $APP_DIR/agents $SBOM_DIR && \
    chown -R user:user $APP_DIR $SBOM_DIR

WORKDIR $APP_DIR

# Copy common scripts with correct ownership
COPY --chown=user:user cli/cli-common.sh cli/cli-entrypoint.sh $APP_DIR/

# Use non-root user for security
USER user

# Define project version as an environment variable
ENV PROJECT_VERSION="${PROJECT_VERSION}"

# CLI Base Image
FROM app-base AS cli-base

ARG PROJECT_VERSION
COPY --chown=user:user cli/cli-debug/target/cli-debug-${PROJECT_VERSION}-bom.json \
     /opt/telicent/sbom/
COPY --chown=user:user cli/cli-debug/target/cli-debug-${PROJECT_VERSION}.jar \
     cli/cli-debug/lib/* \
     /app/lib/
COPY --chown=user:user cli/cli-debug/agents/* \
     /app/agents/

ENV OTEL_METRICS_EXPORTER=none \
    OTEL_TRACES_EXPORTER=none

# Debug Tools
FROM cli-base AS smart-cache-debug-tools

COPY --chown=user:user cli/cli-debug/debug.sh /app/

ENV CLI_SCRIPT=/app/debug.sh
ENTRYPOINT [ "/usr/bin/dumb-init", "-v", "--single-child", "--", "/app/cli-entrypoint.sh" ]
