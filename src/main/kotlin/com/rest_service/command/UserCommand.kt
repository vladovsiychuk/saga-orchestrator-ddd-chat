package com.rest_service.command

import com.rest_service.enums.LanguageEnum
import com.rest_service.enums.UserType
import com.rest_service.exception.IncorrectInputException
import io.micronaut.core.annotation.Introspected
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Introspected
data class UserCommand(
    val type: UserType,
    val username: String?,
    val primaryLanguage: LanguageEnum,
    var translationLanguages: MutableSet<LanguageEnum>?,
) {
    fun validate(): Mono<Boolean> {
        return if (type == UserType.TRANSLATOR && translationLanguages!!.size < 2)
            IncorrectInputException("A translator user must have at least 1 translation language.").toMono()
        else if (type == UserType.REGULAR_USER && !translationLanguages.isNullOrEmpty())
            IncorrectInputException("A regular user cannot have translation languages.").toMono()
        else
            Mono.just(true)
    }
}
