"use client"

import { useMemo } from "react"
import { useUserProfile } from "@/lib/api/hooks"
import { useSession } from "@/lib/providers"
import { PLAYER_SCRIPT } from "./script"
import { PlayerLayout } from "./view/PlayerLayout"

export function Player() {
  const { session } = useSession()
  const { data, isPending, isError } = useUserProfile(session)

  const PlayerScript = useMemo(() => {
    const engineName = data?.streamingAccess?.serviceName
    if (!isPending && !isError && data?.jukebox && engineName) {
      return PLAYER_SCRIPT[engineName]
    }
  }, [data?.streamingAccess?.serviceName, data?.jukebox, isPending, isError])

  if (PlayerScript) {
    return (
      <>
        <PlayerScript />
        <PlayerLayout />
      </>
    )
  }
}
