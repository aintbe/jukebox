import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export const cn = (...inputs: ClassValue[]) => {
  return twMerge(clsx(inputs))
}

export const requireContext = <T>(context: React.Context<T>) => {
  if (!context) {
    throw new Error("React Context is unavailable in Server Components")
  }
}
