import { SignIntoStreamingService } from "@/components/auth/SignIntoStreamingService"
import { AuthDialog } from "../_components/AuthDialog"

export default function SignUp() {
  return (
    <AuthDialog>
      <SignIntoStreamingService />
    </AuthDialog>
  )
}
