import { ApiData, Ticket, UserProfile } from "@/types/api"
import { ExposedSession } from "@/types/app"
import { useQuery } from "@tanstack/react-query"
import { SEC_IN_MS } from "../constants"
import { usePlayerStore } from "../stores/player"
import { api } from "./client"

export const userProfileOptions = (session?: ExposedSession) => ({
  queryKey: ["user", "profile", session?.userId],
  queryFn: async () => {
    const data: ApiData<UserProfile> = await api.get("user").json()

    const jukeboxId = data?.data.jukebox?.id
    usePlayerStore.getState().setJukeboxId(jukeboxId)

    return data?.data
  },
  enabled: !!session?.userId, // Run only if `userId` exists
  retry: false, // Do not retry on failure
})

export const useUserProfile = (session?: ExposedSession) =>
  useQuery(userProfileOptions(session))

export const useWebsocketTicket = (
  handle: string | undefined,
  isConnecting: boolean,
) =>
  useQuery({
    queryKey: ["websocket", "ticket", handle],
    queryFn: async () => {
      if (!handle) return null
      const data: ApiData<Ticket> = await api
        .post(`jukebox/${handle}/join`)
        .json()
      return data?.data.ticket
    },
    staleTime: 30 * SEC_IN_MS,
    gcTime: 30 * SEC_IN_MS,
    retry: 1,
    enabled: isConnecting,
  })
