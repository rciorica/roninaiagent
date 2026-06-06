# Ronin - System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        USER APPLICATIONS                                    │
├──────────────────────────────┬────────────────────────────────────────────────┤
│   Web Browser                │         Windows Desktop App                    │
│   (Any Browser)              │         (Electron - Native)                    │
│                              │                                                │
│  • Chrome                    │  • Windows 10+                                 │
│  • Firefox                   │  • Installer Version                           │
│  • Safari                    │  • Portable Version                            │
│  • Edge                      │  • System Tray (Future)                        │
│  • Mobile Browser            │  • Offline Support (Future)                    │
│                              │                                                │
│  URL: http://app.ronin.dev   │  File: Ronin-{version}-Setup.exe              │
│       or localhost:5173      │       or Ronin-{version}.exe                   │
└──────────────────────────────┴────────────────────────────────────────────────┘
                                    │
                                    │ HTTP/REST
                                    ▼
        ┌───────────────────────────────────────────────────┐
        │     FRONTEND LAYER (React + TypeScript)           │
        ├───────────────────────────────────────────────────┤
        │                                                   │
        │  ┌─────────────┐  ┌──────────────┐              │
        │  │  Pages      │  │  Components  │              │
        │  ├─────────────┤  ├──────────────┤              │
        │  │ Dashboard   │  │ Header       │              │
        │  │ Login       │  │ Sidebar      │              │
        │  │ Signup      │  │ ProjectCard  │              │
        │  │ Projects    │  │ BeltBadge    │              │
        │  │ Profile     │  │ etc.         │              │
        │  └─────────────┘  └──────────────┘              │
        │                                                   │
        │  ┌──────────────────────────────────────────┐   │
        │  │   React Router                           │   │
        │  │   Navigation & State Management          │   │
        │  └──────────────────────────────────────────┘   │
        │                                                   │
        │  ┌──────────────────────────────────────────┐   │
        │  │   HTTP Client (Fetch / Axios)            │   │
        │  │   API Integration                        │   │
        │  └──────────────────────────────────────────┘   │
        │                                                   │
        └────────────┬──────────────────────────────────────┘
                     │
                     │ Electron IPC Bridge (Desktop Only)
                     │ ├─ getAppVersion()
                     │ ├─ checkBackendHealth()
                     │ └─ getAppPath()
                     │
                     ├─────────────────────────────────────────┐
                     │                                         │
        ┌────────────▼─────────────┐         ┌────────────────▼─────┐
        │  Main Renderer Process    │         │  Electron Main       │
        │  (Web View)               │         │  (Node.js)           │
        │                           │         │                      │
        │  ┌──────────────────────┐ │         │ ┌──────────────────┐ │
        │  │ React Virtual DOM    │ │         │ │ Window Manager   │ │
        │  │ Event Handlers       │ │         │ │ Menu Handling    │ │
        │  │ Local Storage        │ │         │ │ System Tray      │ │
        │  │ IndexedDB            │ │         │ │ IPC Server       │ │
        │  └──────────────────────┘ │         │ └──────────────────┘ │
        │                           │         │                      │
        └───────────────────────────┘         └──────────────────────┘
                     │
                     │ HTTP/REST Calls
                     ▼
    ┌───────────────────────────────────────────────────────────┐
    │   API GATEWAY & REVERSE PROXY                             │
    ├───────────────────────────────────────────────────────────┤
    │  (Spring Boot Application Server)                         │
    │  ┌─────────────────────────────────────────────────────┐ │
    │  │  Request Routing & Load Balancing                   │ │
    │  └─────────────────────────────────────────────────────┘ │
    └──────────┬──────────────────────────┬────────────────────┘
               │                          │
               │                          │
    ┌──────────▼──────────┐     ┌─────────▼──────────────┐
    │  AUTHENTICATION     │     │   BUSINESS LOGIC       │
    │  & AUTHORIZATION    │     │   LAYER                │
    ├────────────────────┤     ├────────────────────────┤
    │                    │     │                        │
    │ • JWT Tokens       │     │ • Project Controller   │
    │ • Session Mgmt     │     │ • User Controller      │
    │ • RBAC             │     │ • LLM Controller       │
    │ • OAuth2 (Future)  │     │ • Test Controller      │
    │                    │     │ • Ranking Service      │
    │ Controllers:       │     │                        │
    │ • AuthController   │     │ Services:              │
    │                    │     │ • ProjectService       │
    │                    │     │ • UserService          │
    │                    │     │ • LLMService           │
    │                    │     │ • TestRunService       │
    │                    │     │ • RankingService       │
    │                    │     │                        │
    └────────────────────┘     └────────────────────────┘
               │                          │
               │                          │
    ┌──────────▼──────────────────────────▼──────────┐
    │   DATA ACCESS LAYER (Repository Pattern)       │
    ├───────────────────────────────────────────────┤
    │                                               │
    │  • UserRepository                             │
    │  • ProjectRepository                          │
    │  • LLMProviderRepository                       │
    │  • ProjectTestRunRepository                    │
    │  • ProjectMessageRepository                    │
    │  • ProjectArtifactRepository                   │
    │  • RankRepository                              │
    │                                               │
    │  ORM: Hibernate / JPA                          │
    │  Query Language: HQL / SQL                     │
    │                                               │
    └──────────────────┬──────────────────────────┘
                       │
                       │ SQL Queries
                       ▼
    ┌───────────────────────────────────────────────┐
    │   DATABASE LAYER                              │
    ├───────────────────────────────────────────────┤
    │                                               │
    │  Primary: MySQL / PostgreSQL                  │
    │  (Configurable)                               │
    │                                               │
    │  Tables:                                      │
    │  ├─ users                                     │
    │  ├─ ranks (kyu/dan system)                    │
    │  ├─ projects                                  │
    │  ├─ llm_providers                             │
    │  ├─ user_llm_usage                            │
    │  ├─ project_test_runs                         │
    │  ├─ project_messages                          │
    │  ├─ project_artifact_files                    │
    │  └─ project_message_attachments               │
    │                                               │
    │  Migrations: Flyway                           │
    │  Location: db/migration/                      │
    │                                               │
    └───────────────────────────────────────────────┘
