import type { AxiosInstance, AxiosRequestConfig } from 'axios'

export function useApi() {
  const { $api } = useNuxtApp() as { $api: AxiosInstance }

  return {
    request<T = unknown>(config: AxiosRequestConfig): Promise<T> {
      return $api.request(config) as Promise<T>
    },

    get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
      return $api.get(url, config) as Promise<T>
    },

    post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
      return $api.post(url, data, config) as Promise<T>
    },

    put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
      return $api.put(url, data, config) as Promise<T>
    },

    delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
      return $api.delete(url, config) as Promise<T>
    },

    postForm<T = unknown>(url: string, data: FormData, config?: AxiosRequestConfig): Promise<T> {
      return $api.post(url, data, {
        ...config,
        headers: {
          'Content-Type': 'multipart/form-data',
          ...config?.headers,
        },
      }) as Promise<T>
    },
  }
}
