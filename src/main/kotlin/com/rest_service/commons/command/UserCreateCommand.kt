package com.rest_service.commons.command

import com.rest_service.commons.enums.LanguageEnum
import com.rest_service.commons.enums.UserType
import io.micronaut.core.annotation.Introspected

@Introspected
data class UserCreateCommand(
    val type: UserType,
    val username: String?,
    val email: String,
    val primaryLanguage: LanguageEnum,
    var translationLanguages: Set<LanguageEnum>?,
) : Command
