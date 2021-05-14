package pl.gasior

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.http.entity.ContentType.APPLICATION_JSON
import org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM
import org.jsoup.Jsoup
import java.net.CookieManager
import java.time.Duration

class TTRSSService(private val url: String,
                   private val username: String,
                   private val password: String) {

    private val client = OkHttpClient.Builder()
        .readTimeout(Duration.ofSeconds(30))
        .callTimeout(Duration.ofSeconds(30))
        .build()

    private val requestFactory = TTRSSRequestFactory()

    private val objectMapper = jacksonObjectMapper()

    private var apiKey: String = ""

    fun postOpml(opml: String) {
        if (login()) {
            println("Logged in to $url as $username")
            uploadOpml(opml)
        } else {
            println("Invalid credentials")
        }
    }

    private fun uploadOpml(opml: String) {
        val request = Request.Builder()
            .url(url.toHttpUrl().newBuilder().addPathSegments("api/")
                .build())
            .post(requestFactory.getImportOpmlRequest(apiKey, opml).toRequestBody(APPLICATION_JSON.mimeType.toMediaType()))
            .build()

        val responseBody = client.newCall(request).execute().body

        if (responseBody != null) {
            val apiResponse = objectMapper.readValue(responseBody.string(), ResponseWrapper::class.java)
            for (node in apiResponse.content["message"]!!) {
                println(node.asText())
            }
        } else {
            println("Error uploading OPML")
        }
    }

    private fun login(): Boolean {
        val request = Request.Builder()
            .url(url.toHttpUrl().newBuilder()
                .addPathSegments("api/")
                .build())
            .post(requestFactory.getLoginRequest(username, password).toRequestBody(APPLICATION_JSON.mimeType.toMediaType()))
            .build()

        val response = client.newCall(request).execute()

        if (response.body == null) {
            return false
        }

        val apiResponse = objectMapper.readValue(response.body!!.string(), ResponseWrapper::class.java)

        if (apiResponse.status != 0) {
            return false
        }

        apiKey = apiResponse.content["session_id"]!!.asText()

        return true
    }

    data class ResponseWrapper(
        val seq: Int,
        val status: Int,
        val content: Map<String, JsonNode>
    )
}