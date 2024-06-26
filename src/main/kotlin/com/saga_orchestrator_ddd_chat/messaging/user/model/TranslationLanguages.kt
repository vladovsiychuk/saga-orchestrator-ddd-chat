package com.saga_orchestrator_ddd_chat.messaging.user.model

import com.saga_orchestrator_ddd_chat.commons.command.UserCreateCommand
import com.saga_orchestrator_ddd_chat.commons.enums.LanguageEnum

class TranslationLanguages private constructor(
    val languages: Set<LanguageEnum>
) {
    companion object {
        fun from(command: UserCreateCommand): TranslationLanguages {
            val languages = (command.translationLanguages ?: emptySet()) + command.primaryLanguage
            if (languages.size < 2)
                throw RuntimeException("Translator must have at least two different translation languages.")

            return TranslationLanguages(languages)
        }
    }

    fun contains(language: LanguageEnum): Boolean {
        return languages.contains(language)
    }
}
