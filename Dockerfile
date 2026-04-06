# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory to /app
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

# Compile the Java web server
RUN javac WebServer.java

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the web server when the container launches
CMD ["java", "WebServer"]
