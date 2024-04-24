package com.rest_service.commons

import com.rest_service.commons.dto.DTO

interface Domain {
    fun toDto(): DTO
}
