# =====================================================================================
# Multi-stage build cho eureka-server.
#
# BUILD CONTEXT PHẢI LÀ kinderland-microservices/ (Maven reactor root), KHÔNG phải
# thư mục service — vì service cần parent pom, và Maven parse TOÀN BỘ reactor
# (root pom khai báo 9 module) nên thiếu bất kỳ module pom.xml nào là build fail.
#
#   docker build -f eureka-server/Dockerfile -t kinderland-eureka-server:phase-a .
# =====================================================================================

FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

# --- Lớp 1: chỉ pom.xml -> Docker cache lớp dependency, đổi source KHÔNG phá cache này.
COPY pom.xml .
COPY kinderland-common/pom.xml       kinderland-common/pom.xml
COPY config-server/pom.xml           config-server/pom.xml
COPY eureka-server/pom.xml           eureka-server/pom.xml
COPY api-gateway/pom.xml             api-gateway/pom.xml
COPY auth-service/pom.xml            auth-service/pom.xml
COPY product-service/pom.xml         product-service/pom.xml
COPY order-service/pom.xml           order-service/pom.xml
COPY notification-service/pom.xml    notification-service/pom.xml
COPY payment-service/pom.xml         payment-service/pom.xml

# Tải dependency của eureka-server + các module nó phụ thuộc.
# -am: also make (build cả module phụ thuộc). Lỗi ở bước này không chặn build,
# vì `package` bên dưới vẫn tự tải được — chỉ mất lợi ích cache.
RUN mvn -B -pl eureka-server -am dependency:go-offline -DskipTests || true

# --- Lớp 2: source. Chỉ copy module cần thiết -> đổi service khác không rebuild cái này.
COPY eureka-server/src eureka-server/src

# skipTests CÓ CHỦ Ý: CI đã chạy `mvn clean verify` (chạy test đầy đủ) TRƯỚC job build image.
# Chạy lại test trong 8 lần docker build là lãng phí. KHÔNG dùng -Dmaven.test.skip=true
# (bỏ cả compile test) và KHÔNG skip test ở bước CI verify.
RUN mvn -B -pl eureka-server -am package -DskipTests

# Spring Boot repackage để lại cả *.jar và *.jar.original -> chọn tường minh,
# tránh COPY wildcard match nhầm file .original (không chạy được).
RUN cp "$(find eureka-server/target -maxdepth 1 -name '*.jar' ! -name '*.original' | head -n 1)" /tmp/app.jar


FROM eclipse-temurin:21-jre
WORKDIR /app

# wget cho HEALTHCHECK (image temurin-jre không có curl).
RUN apt-get update && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/* \
    && useradd -r -u 1001 -m appuser

COPY --from=builder /tmp/app.jar app.jar
RUN chown appuser:appuser /app/app.jar

# Chạy non-root: container bị chiếm quyền cũng không có root.
USER appuser

EXPOSE 8761

# JAVA_OPTS cho phép chỉnh heap/GC lúc chạy mà không sửa image.
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
