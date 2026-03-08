import { UserProfile } from "@/types/api"
import { SessionPromise } from "@/types/app"
import ky, { isHTTPError } from "ky"
import { API_DOMAIN, RELAY_DOMAIN, RELAY_HEADERS } from "../constants"

export const publicApi = ky.create({
  prefixUrl: API_DOMAIN,
  retry: 0,
  timeout: 5000,
})

export const createAuthApi = (getSession: () => SessionPromise) => {
  return publicApi.extend({
    hooks: {
      beforeRequest: [
        async request => {
          const session = await getSession()
          if (session?.accessToken) {
            request.headers.set(
              "Authorization",
              `Bearer ${session.accessToken}`,
            )
          }
        },
      ],
    },
  })
}

/**
 * A dedicated Ky instance for direct communication with the relay server.
 *
 * This should only be used in exceptional cases where a synchronous response
 * is strictly required (e.g., initial connection), and where waiting for the
 * API server round-trip is not feasible.
 *
 * Note that:
 * - Does NOT go through the API server
 * - Does NOT include any authentication logic
 * - Authentication is delegated entirely to the third-party service
 *
 * Prefer using the default API instance whenever possible.
 * Use this ONLY when absolutely necessary.
 */
export const relayApi = publicApi.extend({
  prefixUrl: RELAY_DOMAIN,
})

export const getRelayHeaders = (data?: UserProfile) => {
  if (data && data.jukebox && data.streamingAccess) {
    return {
      [RELAY_HEADERS.USER_ID]: String(data.id),
      [RELAY_HEADERS.JUKEBOX_ID]: String(data.jukebox.id),
      [RELAY_HEADERS.STREAMING_SERVICE]: data.streamingAccess.serviceName,
      [RELAY_HEADERS.STREAMING_TOKEN]: data.streamingAccess.token,
      ...(data.streamingAccess.expiresAt !== null && {
        [RELAY_HEADERS.STREAMING_EXPIRES_AT]: data.streamingAccess.expiresAt,
      }),
    }
  }
}

export const onHttpStatus = (error: unknown, status: number) =>
  isHTTPError(error) && error.response.status === status
