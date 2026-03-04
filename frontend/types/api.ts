import { StreamingService } from "./app"

export interface ApiData<T> {
  data: T
}

export interface ApiMessage {
  message: string
}

export interface ApiError {
  error: string
  message: string
}

export interface UserProfile {
  id: string
  username: string
  streamingAccess?: StreamingAccess
  jukebox?: {
    id: number
    handle: string
  }
}

export interface StreamingAccess {
  serviceName: StreamingService
  token: string
  expiresAt: string | null // Never expires if null
}
