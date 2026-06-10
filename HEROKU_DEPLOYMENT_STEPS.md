# Heroku Deployment Steps for Ronin Monorepo

This repository is a monorepo. The backend and frontend apps must be deployed separately using `git subtree push`.

## 1. Commit all changes

```powershell
cd C:\cygwin64\home\BTCES\roninaiagent
git checkout master
git add .
git commit -m "Deploy backend and frontend to Heroku with subtree push"
```

## 2. Deploy backend

```powershell
git subtree push --prefix backend heroku-backend master:main
```

This pushes only the `backend/` subtree to the `ronin-backend` Heroku app. Heroku will see `backend/pom.xml` as the root of the pushed source.

## 3. Deploy frontend

```powershell
git subtree push --prefix frontend heroku-frontend master:main
```

This pushes only the `frontend/` subtree to the `ronin-frontend` Heroku app.

## 4. Use the helper script

If you prefer an automated helper, run:

```bash
./deploy-heroku-helper.sh
```

The helper script will:
- verify a clean git working tree
- configure Heroku remotes
- set `heroku/java` for the backend app
- set `heroku/nodejs` for the frontend app
- deploy each subtree with `master:main`

## 5. Verify buildpacks (optional)

## 4. Verify buildpacks (optional)

If you need to enforce the correct buildpack:

```powershell
heroku buildpacks:set heroku/java --app ronin-backend
heroku buildpacks:set heroku/nodejs --app ronin-frontend
```

## 5. Check logs

```powershell
heroku logs --app ronin-backend --tail
heroku logs --app ronin-frontend --tail
```

## 6. Notes

- Do not use `git push heroku-backend main` or `git push heroku-frontend main` from the repo root.
- The root repository directory does not contain `pom.xml`, so pushing the root will fail for the Java app.
- Use `master:main` mapping because the Heroku apps are configured to deploy from the remote `main` branch.
