import { Session, SessionPromise } from "@/types/app"
import { jwtDecode, JwtPayload } from "jwt-decode"
import { isHTTPError } from "ky"
import { ResponseCookie } from "next/dist/compiled/@edge-runtime/cookies"
import { publicApi } from "../api/utils"
import { AUTH_COOKIE, SEC_IN_MS } from "../constants"
import { encryptSession } from "../crypto"

export const reissue = async (refreshToken: string): SessionPromise => {
  try {
    const res = await publicApi.post("auth/reissue", {
      headers: { Cookie: `refreshToken=${refreshToken}` },
    })
    const accessToken = extractAccessToken(res.headers)
    if (accessToken) {
      return parseAccessToken(accessToken)
    }
  } catch (error) {
    console.debug("Failed to reissue access token.")
    if (isHTTPError(error)) {
      console.debug(await error.response.json())
    }
  }
}

export const extractAccessToken = (headers: Headers) =>
  headers.get("Authorization")?.replace("Bearer ", "")

interface AccessToken extends JwtPayload {
  userId: number
  username: string
}

export const parseAccessToken = (accessToken: string): Session | undefined => {
  const decoded = jwtDecode<AccessToken>(accessToken)
  const user: Session = {
    userId: decoded.userId,
    username: decoded.username,
    accessToken,
    // Expire access token earlier so it would be reissued preemptively.
    accessExpiresAt: decoded.exp ? decoded.exp * SEC_IN_MS - 60 * SEC_IN_MS : 0,
  }

  return Object.values(user).every(Boolean) ? user : undefined
}

interface RefreshToken {
  value: string
  maxAge: number
}

export const parseRefreshToken = (
  cookies: string[],
): RefreshToken | undefined => {
  const refreshCookie = cookies.find(c => c.startsWith("refreshToken="))
  if (!refreshCookie) {
    return
  }
  const attributes = refreshCookie.split(";").map(s => s.trim())

  const value = attributes.at(0)?.split("=")?.at(1)
  const maxAgeStr = attributes
    .find(attr => attr.toLowerCase().startsWith("max-age="))
    ?.split("=")
    .at(1)
  const maxAge = maxAgeStr ? parseInt(maxAgeStr) : undefined

  if (value && maxAge && !isNaN(maxAge)) {
    return {
      value,
      maxAge,
    }
  }
}

/**
 * Basic cookie options for auth information.
 */
const authCookie = {
  httpOnly: true,
  secure: false, // TODO: use .env
  sameSite: "lax",
  path: "/",
} as const

/**
 * Create cookie options for session.
 *
 * @param session Alive session parsed from access token.
 * @returns ResponseCookie
 */
export const getSessionCookie = async (
  session: Session,
): Promise<ResponseCookie> => ({
  ...authCookie,
  name: AUTH_COOKIE.SESSION,
  value: await encryptSession(session),
  expires: session?.accessExpiresAt,
})

/**
 * Create cookie options for refresh token.
 *
 * @param refreshToken If not given, return cookie with `maxAge=0`
 * to delete the stored `refreshToken`.
 * @returns ResponseCookie
 */
export const getRefreshTokenCookie = (
  refreshToken: RefreshToken,
): ResponseCookie => ({
  ...authCookie,
  name: AUTH_COOKIE.REFRESH_TOKEN,
  value: refreshToken.value,
  // Delete cookie if `refreshToken` is not given.
  maxAge: refreshToken.maxAge,
})
