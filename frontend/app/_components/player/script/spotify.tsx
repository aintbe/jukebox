"use client"

import { memo, useRef } from "react"
import { useCallback, useEffect } from "react"
import { UserProfile } from "@/types/api"
import { useQueryClient } from "@tanstack/react-query"
import Script from "next/script"
import { toast } from "sonner"
import { userProfileOptions } from "@/lib/api/hooks"
import { getSession } from "@/lib/auth/actions"
import { WEBSOCKET_EVENT } from "@/lib/constants"
import {
  PlaybackAlbum,
  PlaybackTrack,
  PlayerStatus,
  usePlayerStore,
} from "@/lib/stores/player"
import {
  PlayerActions,
  usePlayerActionsStore,
} from "@/lib/stores/player-actions"

interface SpotifyState {
  deviceId?: string
  mounted: boolean
}

function SpotifyScript() {
  // State that are only defined & used in the context of Spotify SDK.
  const playerRef = useRef<Spotify.Player>(undefined)
  const stateRef = useRef<SpotifyState>({ mounted: false })

  const queryClient = useQueryClient()
  const setStatus = usePlayerStore(state => state.setStatus)
  const setActions = usePlayerActionsStore(state => state.setActions)
  const send = usePlayerActionsStore(state => state.send)

  // `deviceId` only exists when Spotify fires "ready", and `send`
  // only works after websocket has been connected. Since we can't be
  // sure which event fires first, add `tryConnect` to both events.
  const tryConnect = useCallback(
    async (_deviceId?: string) => {
      const deviceId = _deviceId ?? stateRef.current.deviceId
      if (!deviceId) return

      const isConnected = usePlayerActionsStore.getState().isConnected
      if (isConnected) {
        send({ event: WEBSOCKET_EVENT.OUT.SPOTIFY_TRANSFER_DEVICE, deviceId })
        setStatus(PlayerStatus.ONLINE)
      }

      stateRef.current.deviceId = deviceId
    },
    [send, setStatus],
  )

  useEffect(() => {
    const unsubscribe = usePlayerActionsStore.subscribe(
      state => state.isConnected,
      (isConnected: boolean) => {
        if (isConnected) {
          tryConnect()
          unsubscribe()
        }
      },
    )
    return () => unsubscribe()
  }, [tryConnect])

  const getToken = useCallback(async (): Promise<string | undefined> => {
    const session = await getSession()
    if (!session) return

    const options = userProfileOptions(session)
    // Check if cached token exists and is still valid.
    const cachedData = queryClient.getQueryData<UserProfile>(options.queryKey)
    const cachedAccess = cachedData?.streamingAccess

    if (
      cachedAccess &&
      cachedAccess.token &&
      (cachedAccess.expiresAt == null ||
        new Date(cachedAccess.expiresAt) >= new Date())
    ) {
      return cachedAccess.token
    }

    // Fetch new token from API server and update useQuery cache.
    console.debug("Token is missing or expired. Reissuing token...")
    const data = await queryClient.fetchQuery(options)

    return data?.streamingAccess?.token
  }, [queryClient])

  const handleReady = useCallback(() => {
    console.log("todo: remounted script!!") // TODO: remove this after testing.

    if (playerRef.current) return

    console.log("todo: remounted script!! no player; let's roll!")

    window.onSpotifyWebPlaybackSDKReady = () => {
      const player = new window.Spotify.Player({
        name: "Jukebox",
        getOAuthToken: cb => {
          getToken().then(token => {
            if (token) cb(token)
          })
        },
        volume: usePlayerStore.getState().device.volume,
      })

      addEventListeners(player, tryConnect)
      player.connect().then(async (success: boolean) => {
        if (success) {
          setActions(getPlayerActions(player))
          player.activateElement()
          playerRef.current = player

          console.debug("Spotify Player is connected successfully.")
        } else {
          toast.error(
            "Failed to connect to Spotify Player. Please refresh the window.",
          )
        }
      })
    }

    // Script is loaded; now we can requset connection to Spotify player!
    setStatus(PlayerStatus.OFFLINE)

    // If the component is remounted after the script has already loaded,
    // the SDK won't call the function again, so we call it manually here.
    const isConnecting = usePlayerStore.getState().isConnecting
    if (isConnecting && window.Spotify) {
      window.onSpotifyWebPlaybackSDKReady()
    }

    stateRef.current.mounted = true
  }, [getToken, setActions, setStatus, tryConnect])

  console.log("todo: spotify rerender")

  // `handleReady` will automatically handle the requests on mount,
  // so this hook only have to take care of updates on re-render.
  const isConnecting = usePlayerStore(state => state.isConnecting)
  useEffect(() => {
    if (isConnecting && stateRef.current.mounted) {
      window.onSpotifyWebPlaybackSDKReady()
    }
  }, [isConnecting])

  // Clean up connected Spotify player on unmount.
  useEffect(() => {
    return () => {
      if (playerRef.current) {
        console.debug("Disconnecting Spotify player...")
        playerRef.current?.disconnect()
        setStatus(PlayerStatus.UNAVAILABLE)
      }
    }
  }, [setStatus])

  return (
    <Script
      src="https://sdk.scdn.co/spotify-player.js"
      strategy="afterInteractive"
      onReady={handleReady}
    />
  )
}

