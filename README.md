# Calculator Microservice

This is a Spring Boot-based microservice for calculating averages of numbers fetched from various APIs.

## Description

The Calculator microservice fetches numbers from different APIs (Prime, Fibonacci, Even, Random) and calculates the average of the numbers. It maintains a sliding window of the last 10 unique numbers fetched.

## Prerequisites

- Java 17
- Maven

## Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/Jainharshit09/220557.git
    cd Calculator
    ```

2. Build the project using Maven:
    ```sh
    mvn clean install
    ```

## Usage

1. Run the application:
    ```sh
    mvn spring-boot:run
    ```

2. The application will start on port 8080 by default. You can access the endpoints using a tool like `curl` or Postman.

## API Endpoints

- **GET /number/p**: Fetches prime numbers and calculates the average.
- **GET /number/f**: Fetches Fibonacci numbers and calculates the average.
- **GET /number/e**: Fetches even numbers and calculates the average.
- **GET /number/r**: Fetches random numbers and calculates the average.

## Configuration

The API URLs are configured in the `application.properties` file:

```ini
spring.application.name=Calculator
PRIME_API_URL=http://20.244.56.144/test/primes
FIBO_API_URL=http://20.244.56.144/test/fibo
EVEN_API_URL=http://20.244.56.144/test/even
RANDOM_API_URL=http://20.244.56.144/test/rand