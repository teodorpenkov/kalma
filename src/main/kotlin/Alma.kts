import menu.*
import java.io.File

val commonsFile = File(javaClass.getResource("/commons.alma").path).readText()
val commons = CommonsParser().parseFile(commonsFile)
println(
"""
Меню всеки ден:

$commons
"""
)
val almaFile = File(javaClass.getResource("/input.alma").path).readText()
val menu = MenuParser(commons).parseFile(almaFile)

val renderer = Renderer()
val dailyMenus = menu.map { renderer.renderDaily(it) }
val weeklyMenu = renderer.renderWeekly(commons, menu)

val output = File("/Users/teodorpenkov/GitHubWorkspace/Alma/test.html")
output.writeText(weeklyMenu)