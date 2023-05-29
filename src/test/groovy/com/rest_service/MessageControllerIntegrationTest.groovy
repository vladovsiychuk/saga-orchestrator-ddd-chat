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

    void "GET should return messages of rooms user belongs to"() {
        when:
        def request = HttpRequest.GET("/?roomLimit=30").bearerAuth(UserConstant.USER_1_TOKEN)
        def response = client.toBlocking().exchange(request, List)

        then:
        response.body().sort() == [
            [
                id              : MessageConstant.MESSAGE_1_ID,
                roomId          : RoomConstant.ROOM_1_ID,
                senderId        : UserConstant.USER_3_ID,
                translatorId    : UserConstant.USER_4_ID,
                content         : "modified text",
                read            : [],
                originalLanguage: LanguageEnum.ENGLISH.toString(),
                translation     : "modified translation text",
                dateCreated     : 3
            ],
            [
                id              : MessageConstant.MESSAGE_2_ID,
                roomId          : RoomConstant.ROOM_4_ID,
                senderId        : UserConstant.USER_1_ID,
                translatorId    : null,
                content         : "new message 2",
                read            : [],
                originalLanguage: LanguageEnum.ENGLISH.toString(),
                translation     : '',
                dateCreated     : 1
            ]
        ].sort()
    }

    void "GET should return messages of the room"() {
        when:
        def request = HttpRequest.GET("/rooms/${RoomConstant.ROOM_1_ID}").bearerAuth(UserConstant.USER_1_TOKEN)
        def response = client.toBlocking().exchange(request, List)

        then:
        response.body().collect(message -> message.id) == [MessageConstant.MESSAGE_1_ID]
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

        then: "expected dto is returned"
        response.body()["id"] as String != null
        response.body()["roomId"] as String == RoomConstant.ROOM_1_ID
        response.body()["senderId"] as String == UserConstant.USER_1_ID
        response.body()["content"] as String == "new message content"
        response.body()["read"] as List<String> == []
        response.body()["originalLanguage"] as String == LanguageEnum.UKRAINIAN.toString()
        response.body()["translation"] as String == ""
        response.body()["dateCreated"] as Long != null
    }

    void "PUT read endpoint should add the user to read list"() {
        when:
        def request = HttpRequest.PUT("/${MessageConstant.MESSAGE_1_ID}/read", null).bearerAuth(UserConstant.USER_3_TOKEN)
        client.toBlocking().exchange(request, Map)

        then:
        conditions.eventually {
            def request2 = HttpRequest.GET("/?roomLimit=30").bearerAuth(UserConstant.USER_1_TOKEN)
            def response = client.toBlocking().exchange(request2, List)

            response.body().find {it.id == MessageConstant.MESSAGE_1_ID}["read"] == [UserConstant.USER_3_ID]
        }
    }
}
