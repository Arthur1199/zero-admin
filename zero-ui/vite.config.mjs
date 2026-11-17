import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import { fileURLToPath } from 'node:url'
import { gzipSync } from 'node:zlib'
import { mkdirSync, writeFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'

// 保留原gzip副本，代理仍向未压缩文件回退。
function gzipAssets() {
  let output
  return {
    name: 'zero-gzip',
    configResolved(config) { output = resolve(config.root, config.build.outDir) },
    writeBundle(_, bundle) {
      for (const [name, asset] of Object.entries(bundle)) {
        if (!/\.(js|css|html|jpe?g|png|gif|svg)$/.test(name)) continue
        const source = Buffer.from(asset.type === 'chunk' ? asset.code : asset.source)
        const compressed = gzipSync(source)
        if (compressed.length / source.length >= 0.8) continue
        const target = resolve(output, name + '.gz')
        mkdirSync(dirname(target), { recursive: true })
        writeFileSync(target, compressed)
      }
    }
  }
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const prefix = env.VITE_APP_BASE_API || '/dev-api'
  const target = env.VITE_DEV_PROXY_TARGET || 'http://localhost:9099'
  const proxy = {
    [prefix]: {
      target, changeOrigin: true, ws: true,
      // WebSocket保留浏览器访问的Host，避免代理改Host后被后端同源校验误拒绝。
      configure: proxy => proxy.on('proxyReqWs', (proxyReq, request) => {
        if (request.headers.host) proxyReq.setHeader('host', request.headers.host)
      }),
      rewrite: path => path.startsWith(prefix) ? path.slice(prefix.length) : path
    },
    '/v3/api-docs': { target, changeOrigin: true }
  }
  return {
    base: '/',
    // 测试服务使用独立依赖缓存，避免与日常开发服务互相替换优化产物。
    cacheDir: process.env.ZERO_VITE_CACHE_DIR || 'node_modules/.vite',
    plugins: [vue(), vueJsx(), gzipAssets()],
    resolve: { alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) }, extensions: ['.mjs', '.js', '.jsx', '.json', '.vue'] },
    // 浏览器截图、trace和报告不参与热更新，避免测试产物持续写入拖慢开发页面。
    server: { host: '0.0.0.0', port: Number(process.env.port || process.env.npm_config_port || env.VITE_DEV_PORT || 80), open: env.VITE_DEV_OPEN !== 'false', proxy, watch: { ignored: ['**/output/**'] } },
    preview: { host: '127.0.0.1', port: 9526, proxy },
    build: { outDir: 'dist', assetsDir: 'static', sourcemap: false, target: ['chrome111', 'edge111', 'firefox114', 'safari16.4', 'ios16.4'] },
    css: { preprocessorOptions: { scss: { silenceDeprecations: ['legacy-js-api', 'import', 'global-builtin', 'color-functions'] } } },
    test: { include: ['tests/unit/**/*.test.js'], environment: 'node' }
  }
})
