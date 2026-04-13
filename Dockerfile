#Build React
FROM node:24 AS frontend-build

WORKDIR /frontend

COPY frontend/package*.json ./
RUN npm install

COPY frontend .
RUN npm run build


#Build Spring Boot
FROM gradle:9.3.0-jdk25 AS backend-build

WORKDIR /backend

COPY backend/ .

RUN gradle build -x test


#Runtime
FROM eclipse-temurin:25-jdk

WORKDIR /app

#Copy Spring Boot jar
COPY --from=backend-build /backend/build/libs/*.jar app.jar

#Copy React build into static
COPY --from=frontend-build /frontend/dist/assets ./static/assets

#Shared folder
RUN mkdir -p /app/shared

ENTRYPOINT ["java", "-jar", "app.jar"]