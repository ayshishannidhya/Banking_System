const routes = {};
let currentCleanup = null;

export function registerRoute(path, handler) {
    routes[path] = handler;
}

export function navigate(path) {
    window.location.hash = `#${path}`;
}

export async function handleRoute() {
    const hash = window.location.hash.slice(1) || '/login';
    const [path, ...rest] = hash.split('?');
    const params = new URLSearchParams(rest.join('?'));

    if (currentCleanup) {
        currentCleanup();
        currentCleanup = null;
    }

    const handler = routes[path];
    if (handler) {
        const cleanup = await handler(params);
        if (typeof cleanup === 'function') {
            currentCleanup = cleanup;
        }
    } else {
        const app = document.getElementById('app');
        app.innerHTML = `
            <div class="auth-wrapper">
                <div class="auth-card" style="text-align:center">
                    <span class="material-icons-round" style="font-size:4rem;color:var(--text-muted)">explore_off</span>
                    <h2 style="margin:1rem 0 0.5rem">Page Not Found</h2>
                    <p style="color:var(--text-secondary);margin-bottom:1.5rem">The page you're looking for doesn't exist.</p>
                    <button class="btn btn-primary" onclick="location.hash='#/dashboard'">Go to Dashboard</button>
                </div>
            </div>
        `;
    }
}

export function startRouter() {
    window.addEventListener('hashchange', handleRoute);
    handleRoute();
}
