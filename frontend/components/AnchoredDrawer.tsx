"use client"

import { useLayoutEffect, useMemo, useRef, useState } from "react"
import { cn, getTwSpacing } from "@/lib/utils"
import { DialogOverlay } from "@/components/ui/dialog"
import {
  Drawer,
  DrawerContent,
  DrawerHandle,
  DrawerPortal,
} from "@/components/ui/drawer"

export type SnapPoint = number | `${number}px`

interface Props {
  snapPoints: SnapPoint[] // Must have at least one element in it.
  expandedSnapPoint: SnapPoint
  className?: string
  children: (props: ExpandableProps) => React.ReactNode
}

export interface ExpandableProps {
  isExpanded: boolean
}

export function AnchoredDrawer({
  snapPoints,
  expandedSnapPoint,
  className,
  children,
}: Props) {
  const [snap, setSnap] = useState<string | number | null>(
    snapPoints.at(0) ?? null,
  )
  const overlayRef = useRef<HTMLDivElement>(null)

  const isExpanded = useMemo(
    () => snap === expandedSnapPoint,
    [expandedSnapPoint, snap],
  )
  useLayoutEffect(() => {
    const overlay = overlayRef.current
    if (!overlay) return

    // Show interaction with other parts of window only if the drawer is expanded.
    overlay.style.opacity = isExpanded ? "1" : "0"
    overlay.style.pointerEvents = isExpanded ? "auto" : "none"
  }, [isExpanded])

  return (
    <Drawer
      defaultOpen
      dismissible={false} // Cannot close this drawer.
      modal={false} // Allow interaction with other parts of window by default.
      snapPoints={snapPoints}
      activeSnapPoint={snap}
      setActiveSnapPoint={setSnap}
    >
      <DrawerPortal>
        <DialogOverlay
          ref={overlayRef}
          className={cn("opacity-0 transition-all duration-500", {
            "pointer-events-none!": !isExpanded,
          })}
        />
        <DrawerContent
          className={cn("top-0", className)}
          style={{
            marginTop: `calc(${MARGIN_TOP})`,
            height: `calc(${typeof snap === "number" ? `${snap * 100}%` : (snap ?? "0%")} - ${MARGIN_TOP})`,
          }}
        >
          <DrawerHandle className="-mt-6 mb-4 w-12" />
          {children({ isExpanded })}
        </DrawerContent>
      </DrawerPortal>
    </Drawer>
  )
}

const MARGIN_TOP = getTwSpacing(20)
