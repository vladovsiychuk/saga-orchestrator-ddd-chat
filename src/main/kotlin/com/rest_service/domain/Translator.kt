package com.rest_service.domain

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.MappedProperty
import io.micronaut.data.model.DataType
import java.util.UUID

@MappedEntity
data class Translator(
    @field:Id
    val userId: UUID,
    @MappedProperty(type = DataType.JSON)
    val languages: List<String>,
)
