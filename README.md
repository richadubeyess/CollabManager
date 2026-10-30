# CollabManager – Project Management & Collaboration Platform

CollabManager is a full-stack project management and collaboration platform built using **Spring Boot, React.js and MySQL**. It helps teams manage projects, collaborate in real time, track issues and communicate efficiently.

The application also supports subscription management and Razorpay payment integration.

## Features

- **User Authentication:** User registration, login and JWT-based authentication.
- **Project Management:** Create, update, delete and manage projects.
- **Team Collaboration:** Invite users and manage project team members.
- **Issue Tracking:** Create, assign and manage project issues.
- **Comments:** Add comments and collaborate on issues.
- **Real-Time Chat:** Communicate with project members using WebSocket-based messaging.
- **Email Notifications:** Email functionality using Gmail SMTP.
- **Subscription Management:** Manage subscription plans.
- **Payment Integration:** Razorpay integration for subscription payments.

## Tech Stack

| Layer | Technologies |
|---|---|
| Frontend | React.js, JavaScript, Redux, Tailwind CSS |
| Backend | Java 17, Spring Boot 3.2.1 |
| Security | Spring Security, JWT |
| ORM | Spring Data JPA, Hibernate |
| Database | MySQL 8.0 |
| Real-Time Communication | WebSocket |
| Payment Gateway | Razorpay |
| Email Service | Gmail SMTP |
| Build Tool | Maven, npm |
| Containerization | Docker, Docker Compose |
| Web Server | Nginx |

---

# 1. Project Structure

```text
CollabManager/
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

---

# 2. Prerequisites

You can run the application either locally or using Docker.

### Option A: Local Setup

Install the following:

- Java Development Kit (JDK) 17
- Node.js 20 or compatible version
- MySQL Server 8.0
- Git
- Maven (optional, since the project includes Maven Wrapper)

### Option B: Docker Setup

Install:

- Git
- Docker Desktop
- Docker Compose (included with Docker Desktop)

**Note:** With Docker setup, you do not need to install Java, Node.js or MySQL separately. Docker runs these services in containers.

---

# 3. Local Setup (Without Docker)

In this approach, the frontend, backend and database run separately on your local machine.

## Step 1: Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/CollabManager.git
```

Navigate to the project directory:

```bash
cd CollabManager
```

## Step 2: Configure MySQL

Start your local MySQL server.

Log in to MySQL:

```bash
mysql -u root -p
```

Create the database:

```sql
CREATE DATABASE collab;
```

Exit MySQL:

```sql
EXIT;
```

## Step 3: Configure Backend Environment Variables

Navigate to the backend directory:

```bash
cd CollabManagerbackend
```

Create your environment file by copying the example:

**Windows PowerShell:**

```powershell
Copy-Item .env.example .env
```

**Linux/macOS:**

```bash
cp .env.example .env
```

Open the `.env` file and configure your local database connection:

```properties
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

RAZORPAY_API_KEY=your_razorpay_test_key
RAZORPAY_API_SECRET=your_razorpay_test_secret
```

Replace the placeholders with your own credentials.

**Important:**
- Never commit your `.env` file to GitHub.
- Use a Gmail App Password if SMTP authentication requires it.
- Use your own Razorpay test credentials for payment integration.

## Step 4: Run the Backend

From the `CollabManagerbackend` directory, execute:

**Windows:**

```powershell
.\mvnw.cmd spring-boot:run
```

**Linux/macOS:**

```bash
./mvnw spring-boot:run
```

Alternatively, build and run the application:

```bash
mvn clean package
```

```bash
java -jar target/projectmanagement-0.0.1-SNAPSHOT.jar
```

Backend URL:

```text
http://localhost:5054
```

## Step 5: Configure and Run the Frontend

Open another terminal and navigate to the frontend directory:

```bash
cd CollabManagerfrontend/frontend
```

Install dependencies:

```bash
npm install
```

Check the frontend API configuration in:

```text
src/Api/api.js
```

Ensure that the API base URL points to your locally running backend at:

```text
http://localhost:5054
```

Run the React development server:

```bash
npm run dev
```

Frontend URL:

```text
http://localhost:5173
```

You can now access CollabManager through your browser.

---

# 4. Docker Setup (Recommended)

CollabManager supports Docker Compose to run the complete application using three services:

1. React Frontend
2. Spring Boot Backend
3. MySQL Database

Docker Compose automatically creates a network so that the backend can communicate with MySQL using the service name `mysql`.

