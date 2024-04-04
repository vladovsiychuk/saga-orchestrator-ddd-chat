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
        ["memberId": UUID.randomUUID()]
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

    static Map anyValidErrorDto() {
        ["message": 'test text']
    }
}
