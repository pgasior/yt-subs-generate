package pl.gasior

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*

class TTRSSRequestFactory {
    private val objectMapper = jacksonObjectMapper()

    fun getLoginRequest(username: String, password: String): String {
        val request = mapOf(
            "op" to "login",
            "user" to username,
            "password" to password
        )

        return objectMapper.writeValueAsString(request)
    }

    fun getImportOpmlRequest(sid: String, opml: String): String {
        val request = mapOf(
            "op" to "importOPML",
            "sid" to sid,
            "opml" to Base64.getEncoder().encodeToString(opml.toByteArray())
        )

        return objectMapper.writeValueAsString(request)
    }
}