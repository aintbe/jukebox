"use client"

import { REDIRECT_PATH_KEY } from "@/app/sign-in/callback/page"
import { Button } from "@/components/ui/button"
import { apiDomain, DSP_METADATA } from "@/lib/constants"
import { usePathname } from "next/navigation"

export function DspSignIn() {
  const pathname = usePathname()
  const handleClick = () => {
    sessionStorage.setItem(REDIRECT_PATH_KEY, pathname)
  }

  return (
    <div>
      <p>Sign in with:</p>
      <ul>
        {Object.entries(DSP_METADATA).map(
          ([name, { label, icon: Icon, color }]) => (
            <li key={name}>
              <Button asChild style={{ backgroundColor: color }}>
                <a
                  href={`${apiDomain}/oauth2/authorization/${name}`}
                  onClick={handleClick}
                  className="flex items-center gap-2 text-white"
                >
                  <Icon />
                  <span>{label}</span>
                </a>
              </Button>
            </li>
          ),
        )}
      </ul>
    </div>
  )
}
