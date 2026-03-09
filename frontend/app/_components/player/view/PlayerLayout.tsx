"use client"

import { motion } from "framer-motion"
import { PlayerStatus, usePlayerStore } from "@/lib/stores/player"
import { cn } from "@/lib/utils"
import { AnchoredDrawer, SnapPoint } from "@/components/AnchoredDrawer"
import { Button } from "@/components/ui/button"
import { CurrentTrack } from "./CurrentTrack"
import { PlayerController } from "./PlayerController"
import { PlayerProgress } from "./PlayerProgress"

export function PlayerLayout() {
  const status = usePlayerStore(state => state.status)
  const connect = usePlayerStore(state => state.connect)

  return (
    <AnchoredDrawer
      snapPoints={SNAP_POINTS}
      expandedSnapPoint={EXPANDED_SNAP_POINT}
      className="rounded-t-4xl"
    >
      {({ isExpanded }) => (
        <div className="relative h-full">
          <div className="flex h-full flex-col">
            {!isExpanded && (
              <motion.div layoutId="playback-progress">
                <PlayerProgress className="mx-5" withoutEndpoints disabled />
              </motion.div>
            )}
            <div
              className={cn("flex items-center", {
                "grow gap-3 p-4": !isExpanded,
                "mx-10 flex-col items-stretch gap-7": isExpanded,
              })}
            >
              <CurrentTrack isExpanded={isExpanded} />
              {isExpanded && (
                <motion.div layoutId="playback-progress">
                  <PlayerProgress />
                </motion.div>
              )}
              <PlayerController isExpanded={isExpanded} />
            </div>
          </div>
          {status <= PlayerStatus.ONLINE && (
            <div
              className="absolute inset-0 flex items-center justify-center backdrop-blur-xs"
              onPointerDown={e => e.stopPropagation()} // Prevent dragging
            >
              <Button onClick={connect}>Use Jukebox Player Right Now</Button>
            </div>
          )}
        </div>
      )}
    </AnchoredDrawer>
  )
}

const EXPANDED_SNAP_POINT = 1 as const
const SNAP_POINTS: SnapPoint[] = ["184px", EXPANDED_SNAP_POINT]
