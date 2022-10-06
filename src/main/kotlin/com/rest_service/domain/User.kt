package com.rest_service.domain

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.util.UUID


@MappedEntity
data class User(
    @field:Id
    val id: UUID,
    val email: String,
    val dateCreated: Long,
    val dateUpdated: Long,
)
