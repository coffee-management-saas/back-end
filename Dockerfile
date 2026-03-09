# Stage 1: build
FROM maven:3.9.8-amazoncorretto-21 AS build

WORKDIR /app

# Copy file cần thiết trước để tận dụng cache dependency
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Tải dependency trước
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build jar
RUN ./mvnw clean package -DskipTests

# Stage 2: runtime
FROM amazoncorretto:21-alpine

WORKDIR /app

# Copy file jar từ stage build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Cho phép truyền giới hạn RAM qua JAVA_OPTS từ docker-compose
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]