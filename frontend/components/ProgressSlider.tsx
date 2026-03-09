"use client"

import * as React from "react"
import * as ProgressPrimitive from "@radix-ui/react-progress"
import { cn } from "@/lib/utils"
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip"

type Props = React.ComponentPropsWithoutRef<typeof ProgressPrimitive.Root> & {
  vertical?: boolean
  onValueChange?: (newValue: number) => void
  tooltipContent?: (hoveredValue: number) => string
}

const ProgressSlider = React.forwardRef<
  React.ElementRef<typeof ProgressPrimitive.Root>,
  Props
>(
  (
    {
      className,
      max,
      value,
      vertical = false,
      onValueChange,
      tooltipContent,
      ...props
    },
    ref,
  ) => {
    const [hoveredPercent, setHoveredPercent] = React.useState<number>()
    const isLoading = value === null || value === undefined || max === undefined
    const disabled = isLoading || !onValueChange

    const handleHover = (e: React.MouseEvent<HTMLDivElement>) => {
      if (disabled) return
      setHoveredPercent(findMousePercent(e, vertical))
    }

    const handleClick = (e: React.MouseEvent<HTMLDivElement>) => {
      if (disabled) return
      const percent = findMousePercent(e, vertical)
      onValueChange((percent * (max || 100)) / 100)
    }

    const percent = isLoading ? 0 : (value / max) * 100
    return (
      <div
        className={cn("group pointer-events-none relative", className, {
          "pointer-events-auto cursor-pointer": !disabled,
          "h-fit w-full": !vertical,
          "py-2": !vertical && !disabled,
          "h-full w-fit": vertical,
          "px-2": vertical && !disabled,
        })}
        onMouseMove={handleHover}
        onMouseLeave={() => setHoveredPercent(undefined)}
        onClick={handleClick}
      >
        <ProgressPrimitive.Root
          {...props}
          ref={ref}
          className={cn("bg-muted relative z-3 overflow-hidden rounded-full", {
            "h-2 w-full": !vertical,
            "h-full w-2": vertical,
          })}
          max={max}
        >
          <ProgressPrimitive.Indicator
            className="bg-primary h-full w-full flex-1 rounded transition-all"
            style={{
              transform: vertical
                ? `translateY(${100 - percent}%)`
                : `translateX(-${100 - percent}%)`,
            }}
          />
          <div
            className={cn("absolute bg-gray-500", {
              "top-0 left-0 h-full": !vertical,
              "bottom-0 left-0 w-full": vertical,
            })}
            style={{
              [vertical ? "height" : "width"]: `${hoveredPercent ?? 0}%`,
              zIndex: percent >= (hoveredPercent ?? 0) ? 0 : -1,
            }}
          />
        </ProgressPrimitive.Root>
        {!disabled && hoveredPercent !== undefined && (
          <Tooltip open={Boolean(tooltipContent)}>
            <TooltipTrigger asChild>
              <div
                className={cn(
                  "bg-primary absolute z-3 h-3 w-3 rounded-full opacity-0 transition-opacity group-hover:opacity-100",
                  {
                    "top-1/2 -translate-1/2": !vertical,
                    "left-1/2 -translate-x-1/2 translate-y-1/2": vertical,
                  },
                )}
                style={{
                  [vertical ? "bottom" : "left"]: `${hoveredPercent ?? 0}%`,
                }}
              />
            </TooltipTrigger>
            <TooltipContent side="bottom">
              {tooltipContent?.((hoveredPercent * max) / 100)}
            </TooltipContent>
          </Tooltip>
        )}
      </div>
    )
  },
)
ProgressSlider.displayName = "ProgressSlider"
export { ProgressSlider }

const findMousePercent = (
  e: React.MouseEvent<HTMLDivElement>,
  vertical: boolean,
) => {
  const bar = e.currentTarget.getBoundingClientRect()
  const offset = vertical ? bar.bottom - e.clientY : e.clientX - bar.left

  return Math.max(
    0,
    Math.min((offset / (vertical ? bar.height : bar.width)) * 100, 100),
  )
}
