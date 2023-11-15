package com.rest_service.resultReader

import com.rest_service.domain.MessageDomain
import com.rest_service.domain.UserDomain
import com.rest_service.dto.TranslationDTO
import com.rest_service.entity.MessageEvent
import com.rest_service.enums.LanguageEnum
import com.rest_service.enums.MessageEventType
import java.util.UUID

class MessageResultReader(
    messageId: UUID,
) {

    val message = RehydrateMessage(id = messageId)

    fun apply(event: MessageEvent) {
        when (event.type) {
            MessageEventType.MESSAGE_NEW              -> replayNew(event)
            MessageEventType.MESSAGE_READ             -> replayRead(event)
            MessageEventType.MESSAGE_MODIFY           -> replayModify(event)
            MessageEventType.MESSAGE_TRANSLATE,
            MessageEventType.MESSAGE_TRANSLATE_MODIFY -> replayTranslateModify(event)
        }
    }


    private fun replayNew(event: MessageEvent) {
        message.originalLanguage = event.language!!
        message.content = event.content!!
        message.roomId = event.roomId!!
        message.senderId = event.responsibleId
        message.dateCreated = event.dateCreated
    }

    private fun replayRead(event: MessageEvent) {
        message.read.add(event.responsibleId)
    }

    private fun replayModify(event: MessageEvent) {
        message.content = event.content!!
        message.dateCreated = event.dateCreated
        message.modified = true
    }

    private fun replayTranslateModify(event: MessageEvent) {
        message.translationMap[event.language!!] = TranslationDTO(event.responsibleId, event.content!!, event.language, event.type == MessageEventType.MESSAGE_TRANSLATE_MODIFY)
    }

    fun toDomain(userDomain: UserDomain): MessageDomain {
        val user = userDomain.toDto()

        val userLanguages = user.translationLanguages?.toMutableSet() ?: mutableSetOf()
        userLanguages.add(user.primaryLanguage.toString())

        return MessageDomain(
            message.id,
            message.roomId!!,
            message.senderId!!,
            message.content,
            message.read,
            message.originalLanguage!!,
            message.translationMap.filterKeys { it.toString() in userLanguages }.values.toList(),
            message.modified,
            message.dateCreated!!
        )
    }

    data class RehydrateMessage(
        val id: UUID,
        var roomId: UUID? = null,
        var senderId: UUID? = null,
        var content: String = "",
        var read: MutableList<UUID> = mutableListOf(),
        var originalLanguage: LanguageEnum? = null,
        var translationMap: MutableMap<LanguageEnum, TranslationDTO> = mutableMapOf(),
        var modified: Boolean = false,
        var dateCreated: Long? = null,
    )
}
