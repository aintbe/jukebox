/**
 * Interface to represent Props for server pages/layouts.
 * @interface
 */
export interface ServerPageProps<K extends string = never> {
  params: Promise<Record<K, string>>
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>
}
