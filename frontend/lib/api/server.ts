import "server-only"
import { getServerSession } from "../auth/server"
import { createAuthApi } from "./utils"

/**
 * Ky instace with Authorization information.
 */
export const serverApi = createAuthApi(getServerSession)
