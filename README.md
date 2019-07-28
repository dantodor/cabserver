# Cab server

## Requirements - Taxi booking system

### Problem statememt
You are tasked to implement a simple taxi booking system in a 2D grid world with the following criteria:

- The 2D grid world consists of `x` and `y` axis that each fit in a 32 bit integer, i.e. `-2,147,483,648` to `2,147,483,647`.
- There are **3** cars in the system, All three cars should have id `1`, `2` and `3` respectively and initial start location is at origin `(0, 0)`. Note that you can store the car states in memory and there is no need for persistent storage for this exercise.
- A car travels through the grid system will require **1 time unit** to move along the `x` or `y` axis by **1 unit** (i.e. Manhattan distance). For example
    - Car at `(0, 0)` will reach `(0, 2)` in 2 time units.
    - Car at `(1, 1)` will reach `(4, 4)` in 6 time units.
    - More than 1 car can be at the same point at any time.

### APIs

Your service should be running on PORT 8080. For simplicity, you

- **DO NOT** need to implement any form persistent storage. **In memory** data structures will be sufficient for this exercise.
- **DO NOT** need to handle concurrent API calls/data races. The APIs will be triggered **serially**.

There are 3 REST APIs you will need to implement.

#### `POST /api/book`

Your system should pick the nearest available car to the customer location and return the total time taken to travel from the current car location to customer location then to customer destination.

- Request payload
```json
{
  "source": {
    "x": x1,
    "y": y1
  },
  "destination": {
    "x": x2,
    "y": y2
  }
}
```

- Response payload
```json
{
  "car_id": id,
  "total_time": t
}
```
- All car are available initially, and become booked once it is assigned to a customer. It will remain booked until it reaches its destination, and immediately become available again.
- In the event that there are more than one car near the customer location, your service should return the car with the smallest id.
- Only one car be assigned to a customer, and only one customer to a car.
- Cars can occupy the same spot, e.g. car 1 and 2 can be at point (1, 1) at the same time.
- If there is no available car that can satisfy the request, your service should return an empty response, not an error

#### `POST /api/tick`

To facilitate the review of this exercise, your service should expose /api/tick REST endpoint, when called should advance your service time stamp by 1 time unit.

#### `PUT /api/reset` 
Your service should also provide /api/reset REST endpoint, when called will reset all cars data back to the initial state regardless of cars that are currently booked.


### System requirements
Your solution should

- Be implemented using any web framework and/or language of your own choice (e.g. Nodejs, Django, Dropwizard, Play, C#, Go ...etc.)
- Use appropriate algorithms/data structures
- Demonstrate proper software design and engineering practices (i.e. SOLID principles, unit testing ...etc.)
- Be of production quality
- Able to run on Linux
- Contains unit tests and clear instructions on how to build/execute them
- Have clear design/API documentation

## Assumptions

Since the empty request was not fully defined, I took the liberty to assume that an empty response is defined by car_id 0 and total_time 0.

## General implementation considerations
Since Scala is one of my favourite languages, I choosed to solve the task using Scala and Akka actor library.
The system is comprised of an HTTP server built on Akka HTTP, that wraps the logic.
The core is implemented by two actors, FleetManager and Cab.
FleetManager handles the logic of fulfilling web requests, and its Receive function is implemented as a tail recursive function that wraps immutable state.
Cab wraps the logic for handling a single car, and its inner workings are implemented using a state machine with two states, working and waiting.
The messaging protocol is defined in package Model, file Protocol, and the required request and response wrappers are in Models.
Server is configured via the ServerSettings class, which loads its data from application.conf file in the resources folder.
The API is defined in ServerRoutes, and the HTTP subsystem is brought up to life in CabServer file.
For further implementation details please see the source code and comments. 

## Usage

### Prerequisites
- a computer with Windows, Linux or OSX 
- Java JDK, minimal version 1.8 installed
- Scala Simple Build Tool ( sbt ) installed - <https://www.scala-sbt.org/release/docs/Setup.html>
- unzipped source code residing in a local folder
- open command prompt and change directory to that folder

### Compile 

```
$ sbt clean compile
```

### Run tests 

```
$ sbt clean coverage test
```

### Generate code coverage report 

```
$ sbt coverageReport
```

### Run

#### 1. Via sbt 

```
$ sbt run
```
### 2. Via docker
NB, you must have docker installed on your system and appropriate permissions set to the docker daemon, i.e. current user must be able to run docker commands 

```
$ sbt docker:publishLocal
```
This will pull a JDK8 image locally and build a docker image for the app
```
docker run -d -p 8080:8080 --name cabserver cabserver:0.0.1
```
This will run the docker container generated in the previous step.

The API provided by both these methods can be called now according to the given specs.
Also, the parameters of the system can be easily modified by editing application.conf ( http params, number of cabs, initial position, etc. )

