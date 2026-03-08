// <Providers> must be a client component since server components cannot give
// a class instance (in this case `QueryClient`) to client components.
"use client"

import { Session } from "@/types/app"
import { QueryClient, QueryClientProvider } from "@tanstack/react-query"
import { ReactQueryDevtools } from "@tanstack/react-query-devtools"
import { SessionProvider } from "@/lib/providers/SessionProvider"
import { TooltipProvider } from "@/components/ui/tooltip"

interface Props {
  children: React.ReactNode
  session: Session | undefined
}

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { refetchOnWindowFocus: false },
  },
})

export function Providers({ children, session }: Props) {
  return (
    <SessionProvider initialSession={session}>
      <QueryClientProvider client={queryClient}>
        {process.env.NODE_ENV === "development" && (
          <ReactQueryDevtools initialIsOpen={false} />
        )}
        <TooltipProvider>{children}</TooltipProvider>
      </QueryClientProvider>
    </SessionProvider>
  )
}
