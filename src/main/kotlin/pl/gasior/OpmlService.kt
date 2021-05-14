package pl.gasior

import com.rometools.opml.feed.opml.Opml
import com.rometools.opml.feed.opml.Outline
import com.rometools.opml.feed.synd.impl.ConverterForOPML10
import com.rometools.rome.feed.synd.SyndFeedImpl
import com.rometools.rome.io.SyndFeedOutput
import com.rometools.rome.io.WireFeedOutput
import java.net.URL
import java.util.*

class OpmlService(private val categoryName: String) {
    fun generateOpml(subs: List<YoutubeSub>): String {
        val opml = Opml()
        val baseOutline = Outline().apply {
            text = categoryName
            title = categoryName
        }
        val childOutlines = ArrayList<Outline>()
        for (sub in subs) {
            val outline = Outline(sub.title, URL(sub.getChannelFeedUrl()), URL(sub.getChannelUrl()))
            outline.text = sub.title
            childOutlines.add(outline)
        }
        baseOutline.children = childOutlines
        opml.outlines = listOf(baseOutline)

        val syndFeed = SyndFeedImpl()
        ConverterForOPML10().copyInto(opml, syndFeed)

        return SyndFeedOutput().outputString(syndFeed, true)
    }

}
