// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
    compatibilityDate: '2025-07-15',
    devtools: {enabled: false},
    modules: [
      '@nuxtjs/color-mode',
      '@nuxtjs/i18n',
      '@pinia/nuxt',
      'pinia-plugin-persistedstate/nuxt',
      '@nuxtjs/sitemap',
      '@nuxtjs/robots',
      '@nuxt/icon'
    ],
    site: {
        url: 'https://auth.tecgui.cn',
        name: 'Codelin Auth Center'
    },
    sitemap: {
        zeroRuntime: true,
        cacheMaxAgeSeconds: 6 * 60 * 60, //6小时缓存
        autoLastmod: true, // 用于爬虫抓取
        exclude: ['/secret/**',"/admin/**","/user/**"],
    },
    icon: {
        serverBundle: {
            collections: ['mdi', 'carbon', 'simple-icons']
        },
        provider: 'server',
    },
    piniaPluginPersistedstate: {
        storage: 'cookies',
        key: 'local_store_%id',
        cookieOptions: {
            sameSite: 'lax',
        },
        debug: false,
    },

    css: ['~/assets/css/main.css', '~/assets/css/apple.css'],

    postcss: {
        plugins: {
            '@tailwindcss/postcss': {},
        },
    },

    colorMode: {
        classSuffix: '',
        preference: 'system',
        fallback: 'light',
    },

    i18n: {
        strategy: 'no_prefix',
        locales: [
            {code: 'zh-CN', name: '中文', file: 'zh-CN.json'},
            {code: 'en', name: 'English', file: 'en.json'},
        ],
        defaultLocale: 'zh-CN',
        detectBrowserLanguage: false,
    },

    robots: {
        disallow: ["/admin", "/user"]
    },

    app: {
        pageTransition: { name: 'page', mode: 'out-in' },
        head: {
            title: 'Codelin 认证中心',
            htmlAttrs: {
                lang: 'zh-CN',
            },
            meta: [
                { charset: 'utf-8' }, // 字符集
                {name: 'viewport', content: 'width=device-width, initial-scale=1'},
                { name: 'viewport', content: 'width=device-width, initial-scale=1' },
                {
                    name: 'description',
                    content: '覆盖Oauth，SSO登录的统一认证中心'
                },
            ],
            link: [
                { rel: 'icon', type: 'image/x-icon', href: '/favicon.ico' },
                { rel: 'canonical', href: 'https://tecgui.cn/' }
            ],
        },
    },

    vite: {
        build:{
            sourcemap:'hidden'
        },
        terserOptions: {
            compress: {
                drop_console: true,
            },
        },
        server: {
            allowedHosts: true,
            proxy: {
                '/api': {
                    target: 'http://localhost:8080',
                    changeOrigin: true,
                    rewrite: (path) => path.replace(/^\/api/, '')
                }
            }
        },
        optimizeDeps: {
            include: ['@vueuse/core', 'axios'],
        },
    },
    nitro: {
        externals: {
            inline: ['vue'],
        },
    },
    build: {
        transpile: ['@vueuse/core', 'axios'],
    },
})