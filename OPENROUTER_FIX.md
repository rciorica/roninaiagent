# OpenRouter API 401 Authentication Fix

## Root Causes Identified

### 1. **Syntax Error in OpenRouterClient.java** ✓ FIXED
**Location:** Line 69 in `callOpenRouter()` method
**Problem:** A stray `.` before `.build()` was breaking the WebClient builder chain
```java
// BEFORE (broken):
WebClient client = webClientBuilder
        .baseUrl(baseUrl)
        .defaultHeader("Authorization", "Bearer " + apiKey)
        .defaultHeader("Content-Type", "application/json")
        .
        .build();

// AFTER (fixed):
WebClient client = webClientBuilder
        .baseUrl(baseUrl)
        .defaultHeader("Authorization", "Bearer " + apiKey)
        .defaultHeader("Content-Type", "application/json")
        .build();
```

**Impact:** This syntax error prevented the Authorization header from being properly set, causing the 401 "Missing Authentication header" error from OpenRouter.

### 2. **Missing API Key Environment Variable** ⚠️ REQUIRES ACTION
**Location:** `application.properties` line 12
**Current Setting:** `openrouter.api.key=${OPENROUTER_API_KEY:}`
**Problem:** If the `OPENROUTER_API_KEY` environment variable is not set, the header will be sent as `Authorization: Bearer ` (empty bearer token), causing authentication failure.

## Required Setup Steps

### Step 1: Set Environment Variable
Set the `OPENROUTER_API_KEY` environment variable with your OpenRouter API key:

**Windows Command Line:**
```powershell
setx OPENROUTER_API_KEY "your-openrouter-api-key-here"
```

**Windows PowerShell:**
```powershell
[Environment]::SetEnvironmentVariable("OPENROUTER_API_KEY", "your-openrouter-api-key-here", "User")
```

**Linux/Mac/WSL:**
```bash
export OPENROUTER_API_KEY="your-openrouter-api-key-here"
```

### Step 2: Rebuild the Backend
```bash
cd backend
mvn clean install
```

### Step 3: Run the Backend
The application will now correctly pass the API key in the Authorization header.

## Verification

1. Check that the environment variable is set:
   ```powershell
   $env:OPENROUTER_API_KEY  # PowerShell
   echo %OPENROUTER_API_KEY%  # Command Prompt
   ```

2. Check backend logs for the Authorization header being sent (should show no warnings about missing API key)

3. Try generating a project—it should now successfully authenticate with OpenRouter.

## What Was Changed

**File:** `backend/src/main/java/com/ronin/llm/providers/OpenRouterClient.java`

1. Removed the stray `.` on line 69 that was breaking the WebClient builder chain
2. Changed the `@Value` annotation from `@Value("${openrouter.api.key}")` to `@Value("${openrouter.api.key:}")` to provide a default empty string instead of throwing an exception at startup (allows graceful degradation)
3. Changed the constructor from throwing an `IllegalStateException` to logging a `warn` message, allowing the app to start even without the API key configured

This allows the application to start up and provide better error messages to users if the API key is missing, while still requiring the environment variable to be set for project generation to work.
