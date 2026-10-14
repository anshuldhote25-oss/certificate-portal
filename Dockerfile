# Runtime image for the Certificate Generation Portal.
# Build the WAR first so the exact tested artefact is shipped:   mvn -B clean package -DskipTests
FROM tomcat:10.1-jdk17-temurin

ARG APP_ENV=dev
ENV APP_ENV=${APP_ENV} \
    DATA_DIR=/data

# Remove the default Tomcat sample apps, add a non-root user and a data folder for the H2 database
RUN rm -rf /usr/local/tomcat/webapps/* \
 && groupadd --system app && useradd --system --gid app --home-dir /usr/local/tomcat app \
 && mkdir -p /data \
 && chown -R app:app /data /usr/local/tomcat

COPY --chown=app:app target/certificate-portal.war /usr/local/tomcat/webapps/certificate-portal.war

USER app
VOLUME /data
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl --fail --silent http://localhost:8080/certificate-portal/health || exit 1
