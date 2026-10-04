# Clarigence.in

The public site is a lightweight static website. The separate `backend` project runs the contact API and a protected admin dashboard.

## Deploy the public website to Vercel

Vercel can deploy this static site directly from your computer; GitHub is not required. The Vercel build copies only the six public pages, stylesheet, JavaScript, robots file, and public assets. The Spring Boot backend and its generated JAR are excluded from the Vercel upload.

1. Install Node.js if it is not installed, then open Windows CMD.
2. Install and sign in to the Vercel CLI:

   ```cmd
   npm install --global vercel
   vercel login
   ```

3. Deploy from the project directory:

   ```cmd
   cd /d C:\Users\prash\Desktop\clarigence.in
   vercel --prod
   ```

The first run links this folder to your Vercel project and asks for deployment settings. Keep the project root as the current directory. Vercel uses `vercel.json` to build the static site into `dist`.

### Contact form and admin dashboard

The contact form currently points to the local API at `http://localhost:8081/api/contact`. For a live site, deploy the Spring Boot backend and MySQL database to a Java-capable host separately, then configure the Vercel project environment variable `CLARIGENCE_API_URL` as the full HTTPS endpoint ending in `/api/contact` (for example, `https://your-api-domain.example/api/contact`) and redeploy. The build validates this URL and applies it only to the generated `dist/contact.html`; the local website source remains unchanged. Vercel builds fail if this variable is missing, preventing a deployment that sends enquiries to a visitor's localhost. Configure the backend CORS allowlist to include your Vercel domain. The admin dashboard is served by the backend, so it also becomes available at the backend host rather than as a standalone Vercel page.

Vercel’s direct CLI deployment avoids a GitHub push, while `.gitignore` excludes Maven’s regenerated `target` directory if you later put the project in Git. If the 57 MB JAR was already committed to a different Git repository’s history, ignoring it now will not remove the old commit; that Git history would need cleanup before a GitHub push.

## Run locally

Create the MySQL database and first admin account using [backend/README.md](backend/README.md), then run `powershell -NoProfile -ExecutionPolicy Bypass -File .\backend\start-local.ps1` and enter the database password privately. The admin dashboard runs at http://localhost:8081/admin/.

In a second Windows CMD window, start the public frontend:

```cmd
cd /d C:\Users\prash\Desktop\clarigence.in
py -m http.server 8000
```

Visit http://localhost:8000. The contact form submits to the backend at http://localhost:8081/api/contact.
