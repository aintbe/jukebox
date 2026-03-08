import { create } from "zustand"
import { immer } from "zustand/middleware/immer"

export enum PlayerStatus {
  /**
   * No request has been made to use this player.
   */
  OFFLINE = 0,
  /**
   * `PlayerEngine` is online and `PlayerStore` is initialized. You're now
   * able to call `usePlayerStore` to use properties listed in `PlayerStoreInit`.
   */
  ONLINE,
  /**
   * Default set-ups are requested on the server side. Do not merge this status
   * with `PlayerStatus.READY` because the server cannot ensure us if
   * the set-up is actually finished if you're working with Spotify. Skip to
   * the next status if you're working with other third-party services that
   * ensures us what's happening.
   */
  CONNECTING,
  /**
   * Frontend is sure that the set-ups are done on the server side. Ready to play.
   */
  READY,
}

export interface Playback {
  /**
   * The Unix epoch timestamp when this state was captured (ms).
   * Acts as a reference point for calculating actual position.
   */
  timestamp: number
  /**
   * The current playback position within the track (ms).
   * Indicates the specific point in the audio timeline being played.
   */
  position: number
  /**
   * The total duration of the current media track (ms).
   * Represents the full length of the audio resource.
   */
  duration: number
  isPlaying: boolean
  currentTrack?: PlaybackTrack
}

export interface PlaybackTrack {
  id: string
  name: string
  url: string
  /**
   * The total duration of the current media track (ms).
   * Represents the full length of the audio resource.
   */
  duration: number
  album: PlaybackAlbum
  artists: PlaybackArtist[]
}

export interface PlaybackAlbum {
  id: string
  name: string
  url: string
  image?: {
    url: string
    width?: number
    height?: number
  }
}

export interface PlaybackArtist {
  id: string
  name: string
  url: string
}

export interface PlayerDevice {
  volume: number // between 0 and 1.
  isMuted: boolean
}

interface PlayerStore extends PlayerStoreInit {
  status: PlayerStatus
  setStatus: (status: PlayerStatus) => void
  jukeboxId: number | undefined
  setJukeboxId: (jukeboxId?: number) => void
  playback: Playback | undefined
  setPlayback: (playback?: Playback) => void
  device: PlayerDevice
  initialize: (actions: PlayerStoreInit) => void
}

export interface PlayerStoreInit {
  connect: () => Promise<void>
  togglePlay: () => Promise<void>
  playNext: () => Promise<void>
  playPrevious: () => Promise<void>
  playAt: (position: number) => Promise<void>
  setVolume: (volume: number) => Promise<void>
  toggleMute: () => Promise<void>
}

export const usePlayerStore = create<PlayerStore>()(
  immer(set => ({
    status: PlayerStatus.OFFLINE,
    setStatus: (status: PlayerStatus) => set({ status }),
    jukeboxId: undefined,
    setJukeboxId: (jukeboxId?: number) => set({ jukeboxId }),
    playback: undefined,
    setPlayback: (playback?: Playback) => set({ playback }),
    device: {
      volume: 0.5,
      isMuted: false,
    },
    initialize: (actions: PlayerStoreInit) => set(actions),

    // Initialized by scripts calling `initialize`.
    connect: async () => undefined,
    togglePlay: async () => undefined,
    playNext: async () => undefined,
    playPrevious: async () => undefined,
    playAt: async () => undefined,
    setVolume: async () => undefined,
    toggleMute: async () => undefined,
  })),
)
