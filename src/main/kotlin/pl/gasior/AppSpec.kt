package pl.gasior

import com.uchuhimo.konf.ConfigSpec

object AppSpec: ConfigSpec("app") {
    val categoryName by required<String>()
    object TTRSSSpec: ConfigSpec("ttrss") {
        val username by required<String>()
        val password by required<String>()
        val url by required<String>()
    }
}