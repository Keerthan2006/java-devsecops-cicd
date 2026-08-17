# Stage 1
FROM maven:3.9.16-eclipse-temurin-17-alpine AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY . .

RUN mvn clean package -DskipTests

# Stage 2
FROM gcr.io/distroless/java17-debian13

WORKDIR /app

COPY --from=builder /app/target/*.jar ./app.jar

EXPOSE 9090

ENTRYPOINT [ "java","-jar","app.jar" ]

# DISTROLESS IMAGES already has a Java entrypoint internally.

