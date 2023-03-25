package com.rest_service.domain

import io.micronaut.data.annotation.MappedEntity
import java.util.UUID

@MappedEntity
data class Member(
    val roomId: UUID,
    val userId: UUID,
    val joinedAt: Long,
)
