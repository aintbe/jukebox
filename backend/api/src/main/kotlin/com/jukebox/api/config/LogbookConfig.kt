package com.jukebox.api.config

import com.jukebox.core.constants.HttpConstants
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.zalando.logbook.Correlation
import org.zalando.logbook.HttpLogFormatter
import org.zalando.logbook.HttpRequest
import org.zalando.logbook.HttpResponse
import org.zalando.logbook.Precorrelation
import org.zalando.logbook.HttpHeaders as LogbookHttpHeaders

@Configuration
class LogbookConfig {
    @Bean
    @Profile("local")
    fun localHttpLogFormatter(): HttpLogFormatter =

        object : HttpLogFormatter {
            override fun format(
                precorrelation: Precorrelation,
                request: HttpRequest,
            ) = buildString {
                appendLine("HTTP request received:")
                appendLine("${request.method} ${request.path} ${request.protocolVersion}")

                getAuthorization(request.headers)?.also { appendLine(it) }
                getCookie(request.headers)?.also { appendLine(it) }
            }

            override fun format(
                correlation: Correlation,
                response: HttpResponse,
            ) = buildString {
                appendLine("HTTP response returned:")
                appendLine("${response.status} ${response.protocolVersion} ${correlation.duration.toMillis()}ms")

                getAuthorization(response.headers)?.also { appendLine(it) }
                getCookie(response.headers)?.also { appendLine(it) }
                response.bodyAsString.takeIf { it.isNotBlank() }?.also {
                    appendLine("Body: $it")
                }
            }
        }

    private fun getAuthorization(headers: LogbookHttpHeaders) =
        headers[HttpHeaders.AUTHORIZATION]?.firstOrNull()?.takeIf { it.isNotBlank() }?.let {
            // No need to obfuscate, logbook obfuscate it by default.
            "${HttpHeaders.AUTHORIZATION}: $it"
        }

    private fun getCookie(headers: LogbookHttpHeaders): String? =
        headers["Cookie"]
            ?.flatMap { it.split(";") }
            ?.map { it.trim() }
            ?.find { it -> it.startsWith(HttpConstants.REFRESH_TOKEN + "=") }
            ?.let {
                "Cookie: ${
                    obfuscate(it, HttpConstants.REFRESH_TOKEN.length)
                }"
            }

    private fun obfuscate(
        text: String,
        visibleLength: Int,
        paddedLength: Int = 5,
    ): String {
        val visible =
            if (visibleLength + paddedLength < text.length) {
                text.take(visibleLength + paddedLength)
            } else {
                ""
            }
        return "$visible***"
    }
}
