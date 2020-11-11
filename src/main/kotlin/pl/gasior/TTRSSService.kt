package pl.gasior

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM
import org.jsoup.Jsoup
import java.net.CookieManager

class TTRSSService(private val url: String,
                   private val username: String,
                   private val password: String) {

    private val cookieJar = JavaNetCookieJar(CookieManager())
    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .build()

    fun postOpml(opml: String) {
        if (login()) {
            println("Logged in to $url as $username")
            uploadOpml(opml)
        } else {
            println("Invalid credentials")
        }
    }

    private fun uploadOpml(opml: String) {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("op", "dlg")
            .addFormDataPart("method", "importOpml")
            .addFormDataPart(
                "opml_file",
                "subscriptions",
                opml.toRequestBody(APPLICATION_OCTET_STREAM.mimeType.toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(url.toHttpUrl().newBuilder().addPathSegments("backend.php").build())
            .post(body)
            .build()

        val responseBody = client.newCall(request).execute().body

        if (responseBody != null) {
            val parseBodyFragment = Jsoup.parse(responseBody.string())
            val elementsByClass = parseBodyFragment.body().getElementsByClass("panel panel-scrollable")
            println(elementsByClass.html().replace("<br>", ""))
        } else {
            println("Error uploading OPML")
        }
    }

    private fun login(): Boolean {
        val formBody = FormBody.Builder()
            .add("op", "login")
            .add("login", username)
            .add("password", password)
            .add("profile", "0")
            .build()
        val request = Request.Builder()
            .url(url.toHttpUrl().newBuilder()
                .addPathSegments("public.php")
                .addQueryParameter("return", url).build())
            .post(formBody)
            .build()

        client.newCall(request).execute()

        val cookies = cookieJar.loadForRequest(url.toHttpUrl())
        if (cookies.isEmpty()) {
            return false
        }

        return cookies.find { it.name == "ttrss_sid" } != null
    }
}