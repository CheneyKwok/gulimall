FROM java:8
EXPOSE 8080

VOLUME /temp
ADD target/*.jar /app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ['java','-jar','app.jar','--spring.profiles.active==prod']