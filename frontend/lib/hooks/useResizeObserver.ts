import { useEffect, useRef } from "react"

interface Option {
  onResize?: (size: { width: number; height: number }) => void
}

export function useResizeObserver<T extends HTMLElement = HTMLElement>({
  onResize,
}: Option) {
  const ref = useRef<T>(null)
  const onResizeRef = useRef(onResize)

  useEffect(() => {
    onResizeRef.current = onResize
  })

  useEffect(() => {
    if (!ref.current) return

    const observer = new ResizeObserver(([entry]) => {
      const { width, height } = entry.contentRect
      onResizeRef.current?.({ width, height })
    })

    observer.observe(ref.current)
    return () => observer.disconnect()
  }, [])

  return ref
}
