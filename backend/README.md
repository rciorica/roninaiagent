# Ronin Backend

Spring Boot backend for the Ronin AI Agent platform. Manages user projects, LLM integrations, project testing, and user ranking.

## Prerequisites

- **Java 21** (for local development)
- **Maven 3.9+** or use the Maven wrapper (`./mvnw`)
- **Docker** (for containerized builds and deployment)
- **PostgreSQL** (or Supabase PostgreSQL instance)

## Local Development

### Setup

1. Clone the repository:
   ```bash
   git clone <repo-url>
   cd roninaiagent/backend
   ```

2. Configure `src/main/resources/application.properties` with your database credentials:
   ```properties
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres
   SPRING_DATASOURCE_USERNAME=postgres
   SPRING_DATASOURCE_PASSWORD=yourpassword
   ```

3. Build the project:
   ```bash
   ./mvnw clean package -DskipTests
   ```

4. Run the application:
   ```bash
   java -jar target/backend-0.0.1-SNAPSHOT.jar
   ```

   The backend will start on `http://localhost:8080` and apply database migrations automatically.

### Run Tests

```bash
./mvnw test
```

## Docker Build & Run

### Build Docker Image

From the repository root (where both `backend/` and `frontend/` folders exist):

```bash
docker build -f backend/Dockerfile -t ronin-backend:latest .
```

This creates a multi-stage image:
- **Build stage**: Maven 21 compiles the application.
- **Runtime stage**: Lightweight JRE 21 runs the compiled jar.

### Run Docker Container Locally

```bash
docker run --rm \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://host.docker.internal:5432/postgres" \
  -e SPRING_DATASOURCE_USERNAME="postgres" \
  -e SPRING_DATASOURCE_PASSWORD="yourpassword" \
  -e OPENROUTER_API_KEY="your-api-key" \
  -e GOOGLE_CLIENT_ID="your-google-client-id" \
  -e GOOGLE_CLIENT_SECRET="your-google-client-secret" \
  -e FRONTEND_OAUTH_SUCCESS_URL="http://localhost:5173/" \
  ronin-backend:latest
```

For connecting to a local PostgreSQL: use `host.docker.internal` on macOS/Windows, or the container network on Linux.

## Deployment to Heroku

### Prerequisites

