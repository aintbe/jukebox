import { jwtDecode, JwtPayload } from "jwt-decode"
import NextAuth, { JWT, Session, User } from "next-auth"
import Credentials from "next-auth/providers/credentials"

export const NEXT_AUTH_PROVIDER = {
  API: "credentials",
  DSP: "dsp-credentials",
}

export const { handlers, auth, signIn, signOut } = NextAuth({
  providers: [
    Credentials({
      id: NEXT_AUTH_PROVIDER.API,
      name: "로그인",
      credentials: {
        username: { type: "text" },
        password: { type: "password" },
      },
      // `signIn` invokes this function to fetch user from credentials.
      // Returns `User` if authentication is successful.
      authorize: ({ username, password }) => {
        // TODO: 실제 인증 로직 구현 (예: DB 조회, 외부 API 호출 등)
        return null
      },
    }),
    Credentials({
      id: NEXT_AUTH_PROVIDER.DSP,
      name: "스트리밍 서비스로 로그인",
      credentials: {
        accessToken: { label: "access token", type: "text" },
      },
      authorize: async ({ accessToken }) => {
        if (typeof accessToken !== "string" || !accessToken) {
          return null
        }

        const decoded: JWT & JwtPayload = jwtDecode(accessToken)
        const user: User = {
          userId: decoded.userId,
          username: decoded.username,
          accessToken,
          accessExpiresAt: decoded.exp ? decoded.exp * 1000 : 0,
        }

        return Object.values(user).every(Boolean) ? user : null
      },
    }),
  ],
  callbacks: {
    jwt: async ({ token, user }) => {
      // Invalidate the token if it's expired.
      if (token && Date.now() >= token.accessExpiresAt) {
        return null
      }
      // Replace token with user data right during sign in.
      return user ? { ...user } : token
    },
    session: async ({ session, token }: { session: Session; token: JWT }) => {
      if (!token) return session

      session.user = {
        userId: token.userId,
        username: token.username,
      }
      session.token = {
        accessToken: token.accessToken,
        accessExpiresAt: token.accessExpiresAt,
      }
      return session
    },
  },
})
