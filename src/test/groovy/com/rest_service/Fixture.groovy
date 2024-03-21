package com.rest_service

import com.rest_service.commons.enums.LanguageEnum
import com.rest_service.commons.enums.UserType

public class Fixture {
    public static Map anyValidUserCommand() {
        [id: UUID.randomUUID(), "type": UserType.REGULAR_USER, "email": "test@email.com", "primaryLanguage": LanguageEnum.ENGLISH]
    }

    public static Map anyValidRoomCommand() {
        ["userId": UUID.randomUUID()]
    }

    public static Map anyValidRoomDTO() {
        [
            "id"         : UUID.randomUUID(),
            "createdBy"  : UUID.randomUUID(),
            "members"    : [],
            "dateCreated": 123,
            "dateUpdated": 123
        ]
    }

    public static Map anyValidUserDTO() {
        [
            "id"             : UUID.randomUUID(),
            "email"          : "user@test.com",
            "primaryLanguage": LanguageEnum.ENGLISH,
            "type"           : UserType.REGULAR_USER,
            "dateCreated"    : 123,
            "dateUpdated"    : 123
        ]
    }

}
