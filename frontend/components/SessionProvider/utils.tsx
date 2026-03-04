import { requireContext } from "@/lib/utils"
import { useContext } from "react"
import { SessionContext } from "./SessionProvider"
import { SessionPromise } from "@/types/app"
import { getSession } from "@/lib/auth/actions"

export function useSession() {
  requireContext(SessionContext)

  const context = useContext(SessionContext)
  if (!context) {
    throw new Error("`useSession` must be wrapped in <SessionProvider />.")
  }

  return context
}

export enum Action {
  REFRESH = "refresh",
  DELETE = "delete",
}

export const getContextSession = async (action: Action): SessionPromise => {
  switch (action) {
    case Action.REFRESH:
      return getSession()
    case Action.DELETE:
      return
    default:
      console.error("Unsupported session update action:", action)
      return
  }
}

export type Trigger = "broadcast" | "window_event"

export interface BroadcastMessage {
  action: Action
  trigger: Trigger
}
