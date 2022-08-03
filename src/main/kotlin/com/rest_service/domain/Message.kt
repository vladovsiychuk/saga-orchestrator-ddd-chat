package com.rest_service.domain

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

@MappedEntity
data class Message(
    @field:Id
    val id: Int,
    val senderName: String,
    val receiverName: String,
    val message: String,
    val date: Long,
    val status: String,
)
