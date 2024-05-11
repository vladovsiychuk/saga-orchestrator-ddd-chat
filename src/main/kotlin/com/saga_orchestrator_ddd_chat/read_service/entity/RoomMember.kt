package com.saga_orchestrator_ddd_chat.read_service.entity

import io.micronaut.data.annotation.AutoPopulated
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.util.UUID

@MappedEntity
data class RoomMember(
    @field:Id
    @AutoPopulated
    val id: UUID? = null,
    val roomId: UUID,
    val memberId: UUID,
)
