package com.rest_service.resultReader

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.domain.MessageEvent
import com.rest_service.domain.User
import com.rest_service.dto.MessageDTO
import com.rest_service.enums.LanguageEnum
import com.rest_service.enums.MessageEventType
import java.util.UUID

class MessageResultReader(
    messageId: UUID,
    private val user: User,
) {

    private val mapper = jacksonObjectMapper()

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
        if (event.responsibleId != user.id)
            message.read.add(event.responsibleId)
    }

    private fun replayModify(event: MessageEvent) {
        message.content = event.content!!
        message.dateCreated = event.dateCreated
    }

    private fun replayTranslateModify(event: MessageEvent) {
        if (event.language == user.primaryLanguage)
            message.translation = event.content!!
    }

    fun toDto(): MessageDTO {
        return mapper.convertValue(message, MessageDTO::class.java)
    }

    data class RehydrateMessage(
        val id: UUID,
        var roomId: UUID? = null,
        var senderId: UUID? = null,
        var content: String = "",
        var read: MutableList<UUID> = mutableListOf(),
        var originalLanguage: LanguageEnum? = null,
        var translation: String = "",
        var dateCreated: Long? = null,
    )
}
