"use client"

import { useMemo, useState } from "react"
import { AnimatePresence, motion } from "framer-motion"
import { cn, getTwSpacing } from "@/lib/utils"
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

  const isExpanded = useMemo(
    () => snap === expandedSnapPoint,
    [expandedSnapPoint, snap],
  )

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
        <AnimatePresence>
          {isExpanded && (
            <motion.div
              layout={false}
              aria-hidden
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black/80"
            />
          )}
        </AnimatePresence>
        <DrawerContent
          className={cn(
            "dark:bg-primary dark:text-primary-foreground top-0",
            className,
          )}
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
