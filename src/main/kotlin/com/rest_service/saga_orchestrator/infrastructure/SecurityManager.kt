package com.rest_service.saga_orchestrator.infrastructure

import com.rest_service.commons.dto.UserDTO
import com.rest_service.read_service.exception.UnauthorizedException
import com.rest_service.websocket_service.client.ViewServiceFetcher
import io.micronaut.context.annotation.Primary
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono

@Singleton
@Primary
open class SecurityManager(
    private val securityService: SecurityService,
    private val viewFetcher: ViewServiceFetcher,
) {
    open fun getCurrentUserEmail(): String {
        if (!securityService.authentication.isPresent)
            throw RuntimeException("No valid credential found")

        return securityService.authentication.get().attributes["email"].toString()
    }

    open fun getCurrentUserIdAndEmail(): Mono<Pair<UUID, String>> {
        return viewFetcher.getCurrentUser()
            .switchIfEmpty { UnauthorizedException().toMono() }
            .map { it.id to it.email }
    }

    open fun getCurrentUser(): Mono<UserDTO> {
        return viewFetcher.getCurrentUser()
            .switchIfEmpty { UnauthorizedException().toMono() }
    }
}
