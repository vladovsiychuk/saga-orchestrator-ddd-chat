package com.rest_service

import com.rest_service.domain.Member
import com.rest_service.domain.MessageEvent
import com.rest_service.domain.Room
import com.rest_service.domain.User
import com.rest_service.enums.LanguageEnum
import com.rest_service.enums.MessageEventType
import com.rest_service.enums.UserType
import com.rest_service.repository.MemberRepository
import com.rest_service.repository.MessageEventRepository
import com.rest_service.repository.RoomRepository
import com.rest_service.repository.UserRepository
import com.rest_service.websocket.WebSocketService
import com.rest_service.websocket.WebSocketServiceImpl
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.annotation.Client
import io.micronaut.reactor.http.client.ReactorHttpClient
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@MicronautTest(transactional = false)
@Requires(env = Environment.TEST)
class MessageControllerIntegrationTest extends Specification {

    @Inject
    @Client("/v1/messages")
    ReactorHttpClient client

    @Inject
    @Shared
    MessageEventRepository messageEventRepository
    @Inject
    @Shared
    UserRepository userRepository
    @Inject
    @Shared
    MemberRepository memberRepository
    @Inject
    @Shared
    RoomRepository roomRepository

    @Inject
    WebSocketService webSocketService

    @Shared
    String user_token
    @Shared
    def room_id
    @Shared
    def message_1_id
    @Shared
    def regular_user_id
    @Shared
    def companion_regular_user_id
    @Shared
    def translator_user_id

    def conditions = new PollingConditions(timeout: 5, initialDelay: 0.5, factor: 1.25)

    def setupSpec() {
        user_token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJ1c2VyLTFAZ21haWwuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.B7NnRHclfkrcOgK4HX8fqogY-oq3Hv1GrWTylDqOhrg"

        message_1_id = UUID.fromString("2076c104-9d61-400d-bc71-971b93fa4186")
        room_id = UUID.fromString("71ddb6fd-8640-4d23-a5b3-ae16e6945fd4")
        regular_user_id = UUID.fromString("19310583-8811-4286-afc2-8024f05ce779")
        companion_regular_user_id = UUID.fromString("9444016a-c6d3-4356-85e9-1b285c1a000f")
        translator_user_id = UUID.fromString("89a6cea2-52de-4295-942c-a0b7f485f561")

        def regular_user = new User(regular_user_id, null, "user-1@gmail.com", null, LanguageEnum.UKRAINIAN, null, UserType.REGULAR_USER, 1, 1)

        def regular_user_member = new Member(UUID.randomUUID(), room_id, regular_user_id, 1)
        def companion_user_member = new Member(UUID.randomUUID(), room_id, companion_regular_user_id, 1)
        def room = new Room(room_id, null, regular_user_id, 1, 1)

        def message_1_message_new_event = new MessageEvent(
                UUID.randomUUID(),
                message_1_id,
                LanguageEnum.ENGLISH,
                "new message",
                room_id,
                companion_regular_user_id,
                MessageEventType.MESSAGE_NEW,
                1,
        )

        def message_1_message_read_event = new MessageEvent(
                UUID.randomUUID(),
                message_1_id,
                null,
                null,
                null,
                regular_user_id,
                MessageEventType.MESSAGE_READ,
                2,
        )

        def message_1_message_modify_event = new MessageEvent(
                UUID.randomUUID(),
                message_1_id,
                null,
                "modified text",
                null,
                companion_regular_user_id,
                MessageEventType.MESSAGE_MODIFY,
                3,
        )

        def message_1_message_translate_event = new MessageEvent(
                UUID.randomUUID(),
                message_1_id,
                LanguageEnum.UKRAINIAN,
                "translation text",
                null,
                translator_user_id,
                MessageEventType.MESSAGE_TRANSLATE,
                4
        )

        def message_1_message_translate_modify_event = new MessageEvent(
                UUID.randomUUID(),
                message_1_id,
                LanguageEnum.UKRAINIAN,
                "modified translation text",
                null,
                translator_user_id,
                MessageEventType.MESSAGE_TRANSLATE_MODIFY,
                5
        )

        memberRepository.saveAll([regular_user_member, companion_user_member]).collectList().block()
        userRepository.save(regular_user).block()
        roomRepository.save(room).block()

        messageEventRepository.saveAll([
                message_1_message_new_event,
                message_1_message_read_event,
                message_1_message_modify_event,
                message_1_message_translate_event,
                message_1_message_translate_modify_event,
        ]).collectList().block()
    }

    void "GET should return messages of rooms user belongs to"() {
        when:
        def request = HttpRequest.GET("/rooms?messagesPerRoom=30").bearerAuth(user_token)
        def response = client.toBlocking().exchange(request, List)

        then:
        response.body() == [
                [
                        id              : message_1_id.toString(),
                        roomId          : room_id.toString(),
                        senderId        : companion_regular_user_id.toString(),
                        content         : "modified text",
                        read            : [],
                        originalLanguage: LanguageEnum.ENGLISH.toString(),
                        translation     : "modified translation text",
                        dateCreated     : 3
                ]
        ]
    }

    void "GET should return messages of the room"() {
        when:
        def request = HttpRequest.GET("/rooms/$room_id").bearerAuth(user_token)
        def response = client.toBlocking().exchange(request, List)

        then:
        response.body() == [
                [
                        id              : message_1_id.toString(),
                        roomId          : room_id.toString(),
                        senderId        : companion_regular_user_id.toString(),
                        content         : "modified text",
                        read            : [],
                        originalLanguage: LanguageEnum.ENGLISH.toString(),
                        translation     : "modified translation text",
                        dateCreated     : 3
                ]
        ]
    }

    void "POST should create the message, return it, and publish the message to websocket"() {
        given: "command with new message"
        def command = [
                roomId : room_id.toString(),
                content: "new message content"
        ]

        when: "message is created"
        def request = HttpRequest.POST("/", command).bearerAuth(user_token)
        def response = client.toBlocking().exchange(request, Map)

        then: "expected dto is returned"
        response.body()["id"] as String != null
        response.body()["roomId"] as String == room_id.toString()
        response.body()["senderId"] as String == regular_user_id.toString()
        response.body()["content"] as String == "new message content"
        response.body()["read"] as List<String> == []
        response.body()["originalLanguage"] as String == LanguageEnum.UKRAINIAN.toString()
        response.body()["translation"] as String == ""
        response.body()["dateCreated"] as Long != null
    }

    void "only cleanup"() {
        cleanup:
        messageEventRepository.deleteAll().block()
        userRepository.deleteAll().block()
        memberRepository.deleteAll().block()
        roomRepository.deleteAll().block()
    }

    @MockBean(WebSocketServiceImpl)
    WebSocketService webSocketService() {
        Mock(WebSocketService)
    }
}
