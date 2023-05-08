package com.rest_service.resultReader

import com.rest_service.domain.MessageEvent
import com.rest_service.domain.User
import com.rest_service.dto.MessageDTO
import com.rest_service.enums.LanguageEnum
import com.rest_service.enums.MessageEventType
import java.util.UUID

class MessageResultReader(
    messageId: UUID,
) {

    private val message = RehydrateMessage(id = messageId)

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
    }

    private fun replayTranslateModify(event: MessageEvent) {
        message.translationMap[event.language!!] = event.content!!
        message.translatorId = event.responsibleId
    }

    fun toDto(user: User): MessageDTO {
        return MessageDTO(
            message.id,
            message.roomId!!,
            message.senderId!!,
            message.translatorId,
            message.content,
            message.read.filterNot { it == user.id },
            message.originalLanguage!!,
            message.translationMap[user.primaryLanguage] ?: "",
            message.dateCreated!!
        )
    }

    data class RehydrateMessage(
        val id: UUID,
        var roomId: UUID? = null,
        var senderId: UUID? = null,
        var translatorId: UUID? = null,
        var content: String = "",
        var read: MutableList<UUID> = mutableListOf(),
        var originalLanguage: LanguageEnum? = null,
        var translationMap: MutableMap<LanguageEnum, String> = mutableMapOf(),
        var dateCreated: Long? = null,
    )
}
