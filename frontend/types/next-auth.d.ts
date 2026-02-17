import type { DefaultSession, DefaultUser } from "next-auth"
import "next-auth/jwt"

interface AuthUser {
  userId: string
  username: string
}

interface AuthToken {
  accessToken: string
  accessExpiresAt: number
  // TODO: 필요하면 추가하기
  // refreshToken: string;
  // refreshExpiresIn: number;
}

declare module "next-auth" {
  interface User extends DefaultUser, AuthUser, AuthToken {}
  interface Session extends DefaultSession {
    user: AuthUser
    token: AuthToken
  }
  interface JWT extends AuthUser, AuthToken {}
}

declare module "next-auth/jwt" {
  interface JWT extends AuthUser, AuthToken {}
}
