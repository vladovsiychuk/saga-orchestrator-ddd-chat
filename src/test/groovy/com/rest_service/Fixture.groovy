package com.rest_service

import com.rest_service.commons.enums.LanguageEnum
import com.rest_service.commons.enums.UserType

class Fixture {
    static Map anyValidUserCreateCommand() {
        ["type": UserType.REGULAR_USER, "email": "example@test.com", "primaryLanguage": LanguageEnum.ENGLISH, "temporaryId": UUID.randomUUID()]
    }

    static Map anyValidRoomCreateCommand() {
        ["companionId": UUID.randomUUID()]
    }

    static Map anyValidRoomAddMemberCommand() {
        ["roomId": UUID.randomUUID(), "memberId": UUID.randomUUID()]
    }

    static Map anyValidMessageCreateCommand() {
        ["roomId": UUID.randomUUID(), "content": "test content", "language": "ENGLISH"]
    }

    static Map anyValidMessageUpdateCommand() {
        ["messageId": UUID.randomUUID(), "content": "test content"]
    }

    static Map anyValidMessageReadCommand() {
        ["messageId": UUID.randomUUID()]
    }

    static Map anyValidMessageTranslateCommand() {
        ["messageId": UUID.randomUUID(), "translation": "translation text", "language": "ENGLISH"]
    }

    static Map anyValidRoomDTO() {
        [
            "id"         : UUID.randomUUID(),
            "createdBy"  : UUID.randomUUID(),
            "members"    : [],
            "dateCreated": 123,
            "dateUpdated": 123
        ]
    }

    static Map anyValidUserDTO() {
        [
            "id"             : UUID.randomUUID(),
            "email"          : "user@test.com",
            "primaryLanguage": LanguageEnum.ENGLISH,
            "type"           : UserType.REGULAR_USER,
            "dateCreated"    : 123,
            "dateUpdated"    : 123
        ]
    }

    static Map anyValidMessageDTO() {
        [
            "id"              : UUID.randomUUID(),
            "roomId"          : UUID.randomUUID(),
            "senderId"        : UUID.randomUUID(),
            "content"         : "test content",
            "read"            : [],
            "originalLanguage": "ENGLISH",
            "translations"    : [],
            "modified"        : false,
            "dateCreated"     : 123,
        ]
    }

    static Map anyValidErrorDto() {
        ["message": 'test text']
    }
}
