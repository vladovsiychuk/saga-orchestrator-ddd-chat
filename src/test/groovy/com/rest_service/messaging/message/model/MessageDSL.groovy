package com.rest_service.messaging.message.model

import com.rest_service.commons.dto.MessageDTO

class MessageDSL {
    Message domain = new Message()

    static MessageDSL aMessage() {
        return new MessageDSL()
    }

    static MessageDSL the(MessageDSL dsl) {
        return dsl
    }

    MessageDSL and() {
        return this
    }

    MessageDSL reactsTo(MessageDomainEventDSL event) {
        domain.apply(event.event)
        return this
    }


    MessageDTO data() {
        return domain.toDto()
    }
}

