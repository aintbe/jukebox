import Spotify from "@/public/icons/spotify.svg"
import { StreamingService } from "@/types/app"

interface Metadata {
  label: string
  icon: React.ComponentType<React.SVGProps<SVGSVGElement>>
  primaryColor: string
  url: string
}

/**
 * Metadata for each supported DSP, including display label, icon, and brand color.
 * @constant
 */
export const STREAMING_SERVICE_METADATA: Record<StreamingService, Metadata> = {
  [StreamingService.SPOTIFY]: {
    label: "Spotify",
    icon: Spotify,
    primaryColor: "#1ed760",
    url: "https://open.spotify.com",
  },
}
