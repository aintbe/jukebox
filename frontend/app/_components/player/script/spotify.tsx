"use client"

import { memo, RefObject, useRef } from "react"
import { useCallback, useEffect } from "react"
import { UserProfile } from "@/types/api"
import { useQueryClient } from "@tanstack/react-query"
import { isHTTPError } from "ky"
import Script from "next/script"
import { toast } from "sonner"
import { userProfileOptions } from "@/lib/api/hooks"
import { getRelayHeaders, relayApi } from "@/lib/api/utils"
import { getSession } from "@/lib/auth/actions"
import {
  PlaybackAlbum,
  PlaybackTrack,
  PlayerStatus,
  usePlayerStore,
  type PlayerStoreInit,
} from "@/lib/stores/player"

interface SpotifyState {
  deviceId?: string
  headers?: Record<string, string>
}

function SpotifyScript() {
  const queryClient = useQueryClient()
  const initialize = usePlayerStore(state => state.initialize)
  const setStatus = usePlayerStore(state => state.setStatus)

  // State that are only defined & used in the context of Spotify SDK.
  const playerRef = useRef<Spotify.Player>(undefined)
  const stateRef = useRef<SpotifyState>({})

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
      // Update state ref in case we need it for reconnection
      stateRef.current.headers = getRelayHeaders(cachedData)
      return cachedAccess.token
    }

    // Fetch new token from API server and update useQuery cache.
    console.debug("Token is missing or expired. Reissuing token...")
    const data = await queryClient.fetchQuery(options)
    stateRef.current.headers = getRelayHeaders(data)

    return data?.streamingAccess?.token
  }, [queryClient])

  const handleReady = useCallback(() => {
    if (playerRef.current) return

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

      addEventListeners(player, stateRef)
      player.connect().then(async (success: boolean) => {
        if (success) {
          initialize(getPlayerStoreInit(player, stateRef))
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

    // If the component is remounted after the script has already loaded,
    // the SDK won't call the function again, so we call it manually here.
    if (window.Spotify) {
      window.onSpotifyWebPlaybackSDKReady()
    }
  }, [getToken, initialize])

  useEffect(() => {
    return () => {
      if (playerRef.current) {
        console.debug("Disconnecting Spotify player...")
        playerRef.current?.disconnect()
        stateRef.current = {}
        setStatus(PlayerStatus.OFFLINE)
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
  stateRef: RefObject<SpotifyState>,
) => {
  // Setters never change after store creation and won't trigger stale closure issues.
  const { setStatus, setPlayback } = usePlayerStore.getState()

  player.addListener(
    "ready",
    async ({ device_id: deviceId }: Spotify.WebPlaybackInstance) => {
      try {
        stateRef.current.deviceId = deviceId
        setStatus(PlayerStatus.ONLINE)
      } catch (error) {
        if (isHTTPError(error)) {
          console.debug("Failed to check current connection:", error)
          setStatus(PlayerStatus.ONLINE)
        }
      }
    },
  )

  player.addListener(
    "not_ready",
    ({ device_id: deviceId }: Spotify.WebPlaybackInstance) => {
      console.debug("not ready")
      stateRef.current.deviceId = deviceId
      setStatus(PlayerStatus.OFFLINE)
    },
  )

  player.addListener(
    "player_state_changed",
    async (state: Spotify.PlaybackState) => {
      const { status, setStatus } = usePlayerStore.getState()
      if (state === null) {
        // TODO: do something
        setStatus(PlayerStatus.OFFLINE)
        return
      }
      // We can now guarantee that this player is fully ready to play.
      if (status === PlayerStatus.CONNECTING) {
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
    const { status, connect } = usePlayerStore.getState()
    if (status == PlayerStatus.CONNECTING) {
      connect()
      toast.error(
        "Server is still trying to connect to Spotify... Please try again later.",
      )
    } else {
      toast.error("Nothing is queued; add songs to play!")
      console.error("Failed to perform playback:", message)
    }
  })
}

const getPlayerStoreInit = (
  player: Spotify.Player,
  stateRef: RefObject<SpotifyState>,
): PlayerStoreInit => {
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
    connect: async () => {
      const deviceId = stateRef.current.deviceId
      if (!deviceId) {
        console.error(
          `One of following are missing in PlayerStore. (deviceId=${deviceId})`,
        )
        return
      }

      // No need to wait since the server returns 202 Accepted even on success.
      usePlayerStore.setState({ status: PlayerStatus.CONNECTING })
      try {
        // NOTE: It's okay to directly call relay server here, since `getToken` assures
        //    that spotify access token is valid while Spotify.Player instance is alive.
        await relayApi.put("spotify/connect", {
          json: { deviceId },
          headers: stateRef.current.headers,
        })
      } catch (e) {
        console.error("Failed to connect jukebox:", e)
      }
    },

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
