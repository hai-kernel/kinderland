# =====================================================================================
# Multi-stage build cho payment-service.
#
# BUILD CONTEXT PHẢI LÀ kinderland-microservices/ (Maven reactor root), KHÔNG phải
# thư mục service — cần parent pom, và Maven parse TOÀN BỘ reactor (root pom khai báo
# 9 module) nên thiếu bất kỳ module pom.xml nào là build fail.
#
#   docker build -f payment-service/Dockerfile -t kinderland-payment-service:phase-bc .
# =====================================================================================

FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

# Maven Wrapper: dùng đúng Maven 3.9.12 khai báo trong .mvn/wrapper/maven-wrapper.properties,
# không phụ thuộc Maven cài sẵn trong image. chmod +x vì git lưu mvnw ở mode 100644.
COPY .mvn .mvn
COPY mvnw mvnw
RUN chmod +x mvnw

# --- Lớp 1: chỉ pom.xml -> cache lớp dependency, đổi source KHÔNG phá cache này.
COPY pom.xml .
COPY kinderland-common/pom.xml      kinderland-common/pom.xml
COPY config-server/pom.xml          config-server/pom.xml
COPY eureka-server/pom.xml          eureka-server/pom.xml
COPY api-gateway/pom.xml            api-gateway/pom.xml
COPY auth-service/pom.xml           auth-service/pom.xml
COPY product-service/pom.xml        product-service/pom.xml
COPY order-service/pom.xml          order-service/pom.xml
COPY notification-service/pom.xml   notification-service/pom.xml
COPY payment-service/pom.xml        payment-service/pom.xml
RUN ./mvnw -B -pl payment-service -am dependency:go-offline -DskipTests || true

# --- Lớp 2: source.
# payment-service phụ thuộc kinderland-common (khai báo trong payment-service/pom.xml)
COPY kinderland-common/src kinderland-common/src
COPY payment-service/src payment-service/src

# skipTests CÓ CHỦ Ý: CI đã chạy `mvn clean verify` (test đầy đủ) TRƯỚC job build image.
# KHÔNG dùng -Dmaven.test.skip=true và KHÔNG skip test ở bước CI verify.
RUN ./mvnw -B -pl payment-service -am clean package -DskipTests

# Spring Boot repackage để lại cả *.jar và *.jar.original -> chọn tường minh,
# tránh COPY wildcard match nhầm file .original (không chạy được).
RUN cp "$(find payment-service/target -maxdepth 1 -name '*.jar' ! -name '*.original' | head -n 1)" /tmp/app.jar


FROM eclipse-temurin:21-jre
WORKDIR /app

# wget cho HEALTHCHECK trong docker-compose (image temurin-jre không có curl).
RUN apt-get update && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/* \
    && useradd -r -u 1001 -m appuser

COPY --from=builder /tmp/app.jar app.jar
RUN chown appuser:appuser /app/app.jar

# Chạy non-root (UID 1001): container bị chiếm quyền cũng không có root.
USER appuser

EXPOSE 8084

# JAVA_OPTS cho phép chỉnh heap/GC lúc chạy mà không sửa image.
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
