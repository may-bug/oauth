export function useToast() {
  function dispatch(type: string, message: string) {
    window.dispatchEvent(new CustomEvent('toast', { detail: { type, message } }))
  }

  return {
    success: (msg: string) => dispatch('success', msg),
    error: (msg: string) => dispatch('error', msg),
    warning: (msg: string) => dispatch('warning', msg),
    info: (msg: string) => dispatch('info', msg),
  }
}
