package com.rest_service.domain

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.util.UUID

@MappedEntity
data class Message(
    @field:Id
    val id: Int,
    val sender: UUID,
    val receiver: UUID,
    val message: String,
    val date: Long,
    val status: String,
)
