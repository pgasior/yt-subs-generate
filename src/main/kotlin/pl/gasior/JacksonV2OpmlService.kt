package pl.gasior

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

class JacksonV2OpmlService(private val categoryName: String) {
    fun generateOpml(subs: List<YoutubeSub>): String {
        val opml = Opml(
            "1.1", Body(
                Category(categoryName, categoryName,
                    subs.map { Outline(it.title, it.title, "rss", it.getChannelFeedUrl(), it.getChannelUrl()) })
            )
        )

        return XmlMapper().writerWithDefaultPrettyPrinter().writeValueAsString(opml)
    }

    @JacksonXmlRootElement(localName = "opml")
    data class Opml(@field:JacksonXmlProperty(isAttribute = true) val version: String,
                     val body: Body)

    data class Body(@field:JacksonXmlProperty(localName = "outline") val category: Category)

    data class Category(@field:JacksonXmlProperty(isAttribute = true) val text: String,
                        @field:JacksonXmlProperty(isAttribute = true) val title: String,
                        @field:JacksonXmlElementWrapper(useWrapping = false) val outline: List<Outline>)

    data class Outline(@field:JacksonXmlProperty(isAttribute = true) val text: String,
                       @field:JacksonXmlProperty(isAttribute = true) val title: String,
                       @field:JacksonXmlProperty(isAttribute = true) val type: String,
                       @field:JacksonXmlProperty(isAttribute = true) val xmlUrl: String,
                       @field:JacksonXmlProperty(isAttribute = true) val htmlUrl: String
    )
}