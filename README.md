# Safe File Deleter

A small full-stack demo application that lets you safely delete files inside a restricted "allowed root" directory. The backend exposes a single REST endpoint that validates every path (no traversal, no symlinks, regular files only) before deleting, and the frontend provides a simple UI for submitting paths and viewing the result.

## Stack

Spring Boot 3.2 / Java 17 / React 18 / Vite 5

## Layout

```
.
├── backend/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/example/demo/
│           │   ├── DemoApplication.java
│           │   └── controller/ApiController.java
│           └── resources/application.properties
└── frontend/
    ├── index.html
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── App.jsx
        ├── App.css
        └── main.jsx
```

## How to run

**Backend** (listens on `http://localhost:8080`):

```bash
cd backend
mvn spring-boot:run
```

**Frontend** (listens on `http://localhost:5173`, proxies `/api` to the backend):

```bash
cd frontend
npm install
npm run dev
```

Then open `http://localhost:5173` in your browser.

## API

### `POST /api/delete`

Deletes one or more files relative to the configured allowed root.

**Request body:**

```json
{
  "paths": ["tmp/a.txt", "tmp/b.txt"]
}
```

**Response body:**

```json
{
  "deleted": ["tmp/a.txt"],
  "errors": [
    { "path": "tmp/b.txt", "error": "not found" }
  ]
}
```

Each path is validated independently; a failure on one path does not abort the others. Per-path validation rules:

- Path must be non-blank.
- Resolved path must stay inside `app.allowed-root` (no `..` traversal).
- Symlinks are refused.
- Target must exist and be a regular file.

## Notes

- The allowed root is configured via `app.allowed-root` in `backend/src/main/resources/application.properties` (default `./allowed_root`, relative to the backend working directory). The directory must exist before use.
- There is **no authentication or authorization** — this is a demo. Do not expose it on a public network.
- Deletion is permanent (no trash/recycle bin, no undo).
- The frontend expects the backend on `localhost:8080`; change the proxy target in `frontend/vite.config.js` if you run the backend elsewhere.
- Only regular files are supported; directories are rejected.
- No persistence layer or database is used.