# docker build -t np-noproblem/bitbox:latest .

# docker run --restart=always --name=bitbox -d -p 8080:8080 -v /home/share:/app/share -v /home/configuration.properties:/app/configuration.properties np-noproblem/bitbox:latest


FROM maven:3.5-jdk-8-alpine as builder
WORKDIR /bitbox
COPY bitbox /bitbox
RUN mvn install


FROM openjdk:8-jre-alpine
WORKDIR /app
COPY --from=builder /bitbox/target/bitbox-0.0.1-SNAPSHOT-jar-with-dependencies.jar /app/bitbox.jar
CMD ["java", "-cp", "bitbox.jar", "unimelb.bitbox.Peer"]
