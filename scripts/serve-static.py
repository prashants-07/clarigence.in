"""Preview the built frontend with the same clean page URLs used on Vercel."""
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlsplit, urlunsplit
import argparse

ROOT = Path(__file__).resolve().parents[1] / 'dist'
PAGES = {'about', 'services', 'solutions', 'portfolio', 'contact'}

class Handler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(ROOT), **kwargs)

    def do_GET(self):
        url = urlsplit(self.path)
        path = url.path
        clean = '/' if path in {'/index', '/index.html', '/home'} else path
        if path.endswith('.html') and path[1:-5] in PAGES:
            clean = path[:-5]
        if path.endswith('/') and path.strip('/') in PAGES:
            clean = path.rstrip('/')
        if clean != path:
            self.send_response(308)
            self.send_header('Location', urlunsplit(('', '', clean, url.query, '')))
            self.end_headers()
            return
        if path.strip('/') in PAGES:
            self.path = urlunsplit(('', '', path + '.html', url.query, ''))
        super().do_GET()

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--port', type=int, default=8000)
    args = parser.parse_args()
    if not ROOT.is_dir():
        parser.error('Run node scripts/build-static.js first.')
    print(f'Frontend: http://localhost:{args.port}/', flush=True)
    ThreadingHTTPServer(('127.0.0.1', args.port), Handler).serve_forever()
