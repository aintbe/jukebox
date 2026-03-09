"use client"

import { motion } from "framer-motion"
import { Pause, Play, SkipBack, SkipForward } from "lucide-react"
import { usePlayerStore } from "@/lib/stores/player"
import { cn } from "@/lib/utils"
import { ExpandableProps } from "@/components/AnchoredDrawer"
import { IconButton } from "@/components/IconButton"
import { VolumeSlider } from "./VolumeSlider"

export function PlayerController({ isExpanded }: ExpandableProps) {
  const isPlaying = usePlayerStore(state => state.playback?.isPlaying)
  const playPrevious = usePlayerStore(state => state.playPrevious)
  const togglePlay = usePlayerStore(state => state.togglePlay)
  const playNext = usePlayerStore(state => state.playNext)

  const handleClick = () => {
    // TODO: rate limit needed
    console.log("clicked!")
  }

  return (
    <motion.div
      layout
      onClick={handleClick}
      className={cn("gap-2", {
        flex: !isExpanded,
        "col-start-2 grid grid-cols-[1fr_1fr_1.5fr_1fr_1fr]": isExpanded,
      })}
    >
      {isExpanded && (
        <IconButton
          onClick={playPrevious}
          icon={SkipBack}
          size={28}
          className="col-start-2 justify-self-end"
          fill
        />
      )}
      <IconButton
        onClick={togglePlay}
        icon={isPlaying ? Pause : Play}
        size={isExpanded ? 40 : 28}
        className="justify-self-center"
        fill
      />
      {isExpanded && (
        <IconButton
          onClick={playNext}
          icon={SkipForward}
          size={28}
          className="justify-self-start"
          fill
        />
      )}
      <VolumeSlider
        isExpanded={isExpanded}
        size={isExpanded ? 28 : 32}
        className="justify-self-end pr-0"
        fill={isExpanded ? false : true}
      />
    </motion.div>
  )
}
