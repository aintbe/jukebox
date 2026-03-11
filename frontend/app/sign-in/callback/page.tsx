"use client"

import { useEffect, useRef } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { toast } from "sonner"
import { issue } from "@/lib/auth/actions"
import { STORAGE_KEY } from "@/lib/constants"
import { useSession } from "@/lib/providers"

export const DEFAULT_REDIRECT_PATH = "/"

export default function SignInCallbackPage() {
  const searchParams = useSearchParams()

  const { session, refresh } = useSession()
  const router = useRouter()

  const hasProcessed = useRef(false)
  useEffect(() => {
    if (hasProcessed.current) return
    // Do not turn this off again after hook since router might
    // cancel ongoing redirection.
    hasProcessed.current = true

    const signIn = async (): Promise<string | undefined> => {
      const ticket = searchParams.get("ticket")
      const signedIn = await issue(ticket)
      await refresh()

      if (!signedIn) {
        toast.error("Failed to sign in.")
        return
      }
      toast.success("Welcome!")

      const redirectPath = sessionStorage.getItem(STORAGE_KEY.REDIRECT_PATH)
      sessionStorage.removeItem(STORAGE_KEY.REDIRECT_PATH)

      return redirectPath ?? undefined
    }

    const redirect = (path?: string) => {
      router.push(path ?? DEFAULT_REDIRECT_PATH)
    }

    if (!session) {
      signIn().then(path => redirect(path))
    }
    redirect()
  }, [router, session, refresh, searchParams])

  return (
    <div>
      {/* // TODO: show intermediate image */}
      <div className="flex h-screen items-center justify-center">
        <p>로그인 처리 중...</p>
      </div>
    </div>
  )
}
