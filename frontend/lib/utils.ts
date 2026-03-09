import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export const cn = (...inputs: ClassValue[]) => {
  return twMerge(clsx(inputs))
}

/**
 * Tailwind's internal spacing unit.
 * Use with calc() to replicate Tailwind spacing values programmatically.
 *
 * @example
 * ```tsx
 * // Equivalent to Tailwind's m-5
 * <div style={{ margin: `calc(${getTwSpacing(5)})` }} />
 * ```
 */
export const getTwSpacing = (step: number) => `var(--spacing) * ${step}`

export const requireContext = <T>(context: React.Context<T>) => {
  if (!context) {
    throw new Error("React Context is unavailable in Server Components")
  }
}
