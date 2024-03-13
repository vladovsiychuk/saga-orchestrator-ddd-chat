package com.rest_service.commons.dto

import java.util.UUID

data class ErrorDTO(
    val userId: UUID,
    val errorMessage: String,
) : DTO
