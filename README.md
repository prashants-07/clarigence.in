# Clarigence.in

The public site is a lightweight static website. The separate `backend` project runs the contact API and a protected admin dashboard.

The public shell now loads Services, Portfolio and managed page/settings content through the existing Spring Boot backend. The admin includes the CMS alongside the preserved enquiry tools. See [CMS-GUIDE.md](CMS-GUIDE.md) for APIs, initialization, security, deployment configuration and verification.

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

Both homepage and contact-page enquiry forms use the single endpoint in `api-config.js`: `http://localhost:8081/api/contact`. For a live site, configure the Vercel environment variable `CLARIGENCE_API_URL` as your deployed HTTPS endpoint ending in `/api/contact` and redeploy. The existing build validates this URL and replaces it only in `dist/api-config.js`; local source stays unchanged. Vercel builds fail if this variable is missing. Configure the backend CORS allowlist with your public frontend origin. The admin dashboard remains served by the backend.

Vercel’s direct CLI deployment avoids a GitHub push, while `.gitignore` excludes Maven’s regenerated `target` directory if you later put the project in Git. If the 57 MB JAR was already committed to a different Git repository’s history, ignoring it now will not remove the old commit; that Git history would need cleanup before a GitHub push.

## Run locally

Create the MySQL database and first admin account using [backend/README.md](backend/README.md), then run `powershell -NoProfile -ExecutionPolicy Bypass -File .\backend\start-local.ps1` and enter the database password privately. The admin dashboard runs at http://localhost:8081/admin/.

In a second Windows CMD window, start the public frontend:

```cmd
cd /d C:\Users\prash\Desktop\clarigence.in
py -m http.server 8000
```

Visit http://localhost:8000. The contact form submits to the backend at http://localhost:8081/api/contact.

## Public design and content

The six existing public HTML pages share a lightweight stylesheet and JavaScript, with no runtime framework or added production dependencies. Existing logo and favicon assets are preserved. Service details live at anchors on `services.html`; links from those details preselect a service on the contact page. Portfolio items are explicitly labeled illustrative concepts.

Edit shared page content in `scripts/generate-pages.py`, then regenerate the six HTML pages with `py scripts/generate-pages.py`. CSS and JavaScript remain independently editable. This optional authoring script is not needed to serve or deploy the website.

Build locally with `node scripts/build-static.js`. Canonicals and `sitemap.xml` use `https://www.clarigence.in/`; update these together if the primary public domain changes. Keep the existing backend deployment, MySQL configuration and admin credentials unchanged.

Browser checks: `node scripts/verify-frontend.cjs` requires Playwright in the ignored `.qa-tools` directory and the frontend running on port 8000. This is a testing tool only. Results and screenshots are written to ignored `artifacts/qa/`.
