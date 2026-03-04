"use server"

import { cookies } from "next/headers"
import {
  extractAccessToken,
  getRefreshTokenCookie,
  getSessionCookie,
  parseRefreshToken,
  parseSession,
  reissue as reissue,
} from "./utils"
import { SessionPromise } from "@/types/app"
import { HTTPError } from "ky"
import { AUTH_COOKIE } from "../constants"
import { getServerSession } from "./server"
import { serverApi } from "../api/server"
import { publicApi } from "../api/utils"

/**
 * Get session via server action. All server actions and client components
 * need to use this instead of {@link getServerSession}. Unlike {@link getServerSession},
 * this one performs reissuing on possible session expiration.
 *
 * Do not migrate this into client component since client component should
 * not modify browser cookies.
 */
export const getSession = async (): SessionPromise => {
  const session = await getServerSession()
  if (session) return session

  const cookieStore = await cookies()
  const refreshToken = cookieStore.get(AUTH_COOKIE.REFRESH_TOKEN)?.value
  if (!refreshToken) return

  const newSession = await reissue(refreshToken)
  if (newSession) {
    cookieStore.set(await getSessionCookie(newSession))
  } else {
    cookieStore.set(getRefreshTokenCookie())
  }

  return newSession
}

/**
 * Sign out from backend and delete session cookies.
 *
 * Do not migrate this into client component since client component should
 * not modify browser cookies.
 */
export const signOut = async () => {
  await serverApi.post("auth/sign-out")

  const cookieStore = await cookies()
  cookieStore.delete(AUTH_COOKIE.SESSION)
  cookieStore.delete(AUTH_COOKIE.REFRESH_TOKEN)
}

/**
 * Issue access token & refresh token with the authorization code returned
 * for callback page after dsp sign in.
 *
 * Do not migrate this into client component since client component should
 * not modify browser cookies.
 *
 * @param code Authorization code returned via callback query params.
 */
export async function issue(code: string | null): Promise<boolean> {
  if (!code) return false
  try {
    const res = await publicApi.post("auth/issue", { json: { code } })

    const accessToken = extractAccessToken(res.headers)
    const session = accessToken ? parseSession(accessToken) : null

    const setCookies = res.headers.getSetCookie()
    const refreshToken = parseRefreshToken(setCookies)

    if (!session || !refreshToken) {
      console.error(
        "`accessToken` or `refreshToken` is not found in the response header.",
      )
      return false
    }

    const cookieStore = await cookies()
    cookieStore.set(await getSessionCookie(session))
    cookieStore.set(getRefreshTokenCookie(refreshToken))

    return true
  } catch (error) {
    if (error instanceof HTTPError) {
      console.debug(await error.response.json())
    }
    return false
  }
}
