package com.saga_orchestrator_ddd_chat.commons.command

import com.saga_orchestrator_ddd_chat.commons.enums.LanguageEnum
import com.saga_orchestrator_ddd_chat.commons.enums.UserType
import io.micronaut.core.annotation.Introspected

@Introspected
data class UserCreateCommand(
    val type: UserType,
    val username: String?,
    val email: String,
    val primaryLanguage: LanguageEnum,
    var translationLanguages: Set<LanguageEnum>?,
) : Command
