package com.rest_service

import com.rest_service.constant.MessageConstant
import com.rest_service.constant.RoomConstant
import com.rest_service.constant.UserConstant
import com.rest_service.enums.LanguageEnum
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.annotation.Client
import io.micronaut.reactor.http.client.ReactorHttpClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@MicronautTest(transactional = false)
@Requires(env = Environment.TEST)
class MessageControllerIntegrationTest extends Specification {

    @Inject
    @Client("/v1/messages")
    ReactorHttpClient client

    def conditions = new PollingConditions(timeout: 5, initialDelay: 0.5, factor: 1.25)

    void "GET should return to the REGULAR_USER messages of rooms user belongs to"() {
        when:
        def request = HttpRequest.GET("/?roomLimit=30").bearerAuth(UserConstant.USER_1_TOKEN)
        def response = client.toBlocking().exchange(request, List)

        then:
        response.body().sort() == [
            [
                id              : MessageConstant.MESSAGE_1_ID,
                roomId          : RoomConstant.ROOM_1_ID,
                senderId        : UserConstant.USER_3_ID,
                content         : "modified text",
                read            : [UserConstant.USER_1_ID],
                originalLanguage: LanguageEnum.ENGLISH.toString(),
                translations    : [
                    [
                        translatorId: UserConstant.USER_4_ID,
                        translation : "modified translation text",
                        language    : LanguageEnum.UKRAINIAN.toString(),
                    ]
                ],
                dateCreated     : 3
            ],
            [
                id              : MessageConstant.MESSAGE_2_ID,
                roomId          : RoomConstant.ROOM_4_ID,
                senderId        : UserConstant.USER_1_ID,
                content         : "new message 2",
                read            : [],
                originalLanguage: LanguageEnum.ENGLISH.toString(),
                translations    : [],
                dateCreated     : 1
            ]
        ].sort()
    }

    void "GET should return to the TRANSLATOR all the translations that match his languages"() {
        when:
        def request = HttpRequest.GET("/?roomLimit=30").bearerAuth(UserConstant.USER_4_TOKEN)
        def response = client.toBlocking().exchange(request, List)

        then:
        response.body().find { it["id"] == MessageConstant.MESSAGE_1_ID }["translations"] == [
            [
                translatorId: UserConstant.USER_4_ID,
                translation : "modified translation text",
                language    : LanguageEnum.UKRAINIAN.toString(),
            ],
            [
                translatorId: UserConstant.USER_7_ID,
                translation : "second translation text",
                language    : LanguageEnum.ITALIAN.toString(),
            ]
        ]
    }

    void "GET should return messages of the room"() {
        when:
        def request = HttpRequest.GET("/rooms/${RoomConstant.ROOM_1_ID}").bearerAuth(UserConstant.USER_1_TOKEN)
        def response = client.toBlocking().exchange(request, List)

        then:
        response.body().collect(message -> message["id"]) == [MessageConstant.MESSAGE_1_ID]
    }

    void "POST should create the message"() {
        given: "command with new message"
        def command = [
            roomId : RoomConstant.ROOM_1_ID,
            content: "new message content"
        ]

        when: "message is created"
        def request = HttpRequest.POST("/", command).bearerAuth(UserConstant.USER_1_TOKEN)
        def response = client.toBlocking().exchange(request, Map)

        then: "new message is created"
        response.body()["content"] as String == "new message content"
    }

    void "PUT read endpoint should add the user to read list"() {
        when:
        def request = HttpRequest.PUT("/${MessageConstant.MESSAGE_2_ID}/read", null).bearerAuth(UserConstant.USER_6_TOKEN)
        def response = client.toBlocking().exchange(request, Map)

        then:
        response.body()["read"] == [UserConstant.USER_6_ID]

        and:
        conditions.eventually {
            def request2 = HttpRequest.GET("/?roomLimit=30").bearerAuth(UserConstant.USER_1_TOKEN)
            def response2 = client.toBlocking().exchange(request2, List)

            assert response2.body().find { it["id"] == MessageConstant.MESSAGE_2_ID }["read"] == [UserConstant.USER_6_ID]
        }
    }

    void "PUT should modify the content of the message"() {
        given:
        def command = [
            content: "new content text"
        ]

        when: 'message content is modified'
        def request = HttpRequest.PUT("/$MessageConstant.MESSAGE_1_ID", command).bearerAuth(UserConstant.USER_3_TOKEN)
        def response = client.toBlocking().exchange(request, Map)

        then:
        response.body()["content"] == "new content text"
    }

    void "PUT should add the translation to the message"() {
        given:
        def command = [
            translation: "new translation text",
            language   : LanguageEnum.UKRAINIAN.toString()
        ]

        when: 'message is translated'
        def request = HttpRequest.PUT("/$MessageConstant.MESSAGE_2_ID/translate", command).bearerAuth(UserConstant.USER_4_TOKEN)
        def response = client.toBlocking().exchange(request, Map)

        then:
        response.body()["translations"] == [
            [
                translatorId: UserConstant.USER_4_ID,
                translation : "new translation text",
                language    : LanguageEnum.UKRAINIAN.toString(),
            ]
        ]
    }
}
