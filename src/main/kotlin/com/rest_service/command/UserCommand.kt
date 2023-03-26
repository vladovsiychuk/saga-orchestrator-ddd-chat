package com.rest_service.command

import com.rest_service.enums.LanguageEnum
import com.rest_service.enums.UserType
import io.micronaut.core.annotation.Introspected

@Introspected
data class UserCommand(
    val type: UserType,
    val username: String?,
    val primaryLanguage: LanguageEnum,
    var translationLanguages: MutableSet<LanguageEnum>?,
)
