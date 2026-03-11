"use client"

import { useEffect, useRef, useState } from "react"
import { useShallow } from "zustand/react/shallow"
import { MIN_IN_SEC, SEC_IN_MS } from "@/lib/constants"
import { usePlayerStore } from "@/lib/stores/player"
import { usePlayerActionsStore } from "@/lib/stores/player-actions"
import { ProgressSlider } from "@/components/ProgressSlider"

interface Props {
  className?: string
  disabled?: boolean
  withoutEndpoints?: boolean
}

export function PlayerProgress({
  className,
  disabled,
  withoutEndpoints,
}: Props) {
  const state = usePlayerStore(
    useShallow(state =>
      state.playback
        ? {
            timestamp: state.playback.timestamp,
            position: state.playback.position,
            duration: state.playback.duration,
            isPlaying: state.playback.isPlaying,
          }
        : undefined,
    ),
  )

  const [percent, setPercent] = useState<number>()
  const requestRef = useRef<number>(undefined)
  useEffect(() => {
    const animate = () => {
      if (!state) setPercent(undefined)
      else {
        const elapsed = Date.now() - state.timestamp
        setPercent(state.position + elapsed)

        if (state.isPlaying) {
          requestRef.current = requestAnimationFrame(animate)
        }
      }
    }

    requestRef.current = requestAnimationFrame(animate)
    return () => {
      if (requestRef.current) cancelAnimationFrame(requestRef.current)
    }
  }, [state])

  const playAt = usePlayerActionsStore(state => state.playAt)
  const togglePlay = usePlayerActionsStore(state => state.togglePlay)

  const handleValueChange = (position: number) => {
    playAt(position)
    if (state && !state.isPlaying) togglePlay()
  }

  return (
    <div className={className}>
      <ProgressSlider
        max={state?.duration}
        value={percent}
        onValueChange={disabled ? undefined : handleValueChange}
        tooltipContent={stringifyTime}
      />
      {!withoutEndpoints && state?.duration && (
        <div className="flex justify-between">
          <div>{startTime}</div>
          <div>{stringifyTime(state.duration)}</div>
        </div>
      )}
    </div>
  )
}

/**
 * @param time time in ms
 * @returns "mm:ss"
 */
const stringifyTime = (time: number) => {
  const totalSeconds = time / SEC_IN_MS
  const minutes = Math.floor(totalSeconds / MIN_IN_SEC)
  const seconds = Math.floor(totalSeconds % MIN_IN_SEC)

  const padNumber = (n: number) => n.toString().padStart(2, "0")
  return `${padNumber(minutes)}:${padNumber(seconds)}`
}
const startTime = stringifyTime(0)
