package com.rest_service.commons.command

import com.rest_service.commons.enums.LanguageEnum
import com.rest_service.commons.enums.UserType
import com.rest_service.commons.exception.IncorrectInputException
import io.micronaut.core.annotation.Introspected
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Introspected
data class UserCommand(
    var id: UUID?,
    val type: UserType,
    val username: String?,
    val email: String,
    val primaryLanguage: LanguageEnum,
    var translationLanguages: MutableSet<LanguageEnum>?,
    val temporaryId: UUID?,
) : Command {
    fun validate(): Mono<Boolean> {
        return if (type == UserType.TRANSLATOR && translationLanguages!!.size < 2)
            IncorrectInputException("A translator user must have at least 1 translation language.").toMono()
        else if (type == UserType.REGULAR_USER && !translationLanguages.isNullOrEmpty())
            IncorrectInputException("A regular user cannot have translation languages.").toMono()
        else
            Mono.just(true)
    }
}
