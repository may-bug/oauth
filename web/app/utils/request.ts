import axios, {
    type AxiosInstance,
    type AxiosResponse,
    type InternalAxiosRequestConfig
} from 'axios'
import type {ApiResponse} from "~/types/api";
import { captchaToken, captchaCode } from "~/composables/captchaState";

let isRefreshing = false
let failedQueue: Array<{
    resolve: (value: unknown) => void
    reject: (reason?: unknown) => void
}> = []

function processQueue(error: unknown) {
    failedQueue.forEach(({resolve, reject}) => {
        if (error) {
            reject(error)
        } else {
            resolve(undefined)
        }
    })
    failedQueue = []
}

export function createRequestInstance(): AxiosInstance {
    const config = useRuntimeConfig()
    const baseURL = "/api"

    const instance = axios.create({
        baseURL,
        timeout: 15000,
        headers: {
            'Content-Type': 'application/json',
            'Api-Version': '1',
        },
    })

    // Request interceptor
    instance.interceptors.request.use(
        (request: InternalAxiosRequestConfig) => {
            if (import.meta.client) {
                const authStore = useAuthStore()
                const orgStore = useOrgStore()

                if (authStore.accessToken) {
                    request.headers.Authorization = `Bearer ${authStore.accessToken}`
                }
                if (orgStore.orgHeader) {
                    request.headers['X-Org-Id'] = orgStore.orgHeader
                }

                // Captcha headers — from shared captcha state
                if (captchaToken.value) {
                    request.headers['X-Captcha-Token'] = captchaToken.value
                }
                if (captchaCode.value) {
                    request.headers['X-Captcha-Code'] = captchaCode.value
                }
            }
            return request
        },
        (error) => Promise.reject(error)
    )

    // Response interceptor
    instance.interceptors.response.use(
        (response: AxiosResponse<ApiResponse>) => {
            const data = response.data
            if (data.code !== undefined && data.code !== 0) {
                return Promise.reject({
                    code: data.code,
                    message: data.message || '请求失败',
                    status: response.status,
                })
            }
            // Backend Base64-encodes all data fields
            const raw = data.data as unknown
            if (typeof raw === 'string') {
                try { return JSON.parse(atob(raw)) } catch { return raw }
            }
            return raw as any
        },
        async (error) => {
            const originalRequest = error.config

            // 401 - try refresh token
            if (error.response?.status === 401 && !originalRequest._retry) {
                if (isRefreshing) {
                    return new Promise((resolve, reject) => {
                        failedQueue.push({resolve, reject})
                    }).then(() => instance(originalRequest))
                }

                originalRequest._retry = true
                isRefreshing = true

                try {
                    const authStore = useAuthStore()
                    if (!authStore.refreshToken) {
                        throw new Error('No refresh token')
                    }

                    const refreshResponse = await axios.post(`${baseURL}/auth/refresh`, {
                        refreshToken: authStore.refreshToken,
                    })

                    const {accessToken, refreshToken} = refreshResponse.data.data
                    authStore.setTokens(accessToken, refreshToken)
                    processQueue(null)

                    originalRequest.headers.Authorization = `Bearer ${accessToken}`
                    return instance(originalRequest)
                } catch (refreshError) {
                    processQueue(refreshError)
                    const authStore = useAuthStore()
                    authStore.clearAuth()
                    navigateTo('/auth/login')
                    return Promise.reject(refreshError)
                } finally {
                    isRefreshing = false
                }
            }

            // Structured error
            const apiError = {
                code: error.response?.data?.code || error.response?.status || 500,
                message: error.response?.data?.message || error.message || '网络错误',
                status: error.response?.status || 500,
            }

            // Toast for specific errors
            if (import.meta.client) {
                if (apiError.status === 403) {
                    dispatchToast('error', '无权限执行此操作')
                } else if (apiError.status === 429) {
                    dispatchToast('error', '请求过于频繁，请稍后再试')
                }
            }

            return Promise.reject(apiError)
        }
    )

    return instance
}

function dispatchToast(type: string, msg: string) {
    if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('toast', {detail: {type, message: msg}}))
    }
}
