package com.saga_orchestrator_ddd_chat.websocket_service.configuration

import io.micronaut.http.HttpRequest
import io.micronaut.security.token.reader.TokenReader
import jakarta.inject.Singleton
import java.util.Optional

@Singleton
class AccessTokenReader : TokenReader<HttpRequest<*>> {
    override fun findToken(request: HttpRequest<*>?): Optional<String> {
        return request!!.parameters.get("access_token", String::class.java)
    }
}
