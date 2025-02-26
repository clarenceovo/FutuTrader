# Use an official OpenJDK slim runtime as a parent image
FROM openjdk:17-jdk-slim-bullseye

  # Set the working directory in the container
WORKDIR /app

  # Copy the current directory contents into the container at /app
COPY . /app

  # Install Maven
RUN apt-get update && \
apt-get install -y maven

  # Build the application
RUN mvn clean install

  # Run the application
CMD ["java", "-cp", "target/FUTUJavaBot-1.0-SNAPSHOT.jar", "com.fututrader.Main"]
