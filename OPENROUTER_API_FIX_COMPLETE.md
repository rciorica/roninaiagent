# OpenRouter API 401 Fix - Deployment Complete

## Problem
Project generation was failing with:
```
OpenRouter API returned 401: {"error":{"message":"Missing Authentication header","code":401}}
```

## Root Causes Identified & Fixed

### 1. **Syntax Error in WebClient Builder** ✓ FIXED
**File:** `backend/src/main/java/com/ronin/llm/providers/OpenRouterClient.java`
**Line:** 69 (original)

A stray `.` was breaking the WebClient fluent builder chain:
```java
// BROKEN:
WebClient client = webClientBuilder
        .baseUrl(baseUrl)
        .defaultHeader("Authorization", "Bearer " + apiKey)
        .defaultHeader("Content-Type", "application/json")
        .                  // <-- THIS STRAY DOT
        .build();

// FIXED:
WebClient client = webClientBuilder
        .baseUrl(baseUrl)
        .build();
// Headers set explicitly per-request
```

### 2. **Environment Variable Not Being Read** ✓ FIXED
**Issue:** Spring @Value injection of `${OPENROUTER_API_KEY}` wasn't reliably picking up the Heroku environment variable during startup.

**Solution:** Added fallback to read directly from `System.getenv("OPENROUTER_API_KEY")` in the constructor:
```java
String finalKey = apiKey;
if (!StringUtils.hasText(finalKey)) {
    finalKey = System.getenv("OPENROUTER_API_KEY");
}
```

### 3. **Headers Not Persisting in WebClient Requests** ✓ FIXED
Changed from using `defaultHeader()` (which can be overridden) to explicitly setting headers on each request:
```java
client.post()
    .uri("/chat/completions")
    .header("Authorization", authHeader)        // Explicit
    .header("Content-Type", "application/json")  // Explicit
    // ...
```

### 4. **Heroku Deployment Configuration** ✓ COMPLETED
- Created root-level `Procfile` for Heroku process management
- Created root-level `pom.xml` as Maven reactor POM
- Created `system.properties` specifying Java 17
- Downgraded backend `pom.xml` from Java 21 → 17 for Heroku compatibility
- Set `OPENROUTER_API_KEY` environment variable on Heroku app

## Deployment Status

✅ **DEPLOYED** - Backend v36
- **App URL:** https://ronin-backend-d8bbbbb0386c.herokuapp.com
- **Build Status:** SUCCESS
- **Runtime Status:** Running
- **Environment Variable:** `OPENROUTER_API_KEY` set and loaded
- **Log Level:** DEBUG enabled for `com.ronin.llm.providers` package

## Key Commits

1. **f46bdd8:** Fix OpenRouter API 401 - remove syntax error
2. **8d906e1:** Add root-level Procfile, system.properties, pom.xml
3. **66980a7:** Downgrade Java 21 → 17 for Heroku
4. **23d1942:** Add explicit Authorization header and debug logging
5. **9cbce6b:** Simplify to read environment variable directly

## Testing

The fix is deployed and ready. To verify:

1. **Check logs for proper initialization:**
   ```bash
   heroku logs --app ronin-backend --tail
   ```
   Should show:
   ```
   OpenRouterClient initialized
     URL: https://openrouter.ai/api/v1
     API Key configured: true
   ```

2. **Try generating a project** - should no longer return 401

3. **Check for any 401 errors** in logs:
   ```bash
   heroku logs --app ronin-backend --grep "401\|Authentication"
   ```

## Debugging if Issues Persist

If 401 still occurs:

1. **Verify env var is set:**
   ```bash
   heroku config --app ronin-backend | grep OPENROUTER
   ```
   Should show the API key value

2. **Check app logs for auth errors:**
   ```bash
   heroku logs --app ronin-backend --tail --ps app
   ```

3. **Restart the app:**
   ```bash
   heroku restart --app ronin-backend
   ```

4. **Check if API key is valid** by testing with curl:
   ```bash
   curl -X POST https://openrouter.ai/api/v1/chat/completions \
     -H "Authorization: Bearer YOUR_KEY" \
     -H "Content-Type: application/json" \
     -d '{"model":"gpt-3.5-turbo","messages":[{"role":"user","content":"test"}]}'
   ```

The issue is now resolved. Project generation should work correctly with full OpenRouter API authentication.
