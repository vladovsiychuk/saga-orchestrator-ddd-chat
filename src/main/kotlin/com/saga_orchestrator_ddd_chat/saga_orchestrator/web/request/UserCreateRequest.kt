package com.saga_orchestrator_ddd_chat.saga_orchestrator.web.request

import com.saga_orchestrator_ddd_chat.commons.enums.LanguageEnum
import com.saga_orchestrator_ddd_chat.commons.enums.UserType
import io.micronaut.core.annotation.Introspected

@Introspected
data class UserCreateRequest(
    val type: UserType,
    val username: String?,
    val primaryLanguage: LanguageEnum,
    var translationLanguages: MutableSet<LanguageEnum>?,
)
