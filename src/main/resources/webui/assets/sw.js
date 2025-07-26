const CACHE_NAME = 'static-cache-v1';
const STATIC_ASSETS = [
    '/static/cp/webui/assets/page.js',
    '/static/cp/webui/assets/jquery-3.7.1.min.js',
    '/static/cp/webui/assets/dataTables.min.js',
    '/static/cp/webui/assets/dataTables.dataTables.min.css',
    '/static/cp/webui/assets/sweetalert2.js',
    '/static/cp/webui/assets/biliplus.min.css',
    '/static/cp/webui/assets/js.cookie.min.js'
];

// 安装阶段：缓存静态资源
self.addEventListener('install', event => {
    console.log('[SW] Install');
    event.waitUntil(
        caches.open(CACHE_NAME).then(cache => {
            console.log('[SW] Caching:', STATIC_ASSETS);
            return cache.addAll(STATIC_ASSETS);
        })
    );
    self.skipWaiting();
});

// 激活阶段：清理旧缓存
self.addEventListener('activate', event => {
    console.log('[SW] Activate');
    event.waitUntil(
        caches.keys().then(keys =>
        Promise.all(
            keys.map(key => {
                if (key !== CACHE_NAME) {
                    console.log('[SW] Deleting old cache:', key);
                    return caches.delete(key);
                }
            })
        )
        )
    );
    self.clients.claim();
});

// 只缓存列出的资源
self.addEventListener('fetch', event => {
    const url = new URL(event.request.url);
    const pathname = url.pathname;

    // 只处理 GET 并匹配到缓存列表的路径
    if (
    event.request.method === 'GET' &&
    STATIC_ASSETS.includes(pathname)
    ) {
        console.log('[SW] Cache-handled fetch:', pathname);

        event.respondWith(
            caches.match(event.request).then(cached => {
                if (cached) {
                    console.log('[SW] Cache hit:', pathname);
                    return cached;
                }

                return fetch(event.request).then(response => {
                    if (response.ok) {
                        // 可选：更新缓存
                        caches.open(CACHE_NAME).then(cache => {
                            cache.put(event.request, response.clone());
                            console.log('[SW] Updated cache:', pathname);
                        });
                    }
                    return response;
                }).catch(err => {
                    console.error('[SW] Fetch failed:', pathname, err);
                });
            })
        );
    } else {
        // 非缓存资源，直接放行，不处理
        // console.log('[SW] Skipping non-static fetch:', pathname);
        return;
    }
});
