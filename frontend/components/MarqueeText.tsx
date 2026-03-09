"use client"

import { useRef, useState } from "react"
import Marquee from "react-fast-marquee"
import { useResizeObserver } from "@/lib/hooks/useResizeObserver"
import { cn } from "@/lib/utils"

interface Props {
  text: string
  speed: number
  className?: string
}

export function MarqueeText({ text, speed, className }: Props) {
  const [isOverflow, setIsOverflow] = useState(false)

  const textRef = useRef<HTMLSpanElement>(null)
  const containerRef = useResizeObserver<HTMLDivElement>({
    onResize: ({ width }) => {
      if (width && textRef.current) {
        setIsOverflow(width < textRef.current.scrollWidth)
      }
    },
  })

  return (
    <div
      ref={containerRef}
      className={cn({ "overflow-hidden whitespace-nowrap": !isOverflow })}
    >
      {isOverflow ? (
        <Marquee
          key={`marquee-text-${isOverflow}`}
          play={isOverflow}
          speed={speed}
          pauseOnHover
          className="z-0"
        >
          <span ref={textRef} className={cn("mr-8 inline-block", className)}>
            {text}
          </span>
        </Marquee>
      ) : (
        <span ref={textRef} className={cn("inline-block", className)}>
          {text}
        </span>
      )}
    </div>
  )
}
