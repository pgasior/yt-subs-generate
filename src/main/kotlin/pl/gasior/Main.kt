package pl.gasior

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.yaml
import java.nio.file.Path

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = Config { addSpec(AppSpec) }
            .from.yaml.file(Path.of(System.getProperty("user.home"), ".ytsubs", "config.yml").toFile())
        val youtubeService = YoutubeService()
        val opmlService = JacksonV2OpmlService(config[AppSpec.categoryName])

        val subscriptions = youtubeService.fetchAllSubscriptions()
        println("Fetched ${subscriptions.size} subscriptions")
        val generatedOpml = opmlService.generateOpml(subscriptions)
//        println(generatedOpml)
        TTRSSService(
            config[AppSpec.TTRSSSpec.url],
            config[AppSpec.TTRSSSpec.username],
            config[AppSpec.TTRSSSpec.password])
            .postOpml(generatedOpml)
    }
}