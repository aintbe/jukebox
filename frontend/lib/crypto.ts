import { Session, SessionPromise } from "@/types/app"

const CRYPTO_ALGORITHM = "AES-GCM"
const IV_LENGTH = 12

const encoder = new TextEncoder()
const decoder = new TextDecoder()

const cryptoKey = (async () => {
  const keyData = await crypto.subtle.digest(
    "SHA-256",
    encoder.encode(process.env.NEXT_SESSION_SECRET),
  )
  return await crypto.subtle.importKey(
    // [MDN Reference](https://developer.mozilla.org/docs/Web/API/SubtleCrypto/importKey)
    "raw",
    keyData,
    CRYPTO_ALGORITHM,
    false,
    ["encrypt", "decrypt"],
  )
})()

export async function encryptSession(session: Session) {
  const salt = crypto.getRandomValues(new Uint8Array(IV_LENGTH))
  const text = JSON.stringify(session)

  const encrypted = await crypto.subtle.encrypt(
    { name: CRYPTO_ALGORITHM, iv: salt },
    await cryptoKey,
    // Need to convert text into bytes first.
    encoder.encode(text),
  )

  const buffer = Buffer.concat([Buffer.from(salt), Buffer.from(encrypted)])
  return buffer.toString("base64url")
}

export async function decryptSession(cookie: string): SessionPromise {
  try {
    const buffer = Buffer.from(cookie, "base64url")
    const salt = buffer.subarray(0, IV_LENGTH)
    const encrypted = buffer.subarray(IV_LENGTH)

    const decrypted = await crypto.subtle.decrypt(
      { name: CRYPTO_ALGORITHM, iv: salt },
      await cryptoKey,
      encrypted,
    )

    const text = decoder.decode(decrypted)
    return JSON.parse(text)
  } catch (e) {
    console.error("Failed to decrypt session:", e)
  }
}