```

## Component Details

### Frontend (React + Vite)

**Purpose**: User interface for both web and desktop applications

**Technologies**:
- React 19 - UI framework
- TypeScript - Type safety
- Vite - Fast bundler
- Tailwind CSS - Styling
- React Router - Navigation
- Vitest - Testing

**Key Components**:
```
src/
├── pages/
│   ├── Dashboard.tsx       # Project overview
│   ├── Login.tsx           # Authentication
│   ├── Signup.tsx          # User registration
│   └── Projects.tsx        # Project management
├── components/
│   ├── Header.tsx          # Navigation bar
│   ├── Sidebar.tsx         # Side navigation
│   ├── ProjectCard.tsx     # Project display
│   ├── BeltBadge.tsx       # Rank display
│   └── ...
├── layout/
│   └── MainLayout.tsx      # Common layout
├── api.ts                  # HTTP client
└── main.tsx                # Entry point
```

### Desktop (Electron)

**Purpose**: Native Windows desktop application wrapping React frontend

**Technologies**:
- Electron 33 - Desktop framework
- Node.js - Main process
- electron-builder - Packaging
- NSIS - Windows installer

**Structure**:
```
desktop/
├── electron/
│   ├── main.ts             # Main process entry
│   ├── window.ts           # Window creation
│   ├── preload.ts          # IPC bridge
│   └── utils.ts            # Helpers
├── build/                  # Assets & icons
├── installers/             # NSIS scripts
└── dist/                   # Build output
```

**Installer Details**:
- Full installer (Setup.exe): NSIS-based with uninstaller
- Portable executable: Single .exe file
- Bundles all dependencies
- Creates Start Menu and Desktop shortcuts

### Backend (Java + Spring Boot)

**Purpose**: RESTful API and business logic server

**Architecture Layers**:

1. **Controller Layer** - HTTP request handling
   - Validates requests
   - Calls services
   - Returns JSON responses
   - Authentication checks

2. **Service Layer** - Business logic
   - User management
   - Project management
   - LLM integration
   - Test execution
   - Ranking calculations

3. **Repository Layer** - Data access
   - CRUD operations
   - Query execution
   - Entity mapping
   - Transaction management

4. **Database Layer** - Data persistence
   - MySQL/PostgreSQL
   - Flyway migrations
   - Entity relationships

**Key Endpoints**:
```
Authentication:
  POST   /auth/register          # User registration
  POST   /auth/login             # User login
  POST   /auth/logout            # User logout
  GET    /auth/me                # Current user

