"use client"

import { isClient, SECOND_IN_MS } from "@/lib/constants"
import { Session, ExposedSession } from "@/types/app"
import {
  useEffect,
  useState,
  useCallback,
  useRef,
  useMemo,
  createContext,
} from "react"
import { Trigger, BroadcastMessage, Action, getContextSession } from "./utils"
import { requireContext } from "@/lib/utils"

type SessionContextValue = SessionSetter &
  (
    | {
        session: ExposedSession
        isAuthenticated: true
      }
    | {
        session: undefined
        isAuthenticated: false
      }
  )

interface SessionSetter {
  update: (action?: Action) => Promise<void>
}

export const SessionContext = createContext?.<SessionContextValue | undefined>(
  undefined,
)

interface SessionSync {
  isUpdating: boolean
  lastUpdated: number
  accessToken: string | undefined
}

const channel = isClient ? new BroadcastChannel("session") : undefined

interface Props {
  children: React.ReactNode
  initializer: Session | undefined
}

export function SessionProvider({ children, initializer }: Props) {
  requireContext(SessionContext)

  const [session, setSession] = useState<Session | undefined>(initializer)
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

  const update = useCallback(
    async ({ action, cascade }: { action: Action; cascade: boolean }) => {
      if (syncRef.current.isUpdating) return
      syncRef.current.isUpdating = true

      const session = await getContextSession(action)
      const renewed = syncRef.current.accessToken !== session?.accessToken

      // Update session only if the session is actually renewed.
      if (renewed) {
        setSession(session)
        syncRef.current.accessToken = session?.accessToken

        if (cascade) {
          channel?.postMessage({ action: Action.REFRESH, trigger: "broadcast" })
        }
      }

      syncRef.current.lastUpdated = Date.now()
      syncRef.current.isUpdating = false
    },
    [],
  )

  useEffect(() => {
    // Update session after access token expires.
    if (!session?.accessExpiresAt) return

    const timeout = Date.now() - session.accessExpiresAt
    // Add jitter so that it wouldn't try to request reissue
    // all at the same time when multiple tabs are open.
    const jitter = Math.floor(Math.random() * 10 * SECOND_IN_MS)
    const timer = setTimeout(
      () => update({ action: Action.REFRESH, cascade: true }),
      timeout + jitter,
    )

    return () => {
      if (timer) clearTimeout(timer)
    }
  }, [session?.accessExpiresAt, update])

  useEffect(() => {
    // We do not need to update access token too frequently.
    const canUpdate = () =>
      syncRef.current.lastUpdated + 60 * SECOND_IN_MS < Date.now()

    const handleUpdate = (trigger: Trigger) => {
      if (trigger == "broadcast") {
        // Already broadcasted; do not fall in loop
        update({ action: Action.REFRESH, cascade: false })
      } else if (trigger == "window_event") {
        if (canUpdate()) {
          update({ action: Action.REFRESH, cascade: true })
        }
      } else {
        console.error("Unknown trigger:", trigger)
      }
    }

    // Initialize session on load.
    handleUpdate("broadcast")

    // Update if broadcasted or focus/network has changed.
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

      // This will try to update session regardless of the last updated time.
      update: (action: Action = Action.REFRESH) =>
        update({ action, cascade: true }),
    }),
    [exposedSession, update],
  )

  return (
    <SessionContext.Provider value={value}>{children}</SessionContext.Provider>
  )
}
