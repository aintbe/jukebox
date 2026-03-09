"use client"

import { useState } from "react"
import { Volume, Volume1, Volume2, VolumeX } from "lucide-react"
import { usePlayerStore } from "@/lib/stores/player"
import { ExpandableProps } from "@/components/AnchoredDrawer"
import { IconButton, type IconButtonProps } from "@/components/IconButton"
import { ProgressSlider } from "@/components/ProgressSlider"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"

interface Props extends ExpandableProps, Partial<IconButtonProps> {}

export function VolumeSlider({ isExpanded, ...props }: Props) {
  const [open, setOpen] = useState(false)

  const { volume, isMuted } = usePlayerStore(state => state.device)
  const setVolume = usePlayerStore(state => state.setVolume)
  const toggleMute = usePlayerStore(state => state.toggleMute)

  if (!isExpanded && open) {
    setOpen(false)
  }

  return (
    <Popover open={open}>
      <PopoverTrigger asChild>
        <IconButton
          {...props}
          icon={getIcon(volume, isMuted)}
          onClick={isExpanded ? () => setOpen(prev => !prev) : toggleMute}
        />
      </PopoverTrigger>
      {isExpanded && (
        <PopoverContent className="h-40 w-fit px-2">
          <ProgressSlider
            max={1}
            value={volume}
            onValueChange={v => setVolume(v)}
            vertical
          />
        </PopoverContent>
      )}
    </Popover>
  )
}

const getIcon = (volume: number, isMuted: boolean) => {
  if (isMuted || volume === 0) return VolumeX
  if (volume < 0.33) return Volume
  if (volume < 0.66) return Volume1
  return Volume2
}
