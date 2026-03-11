"use client"

import { motion } from "framer-motion"
import Image from "next/image"
import { IMAGE } from "@/lib/constants/app"
import { PlaybackArtist, usePlayerStore } from "@/lib/stores/player"
import { cn } from "@/lib/utils"
import { ExpandableProps } from "@/components/AnchoredDrawer"
import { MarqueeText } from "@/components/MarqueeText"
import { PossibleLink } from "./PossibleLink"

export function CurrentTrack({ isExpanded }: ExpandableProps) {
  const track = usePlayerStore(state => state.playback?.currentTrack)
  return (
    <>
      <motion.div
        layout
        className={cn("overflow-hidden", {
          "aspect-square shrink-0 self-stretch rounded-lg": !isExpanded,
          "mt-10 rounded-3xl drop-shadow-2xl min-[320px]:w-70 min-[320px]:self-center":
            isExpanded,
        })}
      >
        <PossibleLink
          href={isExpanded && track ? track.album.url : undefined}
          className="relative block aspect-square w-full"
        >
          <Image
            src={
              track
                ? (track.album.image?.url ?? IMAGE.DEFAULT_TRACK)
                : placeholderTrack.imageUrl
            }
            alt={track ? track.name : "track-image-preview"}
            className="object-cover"
            fill
            unoptimized
          />
        </PossibleLink>
      </motion.div>
      <motion.div
        layout
        className={cn("overflow-hidden", {
          "shrink grow": !isExpanded,
        })}
      >
        <MarqueeText
          text={track ? track.name : placeholderTrack.name}
          className="font-semibold"
          speed={30}
        />
        <p className="text-muted-foreground truncate whitespace-nowrap">
          {track
            ? renderArtists(track.artists)
            : renderArtists(placeholderTrack.artists)}
        </p>
      </motion.div>
    </>
  )
}

const renderArtists = (artists: PlaybackArtist[]) =>
  artists.map(({ id, name, url }, index) => (
    <span key={id}>
      <PossibleLink href={url}>{name}</PossibleLink>
      {index !== artists.length - 1 && ", "}
    </span>
  ))

// Placeholder track data displayed before playback data is loaded.
// Values are static and non-interactive.
const placeholderTrack = {
  imageUrl: "https://i.scdn.co/image/ab67616d0000b2735076e4160d018e378f488c33",
  name: "Fortnight (feat. Post Malone)",
  artists: [
    { id: "artist-placeholder-1", name: "Taylor Swift", url: "" },
    { id: "artist-placeholder-2", name: "Post Malone", url: "" },
  ],
}
