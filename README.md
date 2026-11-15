# CollabManager -- Project Management & Collaboration Platform

A full-stack project management and team collaboration application built
with **React, Spring Boot, MySQL and Docker**. It supports project and
issue management, team invitations, comments, real-time chat, email
notifications, subscription plans and Razorpay payments.

The project is containerized and deployed on **AWS EC2**. Its CI/CD
pipeline uses **GitHub Actions**, **GitHub Container Registry (GHCR)**
and **AWS Systems Manager (SSM) Run Command**, with AWS authentication
through GitHub Actions OIDC.

> [Live Demo](http://13.238.142.161)
> This is the EC2 instance's current public IP. It can change if the
> instance is stopped and started without an Elastic IP. Update this URL
> if the address changes.

## Contents

## Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Repository Structure](#repository-structure)
- [Prerequisites](#prerequisites)
- [Local Setup](#local-setup-without-docker)
- [Docker Compose Setup](#docker-compose-setup)
- [Environment Variables](#environment-variables)
- [CI/CD Pipeline](#cicd-pipeline)
- [AWS Deployment](#aws-deployment)
- [Razorpay Payments](#razorpay-payments)
- [Email Notifications](#email-notifications)
- [API Documentation – Swagger / OpenAPI](#api-documentation--swagger--openapi)
  - [Production Swagger UI](http://13.238.142.161/swagger-ui/index.html)
  - [OpenAPI Specification](http://13.238.142.161/v3/api-docs)
- [Backend Testing Strategy](#backend-testing-strategy)
- [Unit Testing](#unit-testing)
- [Integration Testing](#integration-testing)
- [Code Coverage Analysis – JaCoCo](#code-coverage-analysis--jacoco)
- [Test Execution and Build Verification](#test-execution-and-build-verification)
- [Testing Results](#testing-results)
- [Useful Docker Commands](#useful-docker-commands)
- [Troubleshooting](#troubleshooting)
- [Security and Production Notes](#security-and-production-notes)
- [Future Improvements](#future-improvements)
------------------------------------------------------------------------

## Features

### Authentication and user management

-   User registration and login.
-   JWT-based authentication and protected APIs.
-   User profile management.
-   Password reset through email.

### Project management and collaboration

-   Create, view, update and delete projects.
-   Invite users to collaborate on projects.
-   Manage project team members.
-   Token-based invitation acceptance.

### Issues, tasks and comments

-   Create and manage project issues.
-   Assign issues to team members.
-   Track project work and issue status.
-   Add comments to issues.

### Real-time communication and notifications

-   WebSocket-based real-time communication.
-   Live chat for project members.
-   Email notifications using Gmail SMTP.

### Subscription and payments

-   Monthly and annual subscription plans.
-   Razorpay Payment Link integration.
-   Payment callback and subscription upgrade flow.

## Technology Stack

  Layer                         Technologies
  ----------------------------- -------------------------------------------
  Frontend                      React.js, JavaScript, Redux, Tailwind CSS
  Frontend tooling              Vite, npm
  Backend                       Java 17, Spring Boot 3.2.1
  Security                      Spring Security, JWT
  Persistence                   Spring Data JPA, Hibernate
  Database                      MySQL 8.0
  Real-time communication       WebSocket
  Payment gateway               Razorpay
  Email service                 Gmail SMTP
  Backend build                 Maven / Maven Wrapper
  Containerization              Docker, Docker Compose
  Web server / reverse proxy    Nginx
  CI/CD                         GitHub Actions
  Container registry            GitHub Container Registry (GHCR)
  Cloud hosting                 AWS EC2 (Amazon Linux 2023)
  Deployment automation         AWS Systems Manager Run Command
  AWS authentication in CI/CD   GitHub Actions OIDC and IAM role

## Architecture

### Application architecture

``` text
                         Browser
                            |
                            | HTTP :80
                            v
                    +----------------+
                    | Nginx / React  |
                    |   Frontend     |
                    +----------------+
                            |
                   /api, /auth, /ws
                            |
                            v
                    +----------------+
                    | Spring Boot    |
                    | Backend :5054  |
                    +----------------+
                            |
                            | JDBC
                            v
                    +----------------+
                    | MySQL 8.0      |
                    | Database       |
                    +----------------+

       External integrations:
       - Razorpay (payment links)
       - Gmail SMTP (email notifications)
```

Nginx serves the React application and proxies API and WebSocket traffic
to the backend. Within Docker Compose, the backend connects to MySQL
using the service hostname `mysql:3306`; Nginx reaches the backend using
`backend:5054`.

### CI/CD architecture

``` text
Developer
   |
   | git push to main
   v
GitHub Repository
   |
   v
GitHub Actions
   |
   +--> Detect changed paths
   +--> Backend: Maven build and tests
   +--> Frontend: npm install and production build
   +--> Build and publish changed Docker image(s)
   |          |
   |          v
   |      GHCR
   |
   v
AWS authentication using GitHub OIDC
   |
   v
AWS Systems Manager Run Command
   |
   v
EC2 instance
   |
   +--> Update changed image tag(s) in Compose
   +--> Pull updated image(s)
   +--> Recreate only changed service(s)
   |
   v
Live CollabManager
```

## Repository Structure

``` text
CollabManager/
│
├── .github/
│   └── workflows/
│       └── ci-cd.yml
│
├── CollabManagerbackend/
│   ├── src/
│   ├── Dockerfile
│   ├── .env.example
│   ├── .gitignore
│   ├── pom.xml
│   └── mvnw
│
├── CollabManagerfrontend/
│   └── frontend/
│       ├── src/
│       ├── public/
│       ├── Dockerfile
│       ├── nginx.conf
│       ├── package.json
│       └── package-lock.json
│
├── docker-compose.yml
├── .gitignore
└── README.md
```

## Prerequisites

### Local development

-   Git
-   JDK 17
-   Node.js 20 or compatible
-   MySQL Server 8.0
-   Maven (optional; Maven Wrapper is included)

### Docker development

-   Git
-   Docker Desktop
-   Docker Compose (included with Docker Desktop)

Docker Compose can run the frontend, backend and database without
requiring Java, Node.js or MySQL to be installed separately on the host.

## Local Setup (Without Docker)

In this setup, MySQL, the backend and frontend run separately.

### 1. Clone the repository

``` bash
git clone https://github.com/richadubeyess/CollabManager.git
cd CollabManager
```

### 2. Create the database

Start MySQL and log in:

``` bash
mysql -u root -p
```

Create the database:

``` sql
CREATE DATABASE collab;
```

Exit MySQL:

``` sql
EXIT;
```

### 3. Configure backend environment variables

``` bash
cd CollabManagerbackend
```

Create `.env` from the example.

**Windows PowerShell**

``` powershell
Copy-Item .env.example .env
```

**Linux/macOS**

``` bash
cp .env.example .env
```

Example local `.env`:

``` properties
SPRING_APPLICATION_NAME=CollabManager
SERVER_PORT=5054

SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/collab
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_mysql_password
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
SPRING_JPA_HIBERNATE_DDL_AUTO=update

SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_gmail_app_password
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true

RAZORPAY_API_KEY=your_razorpay_key
RAZORPAY_API_SECRET=your_razorpay_secret

FRONTEND_URL=http://localhost:5173
```

Replace all placeholders with your own credentials. Use a Gmail App
Password where required and Razorpay test credentials for test-mode
payments.

### 4. Run the backend

From `CollabManagerbackend`:

**Windows**

``` powershell
.\mvnw.cmd spring-boot:run
```

**Linux/macOS**

``` bash
./mvnw spring-boot:run
```

Backend URL:

``` text
http://localhost:5054
```

Alternatively, package and run the JAR:

``` bash
./mvnw clean package
java -jar target/projectmanagement-0.0.1-SNAPSHOT.jar
```

On Windows, use `.\mvnw.cmd` instead of `./mvnw`.

### 5. Run the frontend

Open another terminal:

``` bash
cd CollabManagerfrontend/frontend
npm install
npm run dev
```

Frontend URL:

``` text
http://localhost:5173
```

Check `src/Api/api.js` and make sure the API base URL is appropriate for
local development.

## Docker Compose Setup

Docker Compose runs the React frontend, Spring Boot backend and MySQL
database as separate services on a shared Docker network.

### 1. Clone the repository

``` bash
git clone https://github.com/richadubeyess/CollabManager.git
cd CollabManager
```

### 2. Create the environment file

**Windows PowerShell**

``` powershell
Copy-Item .\CollabManagerbackend\.env.example .\CollabManagerbackend\.env
```

**Linux/macOS**

``` bash
cp CollabManagerbackend/.env.example CollabManagerbackend/.env
```

For Docker, use the Compose service name in the database URL:

``` properties
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/collab
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_mysql_root_password
MYSQL_ROOT_PASSWORD=your_mysql_root_password
FRONTEND_URL=http://localhost:5173
```

Set SMTP and Razorpay credentials if you want to use those integrations.

### 3. Build and start all services

Run from the repository root:

``` bash
docker compose up -d --build
```

Compose builds the frontend and backend images, pulls MySQL, creates the
network and persistent database volume, and starts the services. The
backend waits for the MySQL health check before starting.

### 4. Verify containers

``` bash
docker compose ps
```

The local configuration has used these ports:

  Service    Container                          Host port
  ---------- -------------------------- -----------------
  Frontend   `collabmanager-frontend`              `5173`
  Backend    `collabmanager-backend`               `5054`
  MySQL      `collabmanager-mysql`        Internal `3306`

Check the current `docker-compose.yml` for the exact mappings.

### 5. Open the application

``` text
Frontend: http://localhost:5173
Backend:  http://localhost:5054
```

MySQL is reached internally at `mysql:3306`. It does not need to be
exposed publicly.

## Environment Variables

The backend uses environment variables for application configuration.
Create your own `.env` from `.env.example`; do not commit the real file.

  --------------------------------------------------------------------------------------------------------------------
  Variable                                             Purpose                 Example
  ---------------------------------------------------- ----------------------- ---------------------------------------
  `SPRING_APPLICATION_NAME`                            Spring application name `CollabManager`

  `SERVER_PORT`                                        Backend port            `5054`

  `SPRING_DATASOURCE_URL`                              JDBC URL                Local:
                                                                               `jdbc:mysql://localhost:3306/collab`;
                                                                               Compose:
                                                                               `jdbc:mysql://mysql:3306/collab`

  `SPRING_DATASOURCE_USERNAME`                         Database username       `root`

  `SPRING_DATASOURCE_PASSWORD`                         Database password       Private value

  `SPRING_DATASOURCE_DRIVER_CLASS_NAME`                JDBC driver             `com.mysql.cj.jdbc.Driver`

  `SPRING_JPA_HIBERNATE_DDL_AUTO`                      Hibernate schema        `update`
                                                       behavior                

  `SPRING_MAIL_HOST`                                   SMTP host               `smtp.gmail.com`

  `SPRING_MAIL_PORT`                                   SMTP port               `587`

  `SPRING_MAIL_USERNAME`                               SMTP account            Your email

  `SPRING_MAIL_PASSWORD`                               SMTP app password       Private value

  `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH`              SMTP authentication     `true`

  `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE`   SMTP STARTTLS           `true`

  `RAZORPAY_API_KEY`                                   Razorpay API key        Private value

  `RAZORPAY_API_SECRET`                                Razorpay API secret     Private value

  `MYSQL_ROOT_PASSWORD`                                MySQL container root    Private value
                                                       password                

  `FRONTEND_URL`                                       Frontend origin used in Local: `http://localhost:5173`;
                                                       generated links and     production: `http://<EC2_PUBLIC_IP>`
                                                       callbacks               
  --------------------------------------------------------------------------------------------------------------------

The backend imports an optional `.env` properties file for local
development. Docker Compose passes configured environment variables into
the containers.

## CI/CD Pipeline

The workflow is defined in:

``` text
.github/workflows/ci-cd.yml
```

It automates validation, Docker image publishing and deployment.

### Workflow stages

#### 1. Detect Changes

The workflow checks changed paths to determine whether the backend,
frontend or both have been modified.

#### 2. Backend Build and Test

-   Sets up Java 17.
-   Starts a MySQL 8 test service.
-   Runs Maven verification:

``` bash
./mvnw clean verify
```

#### 3. Frontend Build

-   Sets up Node.js 20.
-   Installs dependencies from the lock file:

``` bash
npm ci
```

-   Builds the production frontend:

``` bash
npm run build
```

#### 4. Build and Publish Docker Images

After validation succeeds, the workflow authenticates to GHCR and
builds/publishes the changed service image(s).

Image repositories:

``` text
ghcr.io/richadubeyess/collabmanager-backend
ghcr.io/richadubeyess/collabmanager-frontend
```

Images are tagged with the commit SHA, using this format:

``` text
sha-<github-commit-sha>
```

A `latest` tag is also published by the workflow.

#### 5. Deploy to AWS EC2 through SSM

-   Runs for eligible pushes after image publishing.
-   Uses GitHub Actions OIDC to obtain AWS credentials through an IAM
    role.
-   Invokes AWS Systems Manager Run Command on the EC2 instance.
-   Updates the changed image tag(s) in the EC2 Compose file.
-   Pulls the new image(s).
-   Recreates only the changed service(s).

### Selective deployment behavior

  -------------------------------------------------------------------------
  Change                                Expected result
  ------------------------------------- -----------------------------------
  `CollabManagerbackend/**`             Backend build/test, publish backend
                                        image, deploy backend

  `CollabManagerfrontend/frontend/**`   Frontend build, publish frontend
                                        image, deploy frontend

  Both paths                            Publish and deploy both services

  Pull request                          Validation/build only; no
                                        production deployment

  Documentation-only change             No application service deployment
  -------------------------------------------------------------------------

The actual conditions and path filters are defined in the workflow YAML.

### How to verify CI/CD

1.  Open the GitHub repository.
2.  Select **Actions**.
3.  Open the latest **CollabManager CI/CD** run.
4.  Check the status of:
    -   Detect Changes
    -   Backend Build and Test
    -   Frontend Build
    -   Build and Publish Docker Images
    -   Deploy to AWS EC2 through SSM
5.  Open the deployment job and inspect the SSM step output.
6.  Test the updated feature on the live website.

A green workflow run confirms that the workflow completed successfully.
Always perform a basic smoke test on the deployed application after
deployment.

## AWS Deployment

The application is hosted on an Amazon EC2 instance running Amazon Linux
2023.

### AWS components

  Component                         Responsibility
  --------------------------------- ---------------------------------------------------
  EC2                               Runs the application containers
  Docker                            Runs each application service
  Docker Compose                    Coordinates the containers
  GHCR                              Stores versioned frontend and backend images
  GitHub Actions                    Builds, tests, publishes and initiates deployment
  AWS Systems Manager Run Command   Runs deployment commands on EC2
  IAM and GitHub OIDC               Provides scoped AWS access to the workflow

### Production service layout

  Service               Container                  Production exposure
  --------------------- -------------------------- -----------------------
  Frontend / Nginx      `collabmanager-frontend`   Public HTTP port `80`
  Spring Boot backend   `collabmanager-backend`    Internal port `5054`
  MySQL                 `collabmanager-mysql`      Internal port `3306`

Nginx serves the React frontend and proxies API and WebSocket requests
to the backend. The backend and MySQL communicate over the private
Docker network.

### EC2 project and environment paths

Project directory:

``` text
/home/ec2-user/CollabManager
```

Backend environment file:

``` text
/home/ec2-user/CollabManager/CollabManagerbackend/.env
```

The production `.env` file is kept on EC2 and is not committed to
GitHub.

Set the production frontend origin:

``` properties
FRONTEND_URL=http://<EC2_PUBLIC_IP>
```

This value is used to construct frontend links, including password-reset
and invitation links. Use the local frontend URL during local
development and the deployed frontend origin in production.

### Deployment sequence

1.  A qualifying change is pushed to `main`.
2.  GitHub Actions detects the changed service.
3.  The relevant backend/frontend validation jobs run.
4.  Docker images are built and pushed to GHCR with commit-specific
    tags.
5.  GitHub Actions authenticates to AWS using OIDC.
6.  The workflow invokes SSM Run Command on EC2.
7.  The EC2 Compose file is updated for the changed image.
8.  Docker pulls the updated image and recreates the changed service.
9.  The workflow reports the command/deployment result.

### Useful commands on EC2

Connect using AWS Systems Manager Session Manager or another configured
secure access method, then:

``` bash
cd /home/ec2-user/CollabManager
```

Check service status:

``` bash
sudo docker compose ps
```

Check Compose syntax/configuration:

``` bash
sudo docker compose config --quiet
```

View backend logs:

``` bash
sudo docker compose logs --tail=100 backend
```

View frontend logs:

``` bash
sudo docker compose logs --tail=100 frontend
```

View MySQL logs:

``` bash
sudo docker compose logs --tail=100 mysql
```

Manual backend update, if needed:

``` bash
sudo docker compose pull backend
sudo docker compose up -d --no-deps backend
```

Manual frontend update, if needed:

``` bash
sudo docker compose pull frontend
sudo docker compose up -d --no-deps frontend
```

Normally, GitHub Actions performs image updates and deployment
automatically.

### Database persistence

MySQL data is stored in a named Docker volume. Normal container restarts
and recreation do not remove this volume.

Do not run this command unless you intentionally want to remove the
Compose-managed volumes and their database data:

``` bash
sudo docker compose down -v
```

A fresh installation does not automatically import data from a
developer's local MySQL database. Database backups and migrations must
be managed separately.

## Razorpay Payments

CollabManager uses Razorpay Payment Links for subscription payments.

### Payment flow

1.  An authenticated user selects a subscription plan.
2.  The frontend calls the backend payment endpoint and includes the JWT
    authorization header.
3.  The backend identifies the user from the JWT.
4.  The backend creates a Razorpay Payment Link using the configured API
    credentials.
5.  The backend returns the payment link to the frontend.
6.  The user completes the payment on Razorpay's hosted page.
7.  Razorpay redirects the browser to the configured callback URL.
8.  The frontend displays the payment result and the application
    continues the subscription upgrade flow.

Payment endpoint:

``` text
POST /api/payments/{planType}
```

Plan types used by the application:

``` text
MONTHLY
ANNUALLY
```

### Callback URL

The callback URL must point to the frontend success route. It must not
be hard-coded to `localhost` in production.

Example production callback:

``` text
http://<EC2_PUBLIC_IP>/upgrade_plan/success?planType=<PLAN_TYPE>
```

Keep the callback environment-aware so local and production deployments
use the correct frontend origin.

### Test mode

Use Razorpay test credentials for test payments. Test-mode transactions
are not real money transactions and are subject to Razorpay's current
test-account limits and policies. Do not place Razorpay secrets in
frontend code.

## Email Notifications

Gmail SMTP is used for email delivery. Configure these values in the
backend `.env`:

``` properties
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_gmail_app_password
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
```

Use a Gmail App Password where required. Do not use or commit your
normal account password. Ensure `FRONTEND_URL` points to the correct
frontend origin so invitation and password-reset links open the correct
application.

# API Documentation & Testing

##  API Documentation – Swagger / OpenAPI

CollabManager uses **Swagger UI with OpenAPI 3.0** to provide interactive documentation for its REST APIs.

The integration enables developers to explore available endpoints, understand API operations, review request and response structures, and execute API requests directly through the browser.

### Key Features

- Interactive documentation of REST API endpoints.
- Organized API documentation using controller-level tags.
- Support for HTTP methods, request parameters, and request bodies.
- JWT Bearer authentication for secured endpoints.
- Direct API execution and response inspection through Swagger UI.
- OpenAPI specification generated from the Spring Boot application.

### Technology Stack

- Spring Boot
- Springdoc OpenAPI
- Swagger UI
- OpenAPI 3.0
- JWT Authentication

### Authentication and Authorization

JWT Bearer authentication is integrated into Swagger UI to support testing of secured endpoints.

The Swagger authorization mechanism allows users to provide their JWT access token and execute protected API operations without manually configuring authorization headers for every request.

Public authentication and password-reset endpoints remain accessible without JWT authentication, while protected endpoints require valid authentication.

### API Organization

The API documentation is organized into logical groups to improve readability and navigation.

| API Module | Description |
|---|---|
| Authentication | User registration and authentication operations |
| Projects | Project creation and management |
| Users | User profile and management operations |
| Comments | Comment management operations |
| Payments | Subscription payment operations |
| Messages | Chat and messaging operations |
| Other Modules | Additional application functionality |

### Production Deployment

Swagger documentation is deployed alongside the application on AWS EC2.

**Production Documentation Links**

- **Swagger UI:** http://13.238.142.161/swagger-ui/index.html
- **OpenAPI Specification:** http://13.238.142.161/v3/api-docs

The frontend Nginx server is configured to route Swagger UI and OpenAPI requests to the Spring Boot backend, allowing API documentation to be accessed through the application's public address.

### API Testing Through Swagger UI

Swagger UI provides an interactive environment for testing APIs.

Developers can:
- Select an API endpoint.
- Provide required parameters and request payloads.
- Authorize requests using a JWT access token.
- Execute API operations.
- Inspect HTTP status codes and response bodies.

**Note:** Swagger documents REST APIs. WebSocket/STOMP messaging operations are tested separately using dedicated API testing tools.

---

## Backend Testing Strategy

A structured testing approach has been implemented to improve application reliability, validate business logic, and verify interactions between different application components.

The backend testing strategy includes:

- Unit Testing
- Integration Testing
- Code Coverage Analysis

### Testing Technologies

| Technology | Purpose |
|---|---|
| JUnit 5 | Automated test case execution |
| Mockito | Mocking dependencies and isolating components |
| Spring Boot Test | Testing Spring Boot application components |
| MockMvc | Testing REST controller endpoints |
| H2 Database | In-memory database for testing |
| JaCoCo | Code coverage analysis |
| Maven Surefire | Unit test execution |

---

## Unit Testing

Unit testing is used to validate individual application components independently, ensuring that business logic behaves as expected.

JUnit 5 and Mockito are used to develop and execute unit tests across the backend application.

### Testing Scope

- Service-layer business logic.
- Successful execution scenarios.
- Exception handling and failure scenarios.
- Repository interaction verification.
- Input and output validation.
- Independent testing of application components using mocked dependencies.

### Testing Approach

Mockito is used to isolate the component under test by mocking its dependencies. JUnit 5 is used to execute test cases and verify expected application behavior through assertions and interaction verification.

This approach helps identify defects at the component level without requiring the complete application environment.

---

## Integration Testing

Integration testing validates the interaction between multiple application components to ensure that they function correctly together.

Unlike unit testing, integration testing focuses on verifying application workflows and interactions across different layers.

### Testing Scope

- Controller and service layer interactions.
- Service and repository layer interactions.
- REST API request and response handling.
- Database operations.
- Application-level validation.
- Successful and unsuccessful execution scenarios.

### Testing Approach

Spring Boot Test is used to initialize the application testing environment, while MockMvc is used to simulate HTTP requests and validate controller responses.

An H2 in-memory database is configured for testing to provide an isolated database environment.

This ensures that automated tests can execute without depending on the production MySQL database or modifying production application data.

### Modules Covered

Integration testing has been performed across the following application modules:

| Module | Testing Focus |
|---|---|
| Authentication | Authentication-related application flows |
| User | User management operations |
| Project | Project management operations |
| Issue | Issue management operations |
| Comment | Comment management operations |
| Message | Messaging functionality |
| Password Reset | Password reset operations |
| Payment | Payment-related functionality |
| Subscription | Subscription management operations |

---

## Code Coverage Analysis – JaCoCo

JaCoCo is integrated into the backend project to measure automated test coverage and provide insights into the extent to which application code is exercised by the test suite.

### Coverage Metrics

The JaCoCo report provides visibility into:

- Instruction Coverage
- Branch Coverage
- Line Coverage
- Method Coverage
- Class Coverage

### Coverage Report

The coverage report is generated as part of the Maven verification process.

The generated HTML report provides a detailed view of coverage at the package, class, and method levels.

This helps identify untested code paths and areas where additional test cases may be beneficial.

---

## Test Execution and Build Verification

Maven is used to manage the backend build and automated test execution lifecycle.

The project supports local test execution and complete build verification through Maven.

The verification process includes:

- Cleaning previously generated build artifacts.
- Compiling application source files.
- Compiling test source files.
- Executing configured automated tests.
- Running the configured verification lifecycle.
- Generating the JaCoCo coverage report.

The test suite can be executed locally from the backend project directory.

---

## Testing Results

The backend testing suite has been executed successfully.

| Testing Category | Status |
|---|---|
| Unit Testing | Passed |
| Integration Testing | Passed |
| H2 Test Database Configuration | Completed |
| JaCoCo Integration | Completed |
| Maven Build Verification | Successful |

**Automated Testing:** Approximately 166 unit tests passed during the recorded test execution.

Integration tests were also executed successfully across the major application modules listed above.

---

## Benefits

The implementation of Swagger documentation and automated testing provides several benefits:

- **Improved API Discoverability:** Interactive API documentation makes endpoints easier to explore and understand.
- **Simplified API Testing:** Developers can execute and inspect API requests directly through Swagger UI.
- **JWT Authentication Testing:** Secured endpoints can be tested through Swagger's authorization mechanism.
- **Improved Code Reliability:** Unit tests validate individual components and business logic.
- **Integration Validation:** Integration tests verify interactions between application layers.
- **Database Isolation:** H2 provides an independent testing environment without affecting production MySQL data.
- **Regression Prevention:** Automated tests help detect unintended changes in existing functionality.
- **Code Quality Visibility:** JaCoCo helps identify areas requiring additional test coverage.



## Useful Docker Commands

Run these commands from the directory containing `docker-compose.yml`.

Start services:

``` bash
docker compose up -d
```

Build and start:

``` bash
docker compose up -d --build
```

Stop services:

``` bash
docker compose down
```

List containers:

``` bash
docker compose ps
```

View all logs:

``` bash
docker compose logs -f
```

Follow backend logs:

``` bash
docker compose logs -f backend
```

Follow frontend logs:

``` bash
docker compose logs -f frontend
```

Follow MySQL logs:

``` bash
docker compose logs -f mysql
```

Restart one service:

``` bash
docker compose restart backend
```

Rebuild one service locally:

``` bash
docker compose up -d --build backend
```

> `docker compose down` normally retains named volumes.
> `docker compose down -v` removes named volumes and can delete database
> data.

## Troubleshooting

### Port already in use

If ports `5173`, `5054` or `3306` are occupied, identify the process
using the port and stop it, or adjust the relevant host port mapping.
Avoid exposing internal production ports.

### Backend cannot connect to MySQL

For Docker Compose, confirm the JDBC URL uses the service hostname:

``` properties
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/collab
```

Then check:

``` bash
docker compose ps
docker compose logs backend
docker compose logs mysql
```

### MySQL authentication failed

Verify that the backend database credentials match the MySQL account.
For an existing database volume, changing `MYSQL_ROOT_PASSWORD` in
`.env` does not automatically change the password already stored in
MySQL.

### Frontend cannot communicate with backend

Check the API base URL, Nginx proxy locations in
`CollabManagerfrontend/frontend/nginx.conf`, backend container status
and backend logs. In production, browser requests should go to the
public frontend origin and Nginx should proxy API requests internally.

### Password-reset or invitation links open localhost

Check the backend `FRONTEND_URL`. Production must use the deployed
frontend origin, not `http://localhost:5173`. Restart/redeploy the
backend after changing its environment.

### Razorpay payment creation fails

Check that the Razorpay key ID and secret are correct and belong to the
same mode, that the request includes a valid JWT, and that the Razorpay
account's current test-mode limits and permissions allow the operation.
Inspect backend logs without exposing credentials.

### Payment completes but redirects to the wrong page

Check the `callback_url` used when creating the Payment Link. It must
point to the deployed frontend success route. Also verify that Nginx
serves the React application fallback for frontend routes.

### CI/CD deployment takes too long

Inspect the deployment job in GitHub Actions. In AWS Console, open
**Systems Manager → Run Command → Command history**, select the command
ID and inspect the output and error sections for the EC2 instance. A
command that remains `In Progress` may be waiting for an image pull,
container startup or another shell command.

### Check production containers and logs

``` bash
cd /home/ec2-user/CollabManager
sudo docker compose ps
sudo docker logs --tail=100 collabmanager-backend
sudo docker logs --tail=100 collabmanager-frontend
```

## Security and Production Notes

-   Never commit `.env` files, private SSH keys, AWS credentials,
    Razorpay secrets or SMTP passwords.
-   Keep production environment files on the deployment host or in a
    dedicated secrets manager.
-   The deployment workflow uses GitHub OIDC for AWS access; keep its
    IAM role permissions scoped to the required deployment actions and
    EC2 resources.
-   Restrict EC2 security-group ingress to the ports the application
    needs. Public users need the web entry point; backend `5054` and
    MySQL `3306` should remain internal.
-   Configure HTTPS with a domain name and TLS certificate before using
    the application for real users or sensitive data.
-   Use Razorpay live credentials only after completing the required
    account activation and production configuration.
-   Configure database backups and test the restore process.
-   Monitor EC2 memory and disk usage. Running MySQL and Spring Boot
    together on a small instance can be memory-intensive.
-   Do not run `docker compose down -v` unless you intentionally want to
    delete the database volume.
-   The current public IP is not a permanent domain. Consider an Elastic
    IP and custom domain for a stable production URL.

## Future Improvements

-   Configure HTTPS and a custom domain.
-   Add automated database backups and a tested restore process.
-   Add application monitoring and centralized logging.
-   Increase unit and integration test coverage.
-   Add database migration management.
-   Add deployment health checks and rollback strategy.
-   Add server-side payment verification and Razorpay webhooks for
    robust payment-state reconciliation.
-   Add production-grade secrets management and operational alerts.

## Author

**Richa Dubey**

-   GitHub: [richadubeyess](https://github.com/richadubeyess)
-   Repository:
    [CollabManager](https://github.com/richadubeyess/CollabManager)

If you find this project useful, feel free to explore the repository and
share feedback.