const SpotifyScriptMemo = memo(SpotifyScript)
SpotifyScriptMemo.displayName = "SpotifyScriptMemo"
export { SpotifyScriptMemo as SpotifyScript }

export interface SpotifyConnection {
  deviceId?: string
}

const addEventListeners = (
  player: Spotify.Player,
  tryConnect: (deviceId?: string) => Promise<void>,
) => {
  player.addListener(
    "ready",
    async ({ device_id: deviceId }: Spotify.WebPlaybackInstance) => {
      tryConnect(deviceId)
    },
  )

  player.addListener(
    "not_ready",
    ({ device_id: deviceId }: Spotify.WebPlaybackInstance) => {
      console.debug("not ready", deviceId)
      usePlayerStore.setState({ status: PlayerStatus.UNAVAILABLE })
    },
  )

  player.addListener(
    "player_state_changed",
    async (state: Spotify.PlaybackState) => {
      const { status, setStatus, setPlayback } = usePlayerStore.getState()
      if (state === null) {
        // TODO: do something
        setStatus(PlayerStatus.UNAVAILABLE)
        return
      }
      // We can now guarantee that this player is fully ready to play.
      if (status === PlayerStatus.ONLINE) {
        setStatus(PlayerStatus.READY)
      }

      console.debug("state changed", state)

      const playback = {
        timestamp: state.timestamp,
        position: state.position,
        duration: state.duration,
        isPlaying: !state.paused,
        currentTrack: parseTrack(state.track_window.current_track),
      }
      setPlayback(playback)
    },
  )

  player.addListener("autoplay_failed", () => {
    console.error("Autoplay is not allowed by the browser autoplay rules")
  })

  player.on("initialization_error", ({ message }: Spotify.Error) => {
    console.error("Failed to initialize:", message)
  })

  player.on("authentication_error", ({ message }: Spotify.Error) => {
    console.error("Failed to authenticate:", message)
  })

  player.on("account_error", ({ message }: Spotify.Error) => {
    console.error("Failed to validate Spotify account:", message)
  })

  player.on("playback_error", ({ message }) => {
    const { status } = usePlayerStore.getState()
    if (status == PlayerStatus.ONLINE) {
      toast.error(
        "Server is still trying to connect to Spotify... Please try again later.",
      )
    } else {
      toast.error("Nothing is queued; add songs to play!")
      console.error("Failed to perform playback:", message)
    }
  })
}

