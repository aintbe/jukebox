export const AUTH_COOKIE = {
  SESSION: "next.session",
  REFRESH_TOKEN: "next.refresh-token",
} as const

export const STORAGE_KEY = {
  REDIRECT_PATH: "next:redirect-to-after-sign-in",
  CONNECT_REQUESTED: "next:connect-requested",
}

export const IMAGE = {
  DEFAULT_TRACK: "/window.svg",
} as const
