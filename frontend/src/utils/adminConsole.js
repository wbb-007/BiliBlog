export function getAdminConsoleUrl() {
  const configured = import.meta.env.VITE_ADMIN_CONSOLE_URL?.trim()
  if (configured) {
    return configured
  }

  const { protocol, hostname } = window.location
  return `${protocol}//${hostname}:5174/admin.html`
}

export function openAdminConsole() {
  window.open(getAdminConsoleUrl(), '_blank', 'noopener')
}
