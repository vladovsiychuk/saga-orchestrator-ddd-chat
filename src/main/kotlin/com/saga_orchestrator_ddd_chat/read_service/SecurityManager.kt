package com.saga_orchestrator_ddd_chat.read_service

import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton

@Singleton
open class SecurityManager(
    private val securityService: SecurityService,
) {
    open fun getCurrentUserEmail(): String {
        if (!securityService.authentication.isPresent)
            throw RuntimeException("No valid credential found")

        return securityService.authentication.get().attributes["email"].toString()
    }
}
