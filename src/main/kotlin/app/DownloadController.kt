package app

import khronos.toString
import menu.CommonsParser
import menu.MenuParser
import menu.Renderer
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet(name = "Download", value = "/download")
class DownloadController : HttpServlet() {

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        req.characterEncoding = "UTF-8"
        val queries = mapOf(
                "menu" to req.getParameter("menu"),
                "commons" to req.getParameter("commons")
        )

        val commons = CommonsParser().parseFile(queries["commons"]!!)
        val menu = MenuParser(commons).parseFile(queries["menu"]!!)

        val renderer = Renderer()
        val dailyMenus = menu.map { renderer.renderDaily(it) }
        val weeklyMenu = renderer.renderWeekly(commons, menu)

        val filename = """
            menu-${ menu.first().date.toString("dd.MM") }-${ menu.last().date.toString("dd.MM") }.zip
        """.trimIndent()
        resp.contentType = "application/zip"
        resp.setHeader("Content-Disposition", "attachment; filename=$filename")
        MenuResponse.sendResponse(resp, queries, dailyMenus, weeklyMenu)
    }
}
