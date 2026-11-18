# Feature Toggle Application (Backend)

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management
* [JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) - Java™ Platform, Standard Edition Development Kit
* [Spring Boot](https://spring.io/projects/spring-boot) - Framework to ease the bootstrapping and development of new Spring Applications
* [MongoDB](https://www.mongodb.com/) - MongoDB is a document database with the scalability and flexibility that you want with the querying and indexing that you need
* [Lombok](https://projectlombok.org/) - Never write another getter or equals method again, with one annotation your class has a fully featured builder, Automate your logging variables, and much more.

## External Tools Used

* [Postman](https://www.getpostman.com/) - API Development Environment (Testing Documentation)

## Requirements

For building and running the application you need:

- [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven](https://maven.apache.org) (or use the included Maven Wrapper)
- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)

## Running unit tests and checking coverage locally

From the repository root (the directory containing `pom.xml`), run the unit tests and generate the JaCoCo coverage report:

```shell
./mvnw clean test jacoco:report
```

On Windows, run:

```shell
mvnw.cmd clean test jacoco:report
```

If Maven is installed globally, use:

```shell
mvn clean test jacoco:report
```

Open `target/site/jacoco/index.html` in a browser to review the generated coverage report.

## Running the application locally

There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method in the `com.raghu.fta.FeatureToggleApplication` class from your IDE.

- Download the zip or clone the Git repository.
- Unzip the zip file (if you downloaded one).
- Open a terminal and change directory (`cd`) to the folder containing `pom.xml` (repository root).
- Run the commands below:

```shell
mvn install
mvn package
docker-compose up
```

The application uses port 8080.

## Documentation

* [Postman Collection](https://www.getpostman.com/collections/d7f71be3afe40ccff8c6)
