package com.rest_service.command

import com.rest_service.enums.LanguageEnum
import com.rest_service.enums.UserType
import com.rest_service.exception.IncorrectInputException
import io.micronaut.core.annotation.Introspected
import reactor.core.publisher.Mono

@Introspected
data class UserCommand(
    val type: UserType,
    val username: String?,
    val primaryLanguage: LanguageEnum,
    var translationLanguages: MutableSet<LanguageEnum>?,
) {
    fun validate(): Mono<Boolean> {
        return if (type == UserType.TRANSLATOR && translationLanguages!!.size < 2)
            Mono.error(IncorrectInputException("A translator user must have at least 1 translation language."))
        else if (type == UserType.REGULAR_USER && !translationLanguages.isNullOrEmpty())
            Mono.error(IncorrectInputException("A regular user cannot have translation languages."))
        else
            Mono.just(true)
    }
}
