package com.saga_orchestrator_ddd_chat.commons

import com.saga_orchestrator_ddd_chat.commons.dto.DTO

interface Domain {
    fun toDto(): DTO
}
