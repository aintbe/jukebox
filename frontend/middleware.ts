import { NextResponse, type NextRequest } from "next/server"
import { getSessionCookie, reissue } from "./lib/auth/utils"
import { AUTH_COOKIE } from "./lib/constants"

export const middleware = async (request: NextRequest) => {
  const response = NextResponse.next({ request })

  const session = request.cookies.get(AUTH_COOKIE.SESSION)
  const refreshToken = request.cookies.get(AUTH_COOKIE.REFRESH_TOKEN)?.value
  if (!session && refreshToken) {
    const session = await reissue(refreshToken)
    if (session) {
      response.cookies.set(await getSessionCookie(session))
    } else {
      response.cookies.delete(AUTH_COOKIE.REFRESH_TOKEN)
    }
  }

  return response
}
