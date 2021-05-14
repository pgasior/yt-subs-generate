package pl.gasior

class YoutubeSub(val title: String, private val channel: String) {
    fun getChannelFeedUrl(): String {
        return "https://www.youtube.com/feeds/videos.xml?channel_id=$channel"
    }

    fun getChannelUrl(): String {
        return "https://www.youtube.com/channel/$channel"
    }
}