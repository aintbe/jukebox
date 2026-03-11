import { PayloadIn, PayloadOut } from "@/types/websocket"

export interface WebSocketStore<I = PayloadIn, O = PayloadOut> {
  socket: WebSocket | undefined
  isConnected: boolean
  connect: (socket: WebSocket) => void
  disconnect: () => void
  send: (payload: O) => void
}

export const initializeWebsocketStore = <I, O>(
  set: (partial: Pick<WebSocketStore<I, O>, "socket" | "isConnected">) => void,
  get: () => Pick<WebSocketStore<I, O>, "socket" | "isConnected">,
): WebSocketStore<I, O> => ({
  socket: undefined,
  isConnected: false,
  connect: socket => set({ socket, isConnected: true }),
  disconnect: () => set({ socket: undefined, isConnected: true }),
  send: (payload: O) => {
    get().socket?.send(JSON.stringify(payload))
  },
})
