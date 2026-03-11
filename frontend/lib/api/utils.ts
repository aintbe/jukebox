import { SessionPromise } from "@/types/app"
import ky, { isHTTPError } from "ky"
import { API_DOMAIN } from "../constants"

export const publicApi = ky.create({
  prefixUrl: API_DOMAIN,
  retry: 0,
  timeout: 5000,
})

export const createAuthApi = (getSession: () => SessionPromise) => {
  return publicApi.extend({
    hooks: {
      beforeRequest: [
        async request => {
          const session = await getSession()
          if (session?.accessToken) {
            request.headers.set(
              "Authorization",
              `Bearer ${session.accessToken}`,
            )
          }
        },
      ],
    },
  })
}

export const onHttpStatus = (error: unknown, status: number) =>
  isHTTPError(error) && error.response.status === status
