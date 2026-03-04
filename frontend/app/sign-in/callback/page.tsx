"use client"

import { Suspense, useEffect, useRef } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { toast } from "sonner"
import { issue } from "@/lib/auth/actions"
import { useSession } from "@/components/SessionProvider"

export const REDIRECT_PATH_KEY = "redirect-to-after-sign-in"
export const DEFAULT_REDIRECT_PATH = "/"

export default function DspSignInCallbackPage() {
  return (
    <div>
      {/* // TODO: show intermediate image */}
      <div className="flex h-screen items-center justify-center">
        <p>로그인 처리 중...</p>
      </div>

      <Suspense fallback={null}>
        <SignInScript />
      </Suspense>
    </div>
  )
}

function SignInScript() {
  const searchParams = useSearchParams()
  const code = searchParams.get("code")

  const { session, update } = useSession()
  const router = useRouter()

  const hasProcessed = useRef(false)
  useEffect(() => {
    if (hasProcessed.current) return
    // Do not turn this off again after hook since router might
    // cancel ongoing redirection.
    hasProcessed.current = true

    const signIn = async (): Promise<string | undefined> => {
      const signedIn = await issue(code)
      await update()

      if (!signedIn) {
        toast.error("Failed to sign in.")
        return
      }
      toast.success("Welcome!")

      const redirectPath = sessionStorage.getItem(REDIRECT_PATH_KEY)
      sessionStorage.removeItem(REDIRECT_PATH_KEY)

      return redirectPath ?? undefined
    }

    const redirect = (path?: string) => {
      router.push(path ?? DEFAULT_REDIRECT_PATH)
    }

    if (!session) {
      signIn().then((path) => redirect(path))
    }
    redirect()
  }, [code, router, session, update])

  return null
}
