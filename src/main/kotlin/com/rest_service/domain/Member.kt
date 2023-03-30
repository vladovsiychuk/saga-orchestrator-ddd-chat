package com.rest_service.domain

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.time.Instant
import java.util.UUID

@MappedEntity
data class Member(
    @field:Id
    val id: UUID,
    val roomId: UUID,
    val userId: UUID,
    val joinedAt: Long = Instant.now()
        .toEpochMilli(),
) {
    constructor(roomId: UUID, userId: UUID) : this(
        UUID.randomUUID(),
        roomId,
        userId
    )
}
