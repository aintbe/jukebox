"use client"

import { useCallback } from "react"
import { isHTTPError } from "ky"
import { User } from "lucide-react"
import { useRouter } from "next/navigation"
import { toast } from "sonner"
import { signOut } from "@/lib/auth/actions"
import { useSession } from "@/lib/providers"
import { useAuthViewStore } from "@/lib/stores/auth-view"
import { DspSignIn } from "@/components/DspSignIn"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuShortcut,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"

export function Header() {
  const { currentView, openDspSignIn, handleDialog } = useAuthViewStore()

  const { session, clear } = useSession()
  const router = useRouter()

  const handleSignOut = useCallback(async () => {
    try {
      await signOut()
      await clear()

      toast.success("See you :)")
      router.refresh()
    } catch (error) {
      if (isHTTPError(error)) {
        toast.error("Failed to sign out. Try again later.")
      }
    }
  }, [router, clear])

  return (
    <header className="flex h-12 flex-col justify-center p-4">
      <div className="flex items-center justify-between">
        <div>{/* TODO: add logo */}</div>
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
          <div className="flex items-center gap-2">
            <Dialog open={currentView !== null} onOpenChange={handleDialog}>
              <DialogTrigger asChild>
                <Button variant="outline">Sign Up</Button>
              </DialogTrigger>
              <DialogTrigger asChild>
                <Button onClick={openDspSignIn}>Sign In</Button>
              </DialogTrigger>
              <div className="flex items-center gap-2">
                <DialogContent className="sm:max-w-md">
                  <DialogTitle>{/* TODO: add titles */}</DialogTitle>
                  <DialogDescription></DialogDescription>
                  <DspSignIn />
                </DialogContent>
              </div>
            </Dialog>
          </div>
        )}
      </div>
    </header>
  )
}
