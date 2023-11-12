package com.rest_service.entity

import io.micronaut.data.annotation.AutoPopulated
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.time.Instant
import java.util.UUID

@MappedEntity
data class Member(
    @field:Id
    @AutoPopulated
    val id: UUID? = null,
    val roomId: UUID,
    val userId: UUID,
    val joinedAt: Long = Instant.now()
        .toEpochMilli(),
)
