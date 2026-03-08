import { StreamingService } from "@/types/app"
import { SpotifyScript } from "./spotify"

export const PLAYER_SCRIPT: Record<StreamingService, React.FC> = {
  [StreamingService.SPOTIFY]: SpotifyScript,
}