## Step 1: Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/CollabManager.git
```

Navigate to the project directory:

```bash
cd CollabManager
```

## Step 2: Configure Environment Variables

Navigate to the backend directory:

```bash
cd CollabManagerbackend
```

Create your `.env` file from the example.

**Windows PowerShell:**

```powershell
Copy-Item .env.example .env
```

**Linux/macOS:**

```bash
cp .env.example .env
```

Configure the required credentials in `.env`.

For Docker, the MySQL datasource URL must use the Compose service name:

```properties
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/collab
```

Configure the database credentials:

```properties
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_mysql_root_password
MYSQL_ROOT_PASSWORD=your_mysql_root_password
```

Make sure `SPRING_DATASOURCE_PASSWORD` and `MYSQL_ROOT_PASSWORD` match when using the root account.

Configure Gmail SMTP and Razorpay credentials if you want to use email and payment functionality.

Return to the project root:

```bash
cd ..
```

## Step 3: Build and Start All Services

Run this single command from the project root:

```bash
docker compose up -d --build
```

Docker Compose will:

- Build the React frontend image.
- Build the Spring Boot backend image.
- Pull the MySQL 8.0 image.
- Create the MySQL container.
- Create a persistent Docker volume for database storage.
- Configure communication between containers.
- Wait for MySQL health checks before starting the backend.
- Start the frontend using Nginx.

## Step 4: Verify Running Containers

Execute:

```bash
docker compose ps
```

You should see three services:

| Service | Container | Host Port |
|---|---|---|
| Frontend | collabmanager-frontend | 5173 |
| Backend | collabmanager-backend | 5054 |
| MySQL | collabmanager-mysql | Internal 3306 |

MySQL should eventually show a healthy status.

## Step 5: Access the Application

Once all services are running, open:

**Frontend:**

```text
http://localhost:5173
```

**Backend:**

```text
http://localhost:5054
```

The backend communicates with MySQL internally through:

```text
mysql:3306
```

You do not need to expose the MySQL port to your host machine for the application to work.

---

# 5. Docker Commands

Run these commands from the project root.

### Start all services

```bash
docker compose up -d
```

### Build and start all services

```bash
docker compose up -d --build
```

### Stop all services

```bash
docker compose down
```

### View running containers

```bash
docker compose ps
```

### View logs of all services

```bash
docker compose logs
```

### View backend logs

```bash
docker compose logs -f backend
```

### View frontend logs

```bash
docker compose logs -f frontend
```

### View MySQL logs

```bash
docker compose logs -f mysql
```

### Restart a particular service

```bash
docker compose restart backend
```

### Rebuild a particular service

```bash
docker compose up -d --build backend
```

**Important:** The MySQL database uses a named Docker volume called `mysql_data`. This preserves database records when containers are stopped or recreated.

Avoid running:

```bash
docker compose down -v
```

unless you intentionally want to delete the database volume and its stored data.

---

# 6. Database Configuration

The application uses MySQL 8.0.

The database name is:

```text
collab
```

### Local Setup

The backend connects to:

```text
localhost:3306
```

### Docker Setup

The backend connects to:

```text
mysql:3306
```

The `mysql` hostname is the MySQL service name defined in `docker-compose.yml`.

Docker Compose uses a named volume:

```yaml
volumes:
  mysql_data:
```

This ensures database persistence across normal container restarts and container recreation.

A new installation starts with a fresh database. Existing local database records are not automatically included in a fresh clone.

---

# 7. Troubleshooting

### 1. Port Already in Use

If ports 5173 or 5054 are already occupied, stop the application using those ports or change the host port mappings in `docker-compose.yml`.

For local MySQL, make sure port 3306 is available.

### 2. Backend Cannot Connect to MySQL

For Docker, verify that the datasource URL is:

```properties
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/collab
```

Also verify that the MySQL container is healthy:

```bash
docker compose ps
```

Check backend logs:

```bash
docker compose logs backend
```

### 3. MySQL Authentication Failed

Verify that the following variables match:

```properties
MYSQL_ROOT_PASSWORD=your_mysql_root_password
SPRING_DATASOURCE_PASSWORD=your_mysql_root_password
```

For an existing Docker volume, changing the password in `.env` alone does not change the password already stored in MySQL.

### 4. Frontend Cannot Communicate with Backend

Check the frontend API configuration in:

```text
CollabManagerfrontend/frontend/src/Api/api.js
```

Ensure the browser-facing API URL points to the correct backend host and port.

### 5. View Container Logs

Use:

```bash
docker compose logs -f
```

This displays logs from all three services.

---

# 8. Important Notes

- Docker Desktop must be running before executing Docker Compose commands.
- Never upload `.env` files or database backups containing private information.
- Configure your own Gmail SMTP and Razorpay credentials.
- The Docker Compose setup creates a fresh database for a new installation; it does not import another developer's database backup.
- The MySQL Docker volume preserves data across normal container restarts.
- Local setup and Docker setup use different MySQL hostnames.
- The application is configured for local development; additional production configuration is required before public deployment.