Users:
  GET    /users/{id}             # Get user details
  PUT    /users/{id}             # Update user
  GET    /users/{id}/rank        # Get user rank

Projects:
  GET    /projects               # List projects
  POST   /projects               # Create project
  GET    /projects/{id}          # Get project details
  PUT    /projects/{id}          # Update project
  DELETE /projects/{id}          # Delete project

LLM Providers:
  GET    /llm/providers          # List providers
  GET    /llm/providers/{id}/usage # Get usage stats
  POST   /llm/switch             # Switch provider

Tests:
  POST   /tests/{projectId}/run  # Run tests
  GET    /tests/{runId}/results  # Get results
  GET    /tests/{projectId}/history # Test history
```

## Data Flow Examples

### User Registration Flow
```
1. User enters details in Signup.tsx
2. Frontend: validateForm() → POST /auth/register
3. Backend: AuthController receives request
4. Backend: validates input
5. Backend: UserService.register() → creates user
6. Backend: UserRepository.save() → inserts to DB
7. Database: INSERT user row
8. Response: JWT token
9. Frontend: stores token, redirects to Dashboard
```

### Project Creation Flow
```
1. User clicks "Create Project" on Dashboard
2. Frontend: POST /projects with project details
3. Backend: ProjectController receives request
4. Backend: ProjectService.create()
5. Backend: ProjectRepository.save() → inserts project
6. Database: INSERT project row
7. Backend: initializes test runner
8. Response: project details with ID
9. Frontend: adds to project list, navigates to project
```

### LLM Token Consumption Flow
```
1. Project needs AI assistance
2. Backend: checks LLMService for available tokens
3. Backend: UserLLMUsageService.getAvailable()
4. If tokens available:
   a. Makes API call to LLM provider
   b. Receives response
   c. Updates usage in database
   d. Returns to frontend
5. If no tokens:
   a. Switches to next provider (if available)
   b. Logs switch event
   c. Notifies user with warning
   d. Continues with new provider
```

## Deployment Targets

### Web Application
- **Production**: Heroku
- **URL**: https://app.ronin.dev (example)
- **Hosting**: Cloud (Heroku, AWS, etc.)
- **Scaling**: Horizontal (multiple dynos)
- **CDN**: For static assets
- **Database**: Cloud-hosted MySQL/PostgreSQL

### Desktop Application
- **Distribution**: GitHub Releases
- **Installer**: Windows installer (.exe)
- **Updates**: Local file downloads (future: electron-updater)
- **Backend Connection**: Localhost:8080
- **Supported**: Windows 10 and later

## Security Architecture

```
Frontend → Backend Communication
├── HTTPS/HTTP
├── CORS headers
├── Request validation
└── Authentication tokens

Backend → Database
├── Prepared statements (SQL injection protection)
├── Transaction management
├── Connection pooling
└── Encryption at rest (future)

User Data Protection
├── JWT tokens
├── Bcrypt password hashing
├── RBAC implementation
└── Audit logging (future)
```

## Performance Optimization

### Frontend
- Code splitting with React.lazy()
- Image optimization
- CSS minification via Tailwind
- Bundle size monitoring
- Lazy loading of routes

### Backend
- Connection pooling (HikariCP)
- Query optimization with JPA
- Caching strategies
- Database indexing
- Pagination for large datasets

### Desktop
- Lazy loading of modules
- Electron native modules
- Memory management
- Startup optimization

## Monitoring & Observability

```
Frontend:
├── Browser console logs
├── Network tab (DevTools)
├── Performance metrics
└── Error tracking (Future: Sentry)

Backend:
├── Spring Boot Actuator
├── Application logs
├── Database query logs
├── API response times

Desktop:
├── Main process logs
├── IPC communication logs
├── DevTools console
└── Crash reporting (Future)
```

## Future Architecture Enhancements

1. **Microservices**
   - Separate LLM service
   - Separate test runner
   - Separate ranking engine

2. **Message Queue**
   - RabbitMQ/Kafka for async tasks
   - Event-driven architecture

3. **Caching Layer**
   - Redis for session cache
   - Distributed caching

4. **Search Engine**
   - Elasticsearch for project search
   - Full-text search capability

5. **Analytics**
   - User behavior tracking
   - Performance monitoring
   - Business intelligence

6. **CI/CD Pipeline**
   - Automated testing
   - Automated deployment
   - Release management
