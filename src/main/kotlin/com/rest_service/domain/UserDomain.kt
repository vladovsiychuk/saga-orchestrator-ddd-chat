package com.rest_service.domain

import com.rest_service.dto.UserDTO
import com.rest_service.entity.User
import com.rest_service.enums.LanguageEnum
import com.rest_service.enums.UserType
import java.util.UUID
import reactor.core.publisher.Mono

class UserDomain(
    private val id: UUID,
    private val username: String?,
    private val email: String,
    private val avatar: String?,
    private val primaryLanguage: LanguageEnum,
    private val translationLanguages: List<String>?,
    private val type: UserType,
    private val dateCreated: Long,
    private val dateUpdated: Long,
) {
    constructor(user: User) : this(
        user.id!!,
        user.username,
        user.email,
        user.avatar,
        user.primaryLanguage,
        user.translationLanguages,
        user.type,
        user.dateCreated,
        user.dateUpdated,
    )

    fun toDto(): Mono<UserDTO> {
        return Mono.just(
            UserDTO(
                id,
                username,
                email,
                avatar,
                primaryLanguage,
                translationLanguages,
                type,
                dateCreated,
                dateUpdated,
            )
        )
    }
}
