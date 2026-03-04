import ky, { HTTPError } from "ky"
import { apiDomain } from "../constants"
import { SessionPromise } from "@/types/app"
import { ApiError } from "@/types/api"

export const publicApi = ky.create({
  prefixUrl: apiDomain,
  retry: 0,
  timeout: 5000,
})

export const createAuthApi = (getSession: () => SessionPromise) => {
  return publicApi.extend({
    hooks: {
      beforeRequest: [
        async (request) => {
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

export const onHttpStatus = (
  error: unknown,
  status: number,
): error is HTTPError<ApiError> =>
  error instanceof HTTPError && error.response.status === status

export const requireContext = <T>(context: React.Context<T>) => {
  if (!context) {
    throw new Error("React Context is unavailable in Server Components")
  }
}
