"use client"

import Link from "next/link"
import { API_DOMAIN, STREAMING_SERVICE_METADATA } from "@/lib/constants"
import { Button } from "@/components/ui/button"

export function SignIntoStreamingService() {
  return (
    <div>
      <p>Sign in with:</p>
      <ul>
        {Object.entries(STREAMING_SERVICE_METADATA).map(
          ([name, { label, icon: Icon, primaryColor }]) => (
            <li key={name}>
              <Button asChild style={{ backgroundColor: primaryColor }}>
                <Link
                  href={`${API_DOMAIN}/oauth2/authorization/${name}`}
                  className="flex items-center gap-2 text-white"
                >
                  <Icon />
                  <span>{label}</span>
                </Link>
              </Button>
            </li>
          ),
        )}
      </ul>
    </div>
  )
}
