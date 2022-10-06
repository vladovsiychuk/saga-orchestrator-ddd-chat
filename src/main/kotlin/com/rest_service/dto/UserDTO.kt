package com.rest_service.dto

import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class UserDTO(
    val id: UUID,
    val email: String,
    val dateCreated: Long,
    val dateUpdated: Long,
)
