export const AUTH_COOKIE = {
  SESSION: "session",
  REFRESH_TOKEN: "refreshToken",
} as const

export const isClient = typeof window !== "undefined"
