package com.rest_service.messaging.user.model

import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class TimeUtils {
    companion object {
        private var clock: Clock = Clock.systemUTC()

        /**
         * This should only be used for testing purpose
         */
        fun setFixedClock(epochMilli: Long) {
            clock = Clock.fixed(Instant.ofEpochMilli(epochMilli), ZoneId.of("UTC"))
        }

        fun now(zoneId: String = "UTC"): Long {
            return LocalDateTime.now(clock)
                .atZone(ZoneId.of(zoneId))
                .toInstant()
                .toEpochMilli()
        }
    }
}
