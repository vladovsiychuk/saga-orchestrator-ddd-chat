package com.rest_service.saga_orchestrator.web.request

import com.rest_service.commons.enums.LanguageEnum
import com.rest_service.commons.enums.UserType
import io.micronaut.core.annotation.Introspected

@Introspected
data class UserCreateRequest(
    val type: UserType,
    val username: String?,
    val primaryLanguage: LanguageEnum,
    var translationLanguages: MutableSet<LanguageEnum>?,
)
