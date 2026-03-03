# MicroFramework Web

---

## Project Description

MicroFramework Web is a minimal HTTP server framework built from raw Java sockets, developed as a university workshop project for the *Transformaciones Digitales* course.

The goal of the project is to understand how web frameworks work under the hood by implementing one from scratch — without using any external libraries or HTTP server dependencies. It covers the full lifecycle of an HTTP request: accepting a TCP connection, parsing the request line and headers, routing to a registered handler, and writing a well-formed HTTP response back to the client.

The framework supports:

- **Endpoint routing** via a simple `get(path, handler)` API backed by a `HashMap`
- **Lambda-based handlers** through the `WebMethod` functional interface
- **Query parameter parsing** with full URL-decoding (`%2C` → `,`, `%20` → space, etc.)
- **Static file serving** loaded from the classpath (HTML, CSS, JS, JSON)
- **Binary asset delivery** (PNG, JPEG, GIF, SVG, WebP, ICO) by writing directly to the raw `OutputStream`, avoiding the text corruption that `PrintWriter` would introduce
- **Mutable per-request `Response`** object to control status code and content type from within a handler

Two example applications are included to demonstrate real usage of the framework: `MathServices` (mathematical constants) and `GradeService` (a university grade calculator).

---

## Table of Contents

1. [Project Description](#project-description)
2. [Architecture](#architecture)
3. [Project Structure](#project-structure)
4. [Build & Run](#build--run)
5. [Framework API](#framework-api)
6. [Example Applications](#example-applications)
7. [Tests & Expected Results](#tests--expected-results)
8. [Authors](#authors)

---

## Architecture

```
Browser / curl
      │  HTTP/1.1 request (raw TCP)
      ▼
┌─────────────────────────────────────────────────────┐
│                    HttpServer                        │
│                                                     │
│  1. Accept TCP connection on port 35000             │
│  2. Parse request line  →  method, path, query      │
│  3. URL-decode query params  (Request)              │
│  4. Look up path in endPoints map                   │
│                                                     │
│      ┌──────────────┐    ┌────────────────────┐     │
│      │  endPoints   │    │  staticfiles dir   │     │
│      │  Map<String, │    │  (classpath root)  │     │
│      │  WebMethod>  │    │                    │     │
│      └──────┬───────┘    └────────┬───────────┘     │
│             │ handler found?      │ file found?      │
│             ▼                     ▼                  │
│       WebMethod.handle()    read raw bytes           │
│       returns HTML string   (text or binary)         │
│             │                     │                  │
│             └──────────┬──────────┘                  │
│                        ▼                             │
│              buildResponse(status,                   │
│                content-type, byte[])                 │
│                        │                             │
│              write headers + body                    │
│              directly to OutputStream                │
└────────────────────────┼────────────────────────────┘
                         │  HTTP/1.1 response (raw TCP)
                         ▼
                      Browser
```

### Key design decisions

| Decision | Reason |
|---|---|
| Raw `OutputStream` instead of `PrintWriter` | `PrintWriter` is text-only and corrupts binary files (images, fonts). Writing bytes directly handles both text and binary uniformly. |
| `WebMethod` as a `@FunctionalInterface` | Enables lambda-based endpoint registration, keeping application code concise. |
| Classpath resource loading | Static files are bundled inside the JAR and loaded with `ClassLoader.getResourceAsStream()`, avoiding filesystem path issues. |
| URL-decoding query params | The browser encodes special characters (`%2C` for `,`, `%20` for space). Decoding at parse time means handlers always receive clean strings. |

---

## Project Structure

```
src/
└── main/
    ├── java/org/example/
    │   ├── utilities/
    │   │   ├── HttpServer.java      # Core: socket loop, routing, response building
    │   │   ├── WebMethod.java       # @FunctionalInterface for endpoint handlers
    │   │   ├── Request.java         # Parsed request + URL-decoded query params
    │   │   ├── Response.java        # Mutable status & content-type for handlers
    │   │   ├── EchoServer.java      # Simple echo server (demo, port 35000)
    │   │   ├── EchoClient.java      # Echo client counterpart
    │   │   ├── URLReader.java       # HTTP client utility (fetch + print)
    │   │   └── URLParser.java       # URL parsing demo
    │   └── appexamples/
    │       ├── MathServices.java    # Example app: mathematical constants
    │       └── GradeService.java    # Example app: grade calculator
    └── resources/
        └── webroot/
            ├── public/              # Static files for MathServices
            │   ├── index.html
            │   ├── styles.css
            │   └── script.js
            └── grades/              # Static files for GradeService
                ├── index.html
                ├── style.css
                ├── app.js
                └── gato-razas-tipos.webp
```

---

## Build & Run

**Requirements:** Java 24, Maven 3.x

```bash
# Compile
mvn compile

# Package into a JAR
mvn package

# Run MathServices (mathematical constants demo)
mvn exec:java -Dexec.mainClass="org.example.appexamples.MathServices"

# Run GradeService (grade calculator demo)
mvn exec:java -Dexec.mainClass="org.example.appexamples.GradeService"

# Other utilities
mvn exec:java -Dexec.mainClass="org.example.utilities.EchoServer"
mvn exec:java -Dexec.mainClass="org.example.utilities.URLReader" -Dexec.args="http://example.com"
```

All servers listen on **port 35000**. Only one can run at a time.

---

## Framework API

### Registering endpoints

```java
import static org.example.utilities.HttpServer.get;
import static org.example.utilities.HttpServer.staticfiles;

// Serve static files from src/main/resources/webroot/public/
staticfiles("/webroot/public");

// Register a GET route
get("/hello", (req, res) -> {
    String name = req.getValues("name");   // query param
    return "Hello, " + name + "!";        // returned string becomes the <body>
});

// Start the server (blocks)
HttpServer.main(args);
```

### `Request`

| Method | Description |
|---|---|
| `req.getValues(key)` | Returns the URL-decoded value of a query parameter, or `""` if absent |
| `req.getQueryParam(key)` | Alias for `getValues` |
| `req.getQueryParams()` | Returns the full `Map<String, String>` of query params |
| `req.getMethod()` | HTTP method (`"GET"`, etc.) |
| `req.getPath()` | Request path (e.g. `"/hello"`) |
| `req.getHeader(name)` | Returns the value of an HTTP request header |

### `Response`

| Method | Description |
|---|---|
| `res.status(int)` | Set the HTTP status code (default: `200`) |
| `res.type(String)` | Set the `Content-Type` header (default: `"text/html"`) |

### Supported static file types

| Extension | Content-Type |
|---|---|
| `.html` | `text/html` |
| `.css` | `text/css` |
| `.js` | `application/javascript` |
| `.json` | `application/json` |
| `.png` | `image/png` |
| `.jpg` / `.jpeg` | `image/jpeg` |
| `.gif` | `image/gif` |
| `.svg` | `image/svg+xml` |
| `.webp` | `image/webp` |
| `.ico` | `image/x-icon` |
| other | `application/octet-stream` |

---

## Example Applications

### MathServices

Demonstrates simple constant-returning endpoints.

```java
staticfiles("/webroot/public");
get("/pi",    (req, res) -> "PI=" + Math.PI);
get("/Hello", (req, res) -> "Hello " + req.getValues("name"));
get("/euler", (req, res) -> "e= " + Math.E);
```

```bash
mvn exec:java -Dexec.mainClass="org.example.appexamples.MathServices"
```

Open `http://localhost:35000` for the interactive UI, or call endpoints directly.

---

### GradeService

Demonstrates query-parameter parsing, branching logic, and a richer frontend with an image.

```java
staticfiles("/webroot/grades");
get("/calificar", (req, res) -> { /* describe a single grade */ });
get("/promedio",  (req, res) -> { /* average of comma-separated grades */ });
get("/aprobo",    (req, res) -> { /* pass/fail check against 3.0 threshold */ });
```

```bash
mvn exec:java -Dexec.mainClass="org.example.appexamples.GradeService"
```

Open `http://localhost:35000` for the interactive UI.

**Grading scale (0.0 – 5.0)**

| Range | Description |
|---|---|
| 4.5 – 5.0 | Excelente |
| 4.0 – 4.4 | Sobresaliente |
| 3.5 – 3.9 | Bueno |
| 3.0 – 3.4 | Aceptable (aprobado) |
| 0.0 – 2.9 | Reprobado |

---

## Tests & Expected Results

Tests were performed manually using the browser and `curl`. The server listens on `http://localhost:35000`.

---

### Static file serving

| Request | Expected result |
|---|---|
| `GET /` | Serves `index.html` (200 OK, `text/html`) |
| `GET /styles.css` | Serves stylesheet (200 OK, `text/css`) |
| `GET /script.js` | Serves JavaScript (200 OK, `application/javascript`) |
| `GET /gato-razas-tipos.webp` | Serves image binary without corruption (200 OK, `image/webp`) |
| `GET /nonexistent` | Falls back to default "My Web Site" page |

```bash
curl -i http://localhost:35000/
# HTTP/1.1 200 OK
# Content-Type: text/html
# ...

curl -i http://localhost:35000/gato-razas-tipos.webp
# HTTP/1.1 200 OK
# Content-Type: image/webp
# Content-Length: <bytes>
# ...
```

---

### MathServices endpoints

#### `GET /pi`
```bash
curl http://localhost:35000/pi
```
```
PI=3.141592653589793
```

#### `GET /euler`
```bash
curl http://localhost:35000/euler
```
```
e= 2.718281828459045
```

#### `GET /Hello?name=Mundo`
```bash
curl "http://localhost:35000/Hello?name=Mundo"
```
```
Hello Mundo
```

#### `GET /Hello` (missing param)
```bash
curl http://localhost:35000/Hello
```
```
Hello
```

---

### GradeService endpoints

#### `GET /calificar?nota=4.5`
```bash
curl "http://localhost:35000/calificar?nota=4.5"
```
```
Nota: 4.5 → Excelente
```

#### `GET /calificar?nota=2.5`
```bash
curl "http://localhost:35000/calificar?nota=2.5"
```
```
Nota: 2.5 → Reprobado
```

#### `GET /calificar` (missing param)
```bash
curl http://localhost:35000/calificar
```
```
Falta el parámetro: ?nota=
```

#### `GET /promedio?notas=3.5,4.0,4.8`
```bash
curl "http://localhost:35000/promedio?notas=3.5,4.0,4.8"
```
```
Notas: 3.5  4.0  4.8
Promedio: 4.1 → Sobresaliente
```

#### `GET /promedio?notas=3.5,%204.2,%203.3` (URL-encoded commas from browser)
```bash
curl "http://localhost:35000/promedio?notas=3.5%2C%204.2%2C%203.3"
```
```
Notas: 3.5  4.2  3.3
Promedio: 3.67 → Bueno
```

#### `GET /aprobo?nota=3.5`
```bash
curl "http://localhost:35000/aprobo?nota=3.5"
```
```
Nota: 3.5 → ✔ APROBÓ (0.50 por encima del mínimo)
```

#### `GET /aprobo?nota=2.8`
```bash
curl "http://localhost:35000/aprobo?nota=2.8"
```
```
Nota: 2.8 → ✘ REPROBÓ (0.20 por debajo del mínimo)
```

#### `GET /aprobo?nota=6.0` (out of range)
```bash
curl "http://localhost:35000/aprobo?nota=6.0"
```
```
Nota fuera de rango (0.0 – 5.0): 6.0
```

---

## Constraints

- **Single-threaded** — one request is handled at a time; concurrent clients must wait.
- **Port 35000** is hardcoded across all server components.
- **GET only** — no POST body parsing or other HTTP methods.
- **No query-param multi-values** — duplicate keys overwrite each other in the map.
- `EchoServer` and `HttpServer` cannot run simultaneously (same port).

---

## Authors
 
William Hernandez 


