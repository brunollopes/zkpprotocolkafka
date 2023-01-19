FROM maven:3.8.2-openjdk-17-slim AS build

WORKDIR /home/app
COPY . /home/app
RUN mvn -f /home/app/pom.xml clean package -Dmaven.test.skip

FROM openjdk:17-jdk-slim
VOLUME /tmp
EXPOSE 8000
COPY --from=build /home/app/target/*.jar zkpprover.jar
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /zkpprover.jar" ]