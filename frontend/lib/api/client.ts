import { getSession } from "../auth/actions"
import { createAuthApi } from "./utils"

/**
 * Ky instace with Authorization information.
 */
export const api = createAuthApi(getSession)
