import { useQuery } from "@tanstack/react-query"
import { ApiData, UserProfile } from "@/types/api"
import { usePlayerStore } from "../stores/usePlayerStore"
import { ExposedSession } from "@/types/app"
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