const getPlayerActions = (player: Spotify.Player): PlayerActions => {
  const setPlayerVolume = async (volume: number) => {
    await player.setVolume(volume)
    return await player.getVolume()
  }

  const updateStoreDevice = (volume: number, isToggled: boolean) => {
    if (isToggled) {
      usePlayerStore.setState(state => {
        if (state.playback) {
          state.device.isMuted = !state.device.isMuted
        }
      })
    } else {
      usePlayerStore.setState(state => {
        if (state.playback) {
          state.device.volume = volume
        }
      })
    }
  }

  return {
    togglePlay: () => player.togglePlay(),

    playNext: () => player.nextTrack(),

    playPrevious: () => player.previousTrack(),

    playAt: (position: number) => player.seek(position),

    setVolume: async (goal: number) => {
      const volume = await setPlayerVolume(goal)
      updateStoreDevice(volume, false)
    },

    toggleMute: async () => {
      const { device } = usePlayerStore.getState()
      if (device) {
        const goal = device.isMuted ? device.volume : 0
        const actual = await setPlayerVolume(goal)
        updateStoreDevice(actual, actual === goal)
      }
    },
  }
}

const parseTrack = (track?: Spotify.Track): PlaybackTrack | undefined => {
  if (!track) return

  const id = track.id ?? convertUriToId(track.uri)
  const albumId = convertUriToId(track.album.uri)
  const artists = track.artists
    .map(artist => ({
      id: convertUriToId(artist.uri) ?? "",
      name: artist.name,
      url: artist.url ?? `https://open.spotify.com/artist/${id}`,
    }))
    .filter(({ id }) => Boolean(id))

  if (id && albumId && artists.length > 0) {
    return {
      id: id,
      name: track.name,
      url: `https://open.spotify.com/track/${id}`,
      duration: track.duration_ms,
      album: {
        id: albumId,
        name: track.album.name,
        url: `https://open.spotify.com/album/${id}`,
        image: findBestImage(track.album.images),
      },
      artists,
    }
  }
}

/**
 * @param uri Has format of `spotify:track:${id}`.
 * @returns Parsed ID
 */
const convertUriToId = (uri: string) => uri.split(":").at(2)

const findBestImage = (images: Spotify.Image[]): PlaybackAlbum["image"] => {
  if (images.length <= 0) return

  const image = images.reduce((bestImage, currentImage) => {
    // Prefer image with known size
    if (!hasSize(bestImage)) return currentImage
    if (!hasSize(currentImage)) return bestImage

    // Prefer square-shaped image with bigger size
    const isBestSquare = hasSquareSize(bestImage)
    if (isBestSquare && hasBiggerSize(bestImage, currentImage)) {
      return bestImage
    }
    const isCurrentSquare = hasSquareSize(currentImage)
    if (isCurrentSquare && hasBiggerSize(currentImage, bestImage)) {
      return bestImage
    }

    // Prefer square-shaped image with mimimum size
    const minSize = { width: 100, height: 100 }
    if (isBestSquare && hasBiggerSize(bestImage, minSize)) {
      return bestImage
    }
    if (isCurrentSquare && hasBiggerSize(currentImage, minSize)) {
      return bestImage
    }

    // Prefer square-shaped image with any size
    if (isBestSquare) return bestImage
    if (isCurrentSquare) return currentImage

    // Return image with any shape but bigger size
    if (hasBiggerSize(bestImage, currentImage)) return bestImage
    if (bestImage.width > currentImage.width) return bestImage
    return currentImage
  })

  return {
    url: image.url,
    width: image.width ?? undefined,
    height: image.height ?? undefined,
  }
}

interface SizedObject {
  width: number
  height: number
}

const hasSize = (
  object: Pick<Spotify.Image, "width" | "height">,
): object is SizedObject => !!object.width && !!object.height

const hasSquareSize = (object: SizedObject) => object.width === object.height

const hasBiggerSize = (a: SizedObject, b: SizedObject) =>
  a.width >= b.width && a.height >= b.height
