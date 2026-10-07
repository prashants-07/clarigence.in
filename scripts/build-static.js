const fs = require('node:fs');
const path = require('node:path');

const root = path.resolve(__dirname, '..');
const output = path.join(root, 'dist');
const publicFiles = [
  'index.html',
  'about.html',
  'services.html',
  'solutions.html',
  'portfolio.html',
  'contact.html',
  'styles.css',
  'script.js',
  'api-config.js',
  'sitemap.xml',
  'robots.txt',
  'favicon.ico',
];

const localApiUrl = 'http://localhost:8081/api/contact';
const configuredApiUrl = process.env.CLARIGENCE_API_URL?.trim();
let apiUrl = localApiUrl;

// Validate deployment configuration before replacing the existing output.
if (configuredApiUrl) {
  let parsedUrl;
  try {
    parsedUrl = new URL(configuredApiUrl);
  } catch {
    throw new Error('CLARIGENCE_API_URL must be an absolute HTTPS URL.');
  }
  if (parsedUrl.protocol !== 'https:' || /^(localhost|127\.0\.0\.1|\[::1\])$/i.test(parsedUrl.hostname)) {
    throw new Error('CLARIGENCE_API_URL must use HTTPS and point to your deployed backend, not localhost.');
  }
  if (parsedUrl.username || parsedUrl.password || parsedUrl.search || parsedUrl.hash) {
    throw new Error('CLARIGENCE_API_URL must not contain credentials, a query string, or a fragment.');
  }
  if (!parsedUrl.pathname.replace(/\/$/, '').endsWith('/api/contact')) {
    throw new Error('CLARIGENCE_API_URL must end in /api/contact.');
  }
  apiUrl = parsedUrl.toString().replace(/\/$/, '');
} else if (process.env.VERCEL === '1' || process.env.VERCEL_ENV) {
  throw new Error('Set CLARIGENCE_API_URL to your deployed HTTPS backend endpoint ending in /api/contact, then redeploy. A live contact form cannot use localhost.');
}

fs.rmSync(output, { recursive: true, force: true });
fs.mkdirSync(output, { recursive: true });

for (const file of publicFiles) {
  const source = path.join(root, file);
  if (!fs.existsSync(source)) {
    throw new Error(`Required public file is missing: ${file}`);
  }
  fs.copyFileSync(source, path.join(output, file));
}

const assetsSource = path.join(root, 'assets');
if (!fs.existsSync(assetsSource)) {
  throw new Error('Required public assets folder is missing: assets');
}
fs.cpSync(assetsSource, path.join(output, 'assets'), { recursive: true });

const configPath = path.join(output, 'api-config.js');
const configSource = fs.readFileSync(configPath, 'utf8');
if (!configSource.includes(localApiUrl)) {
  throw new Error('Could not find the expected local contact endpoint in api-config.js.');
}
fs.writeFileSync(configPath, configSource.replace(localApiUrl, apiUrl));

if (!configuredApiUrl) console.log('Local build: contact form uses the backend on port 8081.');

console.log(`Prepared static Vercel output in ${path.relative(root, output)}.`);
