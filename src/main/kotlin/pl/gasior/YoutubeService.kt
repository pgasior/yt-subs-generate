package pl.gasior

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.youtube.YouTube
import java.io.FileReader
import java.nio.file.Path
import java.util.*

class YoutubeService {

    private val clientSecrets = Path.of(System.getProperty("user.home"), ".ytsubs", "/client_secret.json")
    private val scopes: Collection<String> = listOf("https://www.googleapis.com/auth/youtube.readonly")

    private val applicationName = "Youtube subscriptions"
    private val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()

    fun fetchAllSubscriptions(): List<YoutubeSub> {
        val youtube = getService()
        var pageToken: String? = null
        val subscriptions = ArrayList<YoutubeSub>()
        do {
            val page = fetchSubscriptionsPage(youtube, pageToken)
            pageToken = page.nextPageToken
            subscriptions.addAll(page.subs)
            print("\rFetching... ${subscriptions.size} / ${page.total}")
        } while (pageToken != null)
        println()
        return subscriptions
    }

    private fun fetchSubscriptionsPage(youtubeService: YouTube, page: String?): SubscriptionPageResponse {
        val request = youtubeService.subscriptions().list(listOf("snippet", "contentDetails"))
        val subscriptionsResponse = request.apply {
            mine = true
            maxResults = 50L
            order = "alphabetical"
            pageToken = page
        }.execute()
        val subscriptions = subscriptionsResponse.items.map {
            YoutubeSub(it.snippet.title, it.snippet.resourceId.channelId)
        }

        return SubscriptionPageResponse(subscriptions, subscriptionsResponse.nextPageToken, subscriptionsResponse.pageInfo.totalResults)
    }

    data class SubscriptionPageResponse(val subs: List<YoutubeSub>, val nextPageToken: String?, val total: Int)

    private fun authorize(httpTransport: NetHttpTransport): Credential {
        // Load client secrets.
        val fileReader = FileReader(clientSecrets.toFile())
        val clientSecrets = GoogleClientSecrets.load(jsonFactory, fileReader)
        val path = Path.of(System.getProperty("user.home"), ".ytsubs")
        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
            httpTransport, jsonFactory, clientSecrets, scopes
        )
            .setDataStoreFactory(FileDataStoreFactory(path.toFile()))
            .build()
        return AuthorizationCodeInstalledApp(flow, LocalServerReceiver()).authorize("user")
    }

    private fun getService(): YouTube {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val credential = authorize(httpTransport)
        return YouTube.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName(applicationName)
            .build()
    }
}