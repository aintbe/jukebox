"use client"

import { usePathname, useRouter } from "next/navigation"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from "@/components/ui/dialog"

interface Props {
  children: React.ReactNode
}

export function AuthDialog({ children }: Props) {
  const router = useRouter()
  const pathname = usePathname()

  return (
    <Dialog
      key={pathname} // Force remount when route changes
      defaultOpen
      onOpenChange={() => router.back()}
    >
      <DialogContent className="sm:max-w-md">
        <DialogTitle className="sr-only" />
        <DialogDescription className="sr-only" />
        {children}
      </DialogContent>
    </Dialog>
  )
}
