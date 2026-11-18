# Feature Toggle Application (Backend)

## Built With

* 	[Maven](https://maven.apache.org/) - Dependency Management
* 	[JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) - Java™ Platform, Standard Edition Development Kit 
* 	[Spring Boot](https://spring.io/projects/spring-boot) - Framework to ease the bootstrapping and development of new Spring Applications
* 	[MongoDB](https://www.mongodb.com/) - MongoDB is a document database with the scalability and flexibility that you want with the querying and indexing that you need
* 	[Lombok](https://projectlombok.org/) - Never write another getter or equals method again, with one annotation your class has a fully featured builder, Automate your logging variables, and much more.


## External Tools Used

* [Postman](https://www.getpostman.com/) - API Development Environment (Testing Docmentation)

## Requirements

For building and running the application you need:

- [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven 4](https://maven.apache.org)
- [Docker](https://www.docker.com/)
- [Docker-compose](https://docs.docker.com/compose/)

## Running unit tests and checking coverage locally

From the repository root (the directory containing `pom.xml`), run the tests, generate the JaCoCo report, and enforce the 90% instruction coverage threshold:

```shell
./mvnw clean verify
```

On Windows, run:

```shell
mvnw.cmd clean verify
```

If Maven is installed globally, use `mvn clean verify` instead. Open `target/site/jacoco/index.html` in a browser to review the coverage report. The build fails if instruction coverage is below 90%. JaCoCo 0.8.12 requires Java 8 or later, matching this project's JDK 8 requirement ([release notes](https://www.jacoco.org/jacoco/trunk/doc/changes.html)).

> **Note:** The Docker image plugin is bound to Maven's `package` phase, which runs during `verify`. A Docker daemon must be available when running this command.

## Running the application locally

There are several ways to run a Spring Boot application on your local machine. 
One way is to execute the `main` method in the `com.raghu.fta.FeatureToggleApplication` class from your IDE.

- Download the zip or clone the Git repository.
- Unzip the zip file (if you downloaded one)
- Open Command Prompt and Change directory (cd) to folder containing pom.xml (root)
- Run the below command

```shell
mvn install
mvn package
docker-compose up
```
The application uses the port: 8080

## Documentation

* [Postman Collection](https://www.getpostman.com/collections/d7f71be3afe40ccff8c6)
