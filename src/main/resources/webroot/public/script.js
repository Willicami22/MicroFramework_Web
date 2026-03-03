const output = document.getElementById("output");

async function callEndpoint(path) {
    output.textContent = "Fetching " + path + " …";
    try {
        const res = await fetch(path);
        const text = await res.text();
        output.textContent = "[" + res.status + "] " + path + "\n\n" + text;
    } catch (err) {
        output.textContent = "Error: " + err.message;
    }
}

function callHello() {
    const name = document.getElementById("nameInput").value.trim();
    const path = name ? "/Hello?name=" + encodeURIComponent(name) : "/Hello";
    callEndpoint(path);
}
