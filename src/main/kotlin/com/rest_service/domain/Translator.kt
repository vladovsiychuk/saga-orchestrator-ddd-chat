package com.rest_service.domain

import com.rest_service.enum.LanguageEnum
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.util.UUID

@MappedEntity
data class Translator(
    @field:Id
    val userId: UUID,
    val languages: List<LanguageEnum>,
)
