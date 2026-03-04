import "server-only"

import type { middleware } from "@/middleware"
import { SessionPromise } from "@/types/app"
import { decryptSession } from "../crypto"
import { cookies } from "next/headers"
import { AUTH_COOKIE } from "../constants"
import { cacheTag } from "next/cache"

/**
 * Get session on server component. Since server components does not
 * re-render, it does not need any reissuing logic after the initial
 * render. {@link middleware} will handle reissuing if it's needed in
 * the initial rendering.
 *
 * @returns {Session | undefined}
 */
export const getServerSession = async (): SessionPromise => {
  const cookieStore = await cookies()
  const cookie = cookieStore.get(AUTH_COOKIE.SESSION)?.value

  return await decryptAndCacheSession(cookie)
}

const decryptAndCacheSession = async (cookie?: string): SessionPromise => {
  "use cache"

  if (cookie) {
    const session = await decryptSession(cookie)
    cacheTag("session", session?.userId.toString() ?? "")
    return session
  }
}
