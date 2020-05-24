FROM openjdk:8-jdk-alpine
ARG JAR_FILE=springws/target/dhislink-springws-1.0.jar
COPY ${JAR_FILE} dhislink.jar
COPY /etc/hosts /etc/
ENTRYPOINT ["java","-jar","/dhislink.jar"]