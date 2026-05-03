<script setup lang="ts">

useHead({ title: 'API Playground — Auth Center' })

const api = useApi()
const { error: showError } = useToast()

const method = ref('GET')
const endpoint = ref('/v1/auth/config')
const headers = ref<Array<{ key: string; value: string }>>([{ key: '', value: '' }])
const bodyInput = ref('')
const response = ref<{ status: number; data: any; time: number } | null>(null)
const loading = ref(false)

const presets = [
  { label: '获取配置', method: 'GET', path: '/v1/auth/config' },
  { label: '密码登录', method: 'POST', path: '/v1/auth/login', body: '{\n  "username": "",\n  "password": ""\n}' },
  { label: 'Token 刷新', method: 'POST', path: '/v1/auth/refresh', body: '{\n  "refreshToken": ""\n}' },
  { label: '获取用户信息', method: 'GET', path: '/v1/auth/me' },
  { label: '发送邮箱验证码', method: 'POST', path: '/v1/auth/login/email/send', body: '{\n  "email": ""\n}' },
]

function applyPreset(p: typeof presets[0]) {
  method.value = p.method
  endpoint.value = p.path
  bodyInput.value = p.body || ''
}

function addHeader() {
  headers.value.push({ key: '', value: '' })
}

function removeHeader(i: number) {
  if (headers.value.length > 1) headers.value.splice(i, 1)
}

async function send() {
  loading.value = true
  response.value = null
  const start = performance.now()

  try {
    const config: any = {
      method: method.value,
      url: endpoint.value,
    }
    // Custom headers
    headers.value.filter(h => h.key.trim()).forEach(h => {
      config.headers = config.headers || {}
      config.headers[h.key.trim()] = h.value.trim()
    })
    // Body
    if (['POST', 'PUT', 'PATCH'].includes(method.value) && bodyInput.value) {
      try {
        config.data = JSON.parse(bodyInput.value)
      } catch {
        showError('JSON 格式错误')
        loading.value = false
        return
      }
    }

    const result = await api.request(config)
    response.value = {
      status: 200,
      data: result,
      time: Math.round(performance.now() - start),
    }
  } catch (e: any) {
    response.value = {
      status: e.status || e.response?.status || 500,
      data: e.response?.data || e.message || e,
      time: Math.round(performance.now() - start),
    }
  } finally {
    loading.value = false
  }
}

const resColor = computed(() => {
  if (!response.value) return 'text-text-tertiary'
  return response.value.status < 300 ? 'text-success' : response.value.status < 500 ? 'text-warning' : 'text-danger'
})
</script>

