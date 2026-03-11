"use client"

import { isHTTPError } from "ky"
import { Link2, Theater, User } from "lucide-react"
import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import { toast } from "sonner"
import { signOut } from "@/lib/auth/actions"
import { STORAGE_KEY, STREAMING_SERVICE_METADATA } from "@/lib/constants"
import { useSession } from "@/lib/providers"
import { useJukeboxStore } from "@/lib/stores/jukebox"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuShortcut,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"

export function Header() {
  const { session, clear } = useSession()
  const router = useRouter()
  const pathname = usePathname()

  const handleSignOut = async () => {
    try {
      await signOut()
      await clear()
      sessionStorage.clear() // Clear any state created from this session.

      toast.success("See you :)")
      router.refresh()
    } catch (error) {
      if (isHTTPError(error)) {
        toast.error("Failed to sign out. Try again later.")
      }
    }
  }

  const handleSignIn = () => {
    sessionStorage.setItem(STORAGE_KEY.REDIRECT_PATH, pathname)
    router.push("/sign-in")
  }

  const serviceName = useJukeboxStore(state => state.serviceName)
  const service = serviceName
    ? STREAMING_SERVICE_METADATA[serviceName]
    : undefined
  console.log("todo: header rerender")

  return (
    <header className="flex h-12 items-center justify-between p-4">
      <div className="flex h-7 items-center gap-2">
        <Theater className="h-full" /> {/* TODO: Change icon to service icon */}
        {service && (
          <>
            <Link2 className="h-full" />
            <Link
              href={service.url}
              target="_blank"
              rel="noopener noreferrer"
              className="h-full hover:opacity-80"
            >
              <service.icon className="h-full" color={service.primaryColor} />
            </Link>
          </>
        )}
      </div>
      <div className="flex items-center gap-2">
        {session?.username ? (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline">
                <User />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent className="w-40" align="start">
              <DropdownMenuGroup>
                <DropdownMenuItem onClick={handleSignOut}>
                  Sign Out
                  <DropdownMenuShortcut>⇧⌘Q</DropdownMenuShortcut>
                </DropdownMenuItem>
              </DropdownMenuGroup>
            </DropdownMenuContent>
          </DropdownMenu>
        ) : (
          <>
            <Button variant="outline" asChild>
              <Link href="/sign-up">Sign Up</Link>
            </Button>
            <Button onClick={handleSignIn}>Sign In</Button>
          </>
        )}
      </div>
    </header>
  )
}
