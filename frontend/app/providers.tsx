"use client"

import { SessionProvider } from "@/components/SessionProvider/SessionProvider"
import { Session } from "@/types/app"
import { QueryClient, QueryClientProvider } from "@tanstack/react-query"
import { ReactQueryDevtools } from "@tanstack/react-query-devtools"
import { useState } from "react"

interface Props {
  children: React.ReactNode
  session: Session | undefined
}

export default function Providers({ children, session }: Props) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: { refetchOnWindowFocus: false },
        },
      }),
  )

  return (
    <SessionProvider initializer={session}>
      <QueryClientProvider client={queryClient}>
        {process.env.NODE_ENV === "development" && (
          <ReactQueryDevtools initialIsOpen={false} />
        )}
        {children}
      </QueryClientProvider>
    </SessionProvider>
  )
}
