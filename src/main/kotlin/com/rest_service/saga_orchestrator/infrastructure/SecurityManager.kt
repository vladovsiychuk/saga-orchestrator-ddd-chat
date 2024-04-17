package com.rest_service.saga_orchestrator.infrastructure

import com.rest_service.commons.client.ViewServiceFetcher
import com.rest_service.commons.dto.UserDTO
import com.rest_service.read_service.exception.UnauthorizedException
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Named
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono

@Singleton
@Named("SecurityManager_sagaOrchestrator")
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
