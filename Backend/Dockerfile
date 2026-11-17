FROM maven:3.10-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

COPY src/ src/
RUN ./mvnw -B -DskipTests package

FROM registry.access.redhat.com/ubi9/openjdk-25-runtime:1.24

COPY --from=build --chown=185 /workspace/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /workspace/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /workspace/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /workspace/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 10000
USER 185
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"
ENTRYPOINT ["/opt/jboss/container/java/run/run-java.sh"]
