# 多阶段构建
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests -pl app-platform-common,app-platform-gateway -am

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/app-platform-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
