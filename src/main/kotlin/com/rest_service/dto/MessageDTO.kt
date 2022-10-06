package com.rest_service.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.micronaut.core.annotation.Introspected

@JsonIgnoreProperties(ignoreUnknown = true)
@Introspected
data class MessageDTO(
    val sender: String,
    val receiver: String,
    val message: String,
    val date: Long,
    val status: String,
)
