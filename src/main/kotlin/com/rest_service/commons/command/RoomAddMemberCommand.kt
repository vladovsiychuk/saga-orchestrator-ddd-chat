package com.rest_service.commons.command

import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class RoomAddMemberCommand(
    val roomId: UUID,
    val memberId: UUID
) : Command
