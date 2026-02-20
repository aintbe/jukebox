package com.jukebox.backend.user

import com.jukebox.backend.auth.dto.AuthUser
import com.jukebox.backend.user.dto.UserDto
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
@PreAuthorize("isAuthenticated()")
class UserController(
    private val userService: UserService,
) {
    /**
     * Retrieves the user's profile information along with the latest
     * streaming service access information.
     *
     * @param user The details of the currently authenticated user,
     * injected via [AuthenticationPrincipal].
     * @param authorizedClient The OAuth2 authorized client managed
     * by Spring Security. If the current access token is expired,
     * Spring Security automatically reissues it using the stored
     * refresh token during the injection process, thus ensuring
     * that the retrieved access token is always valid.
     *
     * @return A [UserDto.UserProfileResponse] containing the user profile
     * and the (potentially) streaming service access information.
     */
    @GetMapping()
    fun getProfile(
        @AuthenticationPrincipal user: AuthUser,
        // This automatically reissues access token if current one's expired.
        @RegisteredOAuth2AuthorizedClient("spotify") authorizedClient: OAuth2AuthorizedClient?,
    ): UserDto.UserProfileResponse? {
        val profile = userService.getUserProfile(user.userId)
        val accessToken = authorizedClient?.accessToken
        return UserDto.UserProfileResponse(profile, accessToken)
    }
}
