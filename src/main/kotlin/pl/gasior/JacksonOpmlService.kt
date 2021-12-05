package pl.gasior

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.StringWriter
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter


class JacksonOpmlService(private val categoryName: String) {
    val objectMapper = jacksonObjectMapper()
    fun generateOpml(subs: List<YoutubeSub>): String {
        val xmlOutputFactory: XMLOutputFactory = XMLOutputFactory.newFactory()
        val out = StringWriter()
        val sw: XMLStreamWriter = xmlOutputFactory.createXMLStreamWriter(out)


        sw.document {
            element("opml") {
                attribute("version", "1.1")
                element("body") {
                    element("outline") {
                        attribute("text", categoryName)
                        attribute("title", categoryName)
                        subs.forEach { sub ->
                            element("outline") {
                                attribute("text", sub.title)
                                attribute("title", sub.title)
                                attribute("type", "rss")
                                attribute("xmlUrl", sub.getChannelFeedUrl())
                                attribute("htmlUrl", sub.getChannelUrl())
                            }
                        }
                    }
                }
            }
        }.close()
        return out.toString()
    }

    fun XMLStreamWriter.document(init: XMLStreamWriter.() -> Unit): XMLStreamWriter {
        this.writeStartDocument()
        this.init()
        this.writeEndDocument()
        return this
    }

    fun XMLStreamWriter.element(name: String, init: XMLStreamWriter.() -> Unit): XMLStreamWriter {
        this.writeStartElement(name)
        this.init()
        this.writeEndElement()
        return this
    }

    fun XMLStreamWriter.element(name: String, content: String) {
        element(name) {
            writeCharacters(content)
        }
    }

    fun XMLStreamWriter.attribute(name: String, value: String) = writeAttribute(name, value)
}