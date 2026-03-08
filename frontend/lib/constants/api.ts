/**
 * The base URL for the API server.
 * @constant
 */
export const API_DOMAIN = process.env.NEXT_PUBLIC_API_DOMAIN

/**
 * The base URL for the relay server.
 * @constant
 */
export const RELAY_DOMAIN = process.env.NEXT_PUBLIC_RELAY_DOMAIN

/**
 * Mandatory headers for relay server requests.
 */
export const RELAY_HEADERS = {
  USER_ID: "X-Relay-User-ID",
  JUKEBOX_ID: "X-Relay-Jukebox-ID",
  STREAMING_SERVICE: "X-Relay-Streaming-Service",
  STREAMING_TOKEN: "X-Relay-Streaming-Token",
  STREAMING_EXPIRES_AT: "X-Relay-Streaming-Expires-At",
}
