import Spotify from "@/public/icons/spotify.svg"

export enum Dsp {
  SPOTIFY = "spotify",
  APPLE_MUSIC = "apple_music",
  YOUTUBE_MUSIC = "youtube_music",
}

interface DspMetadata {
  label: string
  icon: React.ComponentType<React.SVGProps<SVGSVGElement>>
  color: string
}

export const DSP_METADATA: Record<Dsp, DspMetadata> = {
  [Dsp.SPOTIFY]: {
    label: "Spotify",
    icon: Spotify,
    color: "#1ed760",
  },
  [Dsp.APPLE_MUSIC]: {
    label: "Apple Music",
    icon: () => <div></div>,
    color: "pink",
  },
  [Dsp.YOUTUBE_MUSIC]: {
    label: "YouTube Music",
    icon: () => <div></div>,
    color: "red",
  },
}
