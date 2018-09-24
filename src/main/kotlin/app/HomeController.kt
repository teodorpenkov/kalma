package app

import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet(name = "Home", value = "/")
class HomeController : HttpServlet() {
    override fun doGet(req: HttpServletRequest, res: HttpServletResponse) {
        res.contentType = "text/html;charset=UTF-8"

        val text = javaClass.getResource("/index.html").readText()
        res.writer.write(text)
        res.writer.close()
    }
}