import { ForwardRefExoticComponent, RefAttributes } from "react"
import { LucideProps } from "lucide-react"
import { cn } from "@/lib/utils"

interface Props extends React.ComponentPropsWithoutRef<"button"> {
  icon: ForwardRefExoticComponent<
    Omit<LucideProps, "ref"> & RefAttributes<SVGSVGElement>
  >
  className?: string
  size?: number
  fill?: boolean
}

export function IconButton({
  icon: Icon,
  className,
  size,
  fill,
  ...props
}: Props) {
  return (
    // TODO: change to <Button />?
    <button
      {...props}
      className={cn(
        "cursor-pointer p-2 transition-all duration-200 hover:scale-105 hover:opacity-70",
        className,
      )}
    >
      <Icon size={size} fill={fill ? "text" : "none"} />
    </button>
  )
}

export type { Props as IconButtonProps }
