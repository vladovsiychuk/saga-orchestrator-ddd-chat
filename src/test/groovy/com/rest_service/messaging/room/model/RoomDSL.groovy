package com.rest_service.messaging.room.model


import com.rest_service.commons.dto.RoomDTO

class RoomDSL {
    Room domain = new Room()

    static RoomDSL aRoom() {
        return new RoomDSL()
    }

    static RoomDSL the(RoomDSL dsl) {
        return dsl
    }

    RoomDSL and() {
        return this
    }

    RoomDSL reactsTo(RoomDomainEventDSL event) {
        domain.apply(event.event)
        return this
    }

    RoomDTO data() {
        return domain.toDto()
    }
}

