package com.rest_service.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class MessageDTO (
    val senderName: String,
    val receiverName: String,
    val message: String,
    val date: Long,
    val status: String,
)
