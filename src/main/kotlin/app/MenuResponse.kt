package app

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.servlet.http.HttpServletResponse

class MenuResponse(private val zipOutputStream: ZipOutputStream,
                   private val queries: Map<String, String>,
                   private val weeklyMenu: String,
                   private val dailyMenus: List<String>) {
    private val dailyOutput = "/daily-%d.html"
    private val weeklyOutput = "/weekly.html"

    companion object {
        fun sendResponse(resp: HttpServletResponse,
                         queries: Map<String, String>,
                         dailyMenus: List<String>,
                         weeklyMenu: String) {
            val response = MenuResponse(ZipOutputStream(resp.outputStream), queries, weeklyMenu, dailyMenus)
            response.prepare()
            response.send()
        }
    }

    fun prepare() {
        addContent(weeklyOutput to weeklyMenu)
        dailyMenus.mapIndexed { index, menu ->
            dailyOutput.format(index) to menu
        }.forEach(this::addContent)
        addContent("commons.txt" to queries["commons"]!!)
        addContent("menu.txt" to queries["menu"]!!)
        addResource("style.css")
        addResource("printstyle.css")
        addResource("asti_logo.png")
    }

    fun send() {
        zipOutputStream.finish()
    }

    private fun addContent(entry: Pair<String, String>) {
        val zipEntry = ZipEntry(entry.first)
        zipOutputStream.putNextEntry(zipEntry)
        zipOutputStream.write(entry.second.toByteArray())
        zipOutputStream.closeEntry()
    }

    private fun addResource(resource: String) {
        val resourceContent = javaClass.getResource("/$resource").readBytes()
        val printStyle = ZipEntry(resource)
        zipOutputStream.putNextEntry(printStyle)
        zipOutputStream.write(resourceContent)
        zipOutputStream.closeEntry()
    }
}