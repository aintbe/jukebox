"use client"

import {
  useEffect,
  useState,
  useCallback,
  useRef,
  useMemo,
  createContext,
} from "react"
import { Session, ExposedSession, SessionPromise } from "@/types/app"
import { SEC_IN_MS } from "@/lib/constants"
import { getSession } from "../auth/actions"

type SessionContextValue = (
  | { session: ExposedSession; isAuthenticated: true }
  | { session: undefined; isAuthenticated: false }
) & {
  refresh: () => Promise<void>
  clear: () => Promise<void>
}

export const SessionContext = createContext<SessionContextValue>({
  session: undefined,
  isAuthenticated: false,
  refresh: async () => undefined,
  clear: async () => undefined,
})

interface SessionSync {
  isUpdating: boolean
  lastUpdated: number
  accessToken: string | undefined
}

const channel =
  typeof window !== "undefined" ? new BroadcastChannel("session") : undefined

interface Props {
  children: React.ReactNode
  initialSession: Session | undefined
}

export function SessionProvider({ children, initialSession }: Props) {
  const [session, setSession] = useState<Session | undefined>(initialSession)
  const syncRef = useRef<SessionSync>({
    isUpdating: false,
    lastUpdated: 0,
    accessToken: undefined,
  })

  // Prevent re-redering after reissuing without user change.
  const exposedSession = useMemo(
    (): ExposedSession | undefined =>
      session?.userId
        ? {
            userId: session?.userId,
            username: session?.username,
          }
        : undefined,
    [session?.userId, session?.username],
  )

  // Check session forcefully when requested, and update if it changed.
  const update = useCallback(
    async (action: Action, cascade: boolean = true) => {
      if (syncRef.current.isUpdating) return
      syncRef.current.isUpdating = true

      const session = await getContextSession(action)
      const renewed = syncRef.current.accessToken !== session?.accessToken

      if (renewed) {
        setSession(session)
        syncRef.current.accessToken = session?.accessToken

        // Cascade the changes to other browsers.
        if (cascade) {
          channel?.postMessage({ action: Action.REFRESH, trigger: "broadcast" })
        }
      }

      syncRef.current.lastUpdated = Date.now()
      syncRef.current.isUpdating = false
    },
    [],
  )

  // Update session automatically after access token expires.
  useEffect(() => {
    if (!session?.accessExpiresAt) return

    const timeout = Date.now() - session.accessExpiresAt
    // Add jitter so that the browser wouldn't try to request reissue
    // all at the same time when multiple tabs are open.
    const jitter = Math.floor(Math.random() * 10 * SEC_IN_MS)
    const timer = setTimeout(() => update(Action.REFRESH), timeout + jitter)

    return () => {
      if (timer) clearTimeout(timer)
    }
  }, [session?.accessExpiresAt, update])

  // Update session automatically if the update request was broadcasted
  // from other brower tabs or browser states have changed.
  useEffect(() => {
    const handleUpdate = (trigger: Trigger) => {
      if (trigger == "broadcast") {
        update(
          Action.REFRESH,
          false, // Already broadcasted; do not fall in loop
        )
      } else if (trigger == "window_event") {
        const has1MinPassed =
          syncRef.current.lastUpdated + 60 * SEC_IN_MS < Date.now()

        // No need to update access token too frequently on window events
        if (has1MinPassed) update(Action.REFRESH)
      } else {
        console.error("Unknown trigger:", trigger)
      }
    }

    const messageHandler = ({ data }: MessageEvent<BroadcastMessage>) =>
      handleUpdate(data.trigger)
    const windowEventHandler = () => handleUpdate("window_event")

    channel?.addEventListener("message", messageHandler)
    window.addEventListener("online", windowEventHandler)
    document.addEventListener("visibilitychange", windowEventHandler)
    return () => {
      channel?.removeEventListener("message", messageHandler)
      window.removeEventListener("online", windowEventHandler)
      document.removeEventListener("visibilitychange", windowEventHandler)
    }
  }, [update])

  const value = useMemo(
    (): SessionContextValue => ({
      ...(exposedSession
        ? {
            session: exposedSession,
            isAuthenticated: true,
          }
        : { session: undefined, isAuthenticated: false }),

      refresh: () => update(Action.REFRESH),
      clear: () => update(Action.CLEAR),
    }),
    [exposedSession, update],
  )

  return (
    <SessionContext.Provider value={value}>{children}</SessionContext.Provider>
  )
}

enum Action {
  REFRESH = "refresh",
  CLEAR = "delete",
}

const getContextSession = async (action: Action): SessionPromise => {
  switch (action) {
    case Action.REFRESH:
      return getSession()
    case Action.CLEAR:
      return undefined
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
