FROM openjdk:11
RUN mkdir /app
COPY /target/horario-1.0.0-SNAPSHOT.jar /app/horario.jar
WORKDIR /app
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "horario.jar"]