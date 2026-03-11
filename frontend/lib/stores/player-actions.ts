import { PayloadIn, PayloadOut } from "@/types/websocket"
import { create } from "zustand"
import { subscribeWithSelector } from "zustand/middleware"
import { WEBSOCKET_EVENT as EVENT } from "../constants"
import { initializeWebsocketStore, WebSocketStore } from "./websocket"

export const handleMessage = (event: MessageEvent<PayloadIn>) => {
  const payload = JSON.parse(String(event.data))
  console.log("todo: message came!!!!", payload)
}

interface PlayerActionsStore extends WebSocketStore, PlayerActions {
  setActions: (actions: PlayerActions) => void
}

export interface PlayerActions {
  togglePlay: () => Promise<void>
  playNext: () => Promise<void>
  playPrevious: () => Promise<void>
  playAt: (position: number) => Promise<void>
  setVolume: (volume: number) => Promise<void>
  toggleMute: () => Promise<void>
}

export const usePlayerActionsStore = create<PlayerActionsStore>()(
  subscribeWithSelector((set, get) => ({
    ...initializeWebsocketStore(set, get),
    setActions: actions => {
      // Object.entries(() => {

      // })
      set(actions)
    },

    // Initialized by scripts calling `setActions`.
    togglePlay: async () => {
      // get().send({ event: "PLAY" })
    },
    playNext: async () => undefined,
    playPrevious: async () => undefined,
    playAt: async () => undefined,
    setVolume: async () => undefined,
    toggleMute: async () => undefined,
  })),
)
