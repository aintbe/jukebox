import Link from "next/link"
import { cn } from "@/lib/utils"

type CommonElementProps = React.ComponentPropsWithoutRef<"span"> &
  React.ComponentPropsWithoutRef<"a">

interface Props extends CommonElementProps {
  href?: string
  children?: React.ReactNode
}

export function PossibleLink({ href, children, className, ...props }: Props) {
  return href ? (
    <Link
      {...props}
      href={href}
      target="_blank"
      rel="noopener noreferrer"
      className={cn("hover:underline", className)}
    >
      {children}
    </Link>
  ) : (
    <span {...props} className={className}>
      {children}
    </span>
  )
}
