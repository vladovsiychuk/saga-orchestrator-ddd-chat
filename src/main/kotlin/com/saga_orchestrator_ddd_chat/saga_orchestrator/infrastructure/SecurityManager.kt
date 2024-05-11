package com.saga_orchestrator_ddd_chat.saga_orchestrator.infrastructure

import com.saga_orchestrator_ddd_chat.commons.client.ViewServiceFetcher
import com.saga_orchestrator_ddd_chat.commons.dto.UserDTO
import com.saga_orchestrator_ddd_chat.read_service.exception.UnauthorizedException
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono

@Singleton
open class SecurityManager(
    private val securityService: SecurityService,
    private val viewFetcher: ViewServiceFetcher,
) {
    open fun getCurrentUserEmail(): String {
        if (!securityService.authentication.isPresent)
            throw RuntimeException("No valid credential found")

        return securityService.authentication.get().attributes["email"].toString()
    }

    open fun getCurrentUser(): Mono<UserDTO> {
        return viewFetcher.getCurrentUser()
            .switchIfEmpty { UnauthorizedException().toMono() }
    }
}
