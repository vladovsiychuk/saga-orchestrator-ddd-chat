package com.rest_service.websocket_service.configuration

import io.micronaut.http.HttpRequest
import io.micronaut.security.token.reader.TokenReader
import jakarta.inject.Singleton
import java.util.Optional

@Singleton
class AccessTokenReader : TokenReader {
    override fun findToken(request: HttpRequest<*>?): Optional<String> {
        return request!!.parameters.get("access_token", String::class.java)
    }
}
