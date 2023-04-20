FROM java:openjdk-8u111-jre-alpine
EXPOSE 8080

VOLUME /temp
ADD target/*.jar /app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-jar","-Xms128m","-Xmx300m","/app.jar","--spring.profiles.active=prod"]