# Yêu cầu: chạy `mvn clean package` trước để có target/*.jar
FROM eclipse-temurin:21-jre
WORKDIR /app
# wget dùng cho healthcheck /actuator/health trong docker-compose.full.yml
RUN apt-get update && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/*
COPY target/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
