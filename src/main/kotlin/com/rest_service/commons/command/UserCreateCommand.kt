package com.rest_service.commons.command

import com.rest_service.commons.enums.LanguageEnum
import com.rest_service.commons.enums.UserType
import com.rest_service.commons.exception.IncorrectInputException
import io.micronaut.core.annotation.Introspected
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Introspected
data class UserCreateCommand(
    val type: UserType,
    val username: String?,
    val email: String,
    val primaryLanguage: LanguageEnum,
    var translationLanguages: Set<LanguageEnum>?,
) : Command {
    fun validate(): Mono<Boolean> {
        return if (type == UserType.TRANSLATOR && translationLanguages!!.size < 2)
            IncorrectInputException("A translator user must have at least 1 translation language.").toMono()
        else
            Mono.just(true)
    }
}