<template>
  <div class="min-h-screen bg-bg flex flex-col">
    <AppHeader />

    <main class="flex-1 max-w-6xl mx-auto px-4 sm:px-6 py-24 w-full">
      <div class="mb-10">
        <h1 class="text-[32px] font-bold text-text-primary tracking-tight mb-2">API Playground</h1>
        <p class="text-text-secondary text-[15px]">在线调试 Auth Center API，实时查看请求与响应</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Request panel -->
        <div class="space-y-5">
          <div class="bg-surface border border-border/40 rounded-2xl p-5">
            <h3 class="text-sm font-semibold text-text-primary mb-4">快速预设</h3>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="p in presets"
                :key="p.path"
                class="px-3 py-1.5 rounded-lg border border-border text-[12px] font-medium text-text-secondary hover:bg-surface-secondary hover:text-text-primary hover:border-text-tertiary/30 transition-all duration-150"
                @click="applyPreset(p)"
              >{{ p.label }}</button>
            </div>
          </div>

          <div class="bg-surface border border-border/40 rounded-2xl p-5 space-y-4">
            <div class="flex gap-3">
              <select v-model="method" class="h-9 w-28 bg-surface-secondary/50 border border-border rounded-xl px-3 text-sm font-semibold text-text-primary focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent transition-all appearance-none bg-no-repeat select-chevron">
                <option>GET</option>
                <option>POST</option>
                <option>PUT</option>
                <option>DELETE</option>
                <option>PATCH</option>
              </select>
              <input v-model="endpoint" type="text" placeholder="/v1/..." class="flex-1 h-9 bg-surface-secondary/50 border border-border rounded-xl px-3 text-sm font-mono text-text-primary placeholder:text-text-tertiary focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent focus:bg-surface transition-all duration-150" />
            </div>

            <!-- Headers -->
            <div>
              <div class="flex items-center justify-between mb-2">
                <span class="text-xs font-semibold text-text-tertiary uppercase tracking-wider">Headers</span>
                <button class="text-[11px] text-accent hover:underline" @click="addHeader">+ 添加</button>
              </div>
              <div class="space-y-1.5">
                <div v-for="(h, i) in headers" :key="i" class="flex gap-2">
                  <input v-model="h.key" placeholder="Key" class="flex-1 h-8 bg-surface-secondary/50 border border-border rounded-lg px-2.5 text-xs font-mono text-text-primary placeholder:text-text-tertiary focus:outline-none focus:ring-1 focus:ring-accent/20 focus:border-accent transition-all" />
                  <input v-model="h.value" placeholder="Value" class="flex-1 h-8 bg-surface-secondary/50 border border-border rounded-lg px-2.5 text-xs font-mono text-text-primary placeholder:text-text-tertiary focus:outline-none focus:ring-1 focus:ring-accent/20 focus:border-accent transition-all" />
                  <button class="w-7 h-8 flex items-center justify-center text-text-tertiary hover:text-danger transition-colors" @click="removeHeader(i)">
                    <Icon name="mdi:close" size="14" />
                  </button>
                </div>
              </div>
            </div>

            <!-- Body -->
            <div v-if="['POST', 'PUT', 'PATCH'].includes(method)">
              <span class="text-xs font-semibold text-text-tertiary uppercase tracking-wider mb-2 block">Body (JSON)</span>
              <textarea
                v-model="bodyInput"
                rows="8"
                placeholder='{"key": "value"}'
                class="w-full bg-surface-secondary/50 border border-border rounded-xl px-3 py-2.5 text-sm font-mono text-text-primary placeholder:text-text-tertiary focus:outline-none focus:ring-2 focus:ring-accent/20 focus:border-accent focus:bg-surface transition-all resize-none"
              />
            </div>

            <button
              :disabled="loading"
              class="w-full h-10 flex items-center justify-center gap-2 rounded-xl text-sm font-semibold text-white transition-all duration-150 bg-accent hover:bg-accent-hover active:scale-[0.98] disabled:opacity-50"
              @click="send"
            >
              <Icon v-if="loading" name="mdi:loading" size="16" class="animate-spin" />
              <Icon v-else name="mdi:send" size="16" />
              {{ loading ? '发送中...' : '发送请求' }}
            </button>
          </div>
        </div>

        <!-- Response panel -->
        <div class="bg-surface border border-border/40 rounded-2xl p-5 flex flex-col min-h-[400px]">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-sm font-semibold text-text-primary">响应</h3>
            <div v-if="response" class="flex items-center gap-3 text-xs">
              <span :class="resColor + ' font-semibold'">Status: {{ response.status }}</span>
              <span class="text-text-tertiary">{{ response.time }}ms</span>
            </div>
          </div>

          <div v-if="!response" class="flex-1 flex items-center justify-center text-text-tertiary">
            <div class="text-center">
              <Icon name="mdi:arrow-left" size="32" class="mx-auto mb-2 opacity-30" />
              <p class="text-[13px]">发送请求以查看响应</p>
            </div>
          </div>

          <div v-else class="flex-1 overflow-auto">
            <pre :class="['text-[13px] font-mono leading-relaxed whitespace-pre-wrap break-all p-4 rounded-xl', response.status < 300 ? 'bg-success/[0.04]' : 'bg-danger/[0.04]']">{{ JSON.stringify(response.data, null, 2) }}</pre>
          </div>
        </div>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<style scoped>
.select-chevron {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='10' height='10' fill='%23AEAEB2' viewBox='0 0 24 24'%3E%3Cpath d='M7.41 8.58L12 13.17l4.59-4.59L18 10l-6 6-6-6z'/%3E%3C/svg%3E");
  background-position: right 8px center;
}
</style>