- [Heroku CLI](https://devcenter.heroku.com/articles/heroku-cli) installed
- GitHub repository (public or private)
- Heroku account

### Steps

1. **Build the JAR locally**:
   ```bash
   cd backend
   mvn clean package -DskipTests
   ```

2. **Login to Heroku**:
   ```bash
   heroku login
   ```

3. **Create a Heroku app**:
   ```bash
   heroku create your-app-name
   ```
   Replace `your-app-name` with your desired app name.

4. **Add PostgreSQL database**:
   ```bash
   heroku addons:create heroku-postgresql:hobby-dev -a your-app-name
   ```
   This creates a free-tier PostgreSQL database. Heroku automatically sets the `DATABASE_URL` environment variable.

5. **Set environment variables**:
   ```bash
   heroku config:set -a your-app-name \
     OPENROUTER_API_KEY=your-openrouter-api-key \
     GOOGLE_CLIENT_ID=your-google-oauth-client-id \
     GOOGLE_CLIENT_SECRET=your-google-oauth-client-secret \
     FRONTEND_OAUTH_SUCCESS_URL=https://your-frontend-domain.com/ \
     GOOGLE_REDIRECT_URI=https://your-app-name.herokuapp.com/auth/oauth2/callback/google
   ```

6. **Deploy using Maven plugin**:
   ```bash
   mvn heroku:deploy -a your-app-name
   ```
   Or deploy using Git push (if you connect Heroku to GitHub):
   ```bash
   git push heroku main
   ```

7. **Verify deployment**:
   ```bash
   heroku logs -a your-app-name --tail
   ```
   Check logs for successful startup.

### Verify Deployment

Once deployed, test the health endpoint:

```bash
curl https://your-app-name.herokuapp.com/health
```

## Deployment to Railway

### Prerequisites

- Push code to GitHub (public or private repository).
- Set up a Railway project.

### Steps

1. **Add `backend/Dockerfile` to your repository** (already included).

2. **In Railway**:
   - Create a new **Service**.
   - Connect your GitHub repository.
   - Set the **Service Root** to `backend/` (or leave default and specify Dockerfile path: `backend/Dockerfile`).

3. **Configure Build & Start**:
   - **Builder**: Docker (Railway auto-detects `backend/Dockerfile`).
   - **Build Command**: (leave default; Railway uses the Dockerfile)
   - **Start Command**: (leave default; Dockerfile specifies ENTRYPOINT)

4. **Set Environment Variables** in Railway:
   ```
   SPRING_DATASOURCE_URL=jdbc:postgresql://your-supabase-host:5432/postgres
   SPRING_DATASOURCE_USERNAME=postgres
   SPRING_DATASOURCE_PASSWORD=your-supabase-password
   OPENROUTER_API_KEY=your-openrouter-api-key
   GOOGLE_CLIENT_ID=your-google-oauth-client-id
   GOOGLE_CLIENT_SECRET=your-google-oauth-client-secret
   FRONTEND_OAUTH_SUCCESS_URL=https://your-frontend-domain.com/
   GOOGLE_REDIRECT_URI=https://your-railway-backend-url/auth/oauth2/callback/google
   ```

5. **Deploy**:
   - Push changes to GitHub.
   - Railway automatically builds the Docker image and deploys.
   - View logs in the Railway dashboard.

### Verify Deployment

Once deployed, test the health endpoint:

```bash
curl https://your-railway-backend-url/health
```

## Environment Variables

The application reads configuration from environment variables with fallbacks to `application.properties`:

| Variable | Purpose | Example |
|----------|---------|---------|
| `DATABASE_URL` | PostgreSQL connection string (Heroku auto-sets this) | `postgresql://user:pass@host:5432/db` |
| `SPRING_DATASOURCE_URL` | PostgreSQL connection string (fallback) | `jdbc:postgresql://host:5432/db` |
| `SPRING_DATASOURCE_USERNAME` | DB user | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | `secure-password` |
| `OPENROUTER_API_KEY` | OpenRouter API key for LLM calls | (from OpenRouter dashboard) |
| `HTTP_PROXY` / `HTTPS_PROXY` | Optional outbound proxy URL in the form `http://proxy.example.com:3128` or `http://user:pass@proxy.example.com:3128` | Use when your environment requires a proxy for internet access |
| `GOOGLE_CLIENT_ID` | Google OAuth client ID | (from Google Cloud Console) |
| `GOOGLE_CLIENT_SECRET` | Google OAuth client secret | (from Google Cloud Console) |
| `FRONTEND_OAUTH_SUCCESS_URL` | Frontend URL after OAuth login | `https://app.example.com/` |
| `GOOGLE_REDIRECT_URI` | OAuth callback URL | `https://api.example.com/auth/oauth2/callback/google` |

**Never commit API keys or passwords to the repository.** Use environment variables in production.

### Starting locally behind a proxy

Windows PowerShell:
```powershell
$env:OPENROUTER_API_KEY = "<your-openrouter-api-key>"
$env:HTTPS_PROXY = "http://proxy.example.com:3128"
$env:HTTP_PROXY = "http://proxy.example.com:3128"
.\mvnw spring-boot:run
```

Windows cmd.exe:
```cmd
set OPENROUTER_API_KEY=<your-openrouter-api-key>
set HTTPS_PROXY=http://proxy.example.com:3128
set HTTP_PROXY=http://proxy.example.com:3128
.\mvnw spring-boot:run
```

If your proxy requires authentication, include credentials in the URL:
`http://user:password@proxy.example.com:3128`

## Architecture

- **Framework**: Spring Boot 3.2.4
- **JDK**: Java 21
- **Database**: PostgreSQL (via Supabase or self-hosted)
- **Migrations**: Flyway (auto-applied on startup)
- **API**: RESTful (Spring Web MVC)
- **Authentication**: Google OAuth2, JWT tokens
- **LLM Integration**: OpenRouter API for multi-model routing

## Project Structure

```
backend/
├── Dockerfile           # Multi-stage build for containerization
├── pom.xml              # Maven build configuration
├── src/
│   ├── main/java/com/ronin/
│   │   ├── RoninApplication.java
│   │   ├── auth/              # Authentication & OAuth
│   │   ├── code/              # Code generation & execution
│   │   ├── config/            # Spring configuration
│   │   ├── llm/               # LLM provider integrations
│   │   ├── projects/          # Project CRUD & management
│   │   ├── ranking/           # User ranking & belt system
│   │   └── users/             # User management
│   ├── main/resources/
│   │   ├── application.properties  # Configuration with env-var fallbacks
│   │   └── db/migration/           # Flyway SQL migration scripts
│   └── test/java/com/ronin/        # Unit and integration tests
```

## Troubleshooting

### `mvn: command not found`

Ensure Maven is installed or use the Maven wrapper:

```bash
./mvnw clean package
```

### `JAVA_HOME is not defined`

Set `JAVA_HOME` environment variable to your JDK installation path:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk  # Linux
export JAVA_HOME=$(/usr/libexec/java_home)     # macOS
```

Or use Docker, which includes Java automatically.

### Database connection timeout

Verify:
- PostgreSQL is running (or Supabase is accessible).
- Credentials in environment variables are correct.
- Firewall allows outbound connections to the database host.
- For Supabase: use the connection string from the "Connect" tab (not the pooler, unless using Supabase's pooler).

## Support

For issues, check logs:

```bash
# Local
tail -f backend.log

# Railway
# View in Railway dashboard → Logs tab
```

Contact the Ronin team or open an issue on GitHub.
