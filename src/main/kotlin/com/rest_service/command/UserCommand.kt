package com.rest_service.command

import com.rest_service.enums.LanguageEnum
import com.rest_service.enums.UserType
import io.micronaut.core.annotation.Introspected

@Introspected
data class UserCommand(
    val type: UserType,
    val username: String?,
    val primaryLanguage: LanguageEnum,
    val translationLanguages: List<LanguageEnum>?,
) {
    init {
        if (type == UserType.TRANSLATOR && (translationLanguages == null || translationLanguages.size < 2))
            throw IllegalArgumentException("A translator user must have at least 2 translation languages.")
        else if (type == UserType.REGULAR_USER && translationLanguages != null)
            throw IllegalArgumentException("A regular user cannot have translation languages.")
    }
}
