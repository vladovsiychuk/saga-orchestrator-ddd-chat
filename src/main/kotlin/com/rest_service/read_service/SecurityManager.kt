package com.rest_service.read_service

import io.micronaut.security.utils.SecurityService
import jakarta.inject.Named
import jakarta.inject.Singleton

@Singleton
@Named("SecurityManager_readService")
open class SecurityManager(
    private val securityService: SecurityService,
) {
    open fun getCurrentUserEmail(): String {
        if (!securityService.authentication.isPresent)
            throw RuntimeException("No valid credential found")

        return securityService.authentication.get().attributes["email"].toString()
    }
}
