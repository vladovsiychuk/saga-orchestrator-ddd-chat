package com.rest_service.util

import com.rest_service.domain.UserDomain
import com.rest_service.repository.UserRepository
import jakarta.inject.Singleton
import reactor.core.publisher.Mono

@Singleton
class UserUtil(
    private val securityUtil: SecurityUtil,
    private val userRepository: UserRepository,
) {
    fun getCurrentUser(): Mono<UserDomain> {
        val email = securityUtil.getUserEmail()
        return userRepository.findByEmail(email)
            .map { UserDomain(it) }
    }
}
