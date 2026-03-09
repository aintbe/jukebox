import { create } from "zustand"
import { immer } from "zustand/middleware/immer"
import { STORAGE_KEY } from "../constants"

export enum PlayerStatus {
  /**
   * It is guaranteed that the error occurred and thus request cannot be made.
   */
  UNAVAILABLE = -1,
  /**
   * Player script is not loaded or
   */
  OFFLINE = 0,
  /**
   * `PlayerScript` is online and `PlayerStore` is initialized. You're now
   * able to call `usePlayerStore` to use properties listed in `PlayerStoreInit`.
   * Do not merge this status with `PlayerStatus.READY` because the backend server
   * cannot assure us if it actually completed set-ups if you're working with Spotify.
   * Skip to the next status if you're working with other third-party services that
   * ensures us what's happening.
   */
  ONLINE,
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
  /**
   * Preserved through one session so that user wouldn't need to
   * request new connection every time they refresh.
   */
  isConnecting: boolean
  connect: () => void
  jukeboxId: number | undefined
  setJukeboxId: (jukeboxId?: number) => void
  playback: Playback | undefined
  setPlayback: (playback?: Playback) => void
  device: PlayerDevice
  initialize: (actions: PlayerStoreInit) => void
}

export interface PlayerStoreInit {
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
    isConnecting:
      typeof window !== "undefined"
        ? sessionStorage.getItem(STORAGE_KEY.CONNECT_REQUESTED) === "true"
        : false,
    connect: () => {
      set({ isConnecting: true })
      sessionStorage.setItem(STORAGE_KEY.CONNECT_REQUESTED, "true")
      window.dispatchEvent(new Event(STORAGE_KEY.CONNECT_REQUESTED))
    },
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
    togglePlay: async () => undefined,
    playNext: async () => undefined,
    playPrevious: async () => undefined,
    playAt: async () => undefined,
    setVolume: async () => undefined,
    toggleMute: async () => undefined,
  })),
)
