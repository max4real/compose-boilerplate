package com.max4real.compose_boilerplate.extensions

import com.fasterxml.uuid.Generators
import java.util.UUID

object TimeUUID {
    private val timeBasedGenerator = Generators.timeBasedGenerator()

    fun generate(): UUID {
        return timeBasedGenerator.generate()
    }

    fun generateString(): String {
        return generate().toString()
    }

    fun extractCreatedAtMs(messageId: String): Long {
        val uuid = UUID.fromString(messageId)
        return (uuid.timestamp() - 122192928000000000L) / 10000L
    }
}