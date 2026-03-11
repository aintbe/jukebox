export interface ExposedSession {
  userId: number
  username: string
}

/**
 * Retrieved session from access token.
 * @interface
 */
export interface Session extends ExposedSession {
  accessToken: string
  /**
   * Access token expiration time in milliseconds. Stored time will be
   * shorter than actual expiration time so that reissue could be done
   * preemptively.
   */
  accessExpiresAt: number
}
export type SessionPromise = Promise<Session | undefined>

/**
 * Enum representing supported Digital Service Providers (DSPs).
 * @enum {string}
 */
export enum StreamingService {
  SPOTIFY = "spotify",
}
