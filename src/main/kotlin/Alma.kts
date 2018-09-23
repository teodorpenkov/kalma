import menu.CommonsParser
import menu.MenuParser
import menu.Renderer
import java.io.File

val outputDir = "/Users/teodorpenkov/GitHubWorkspace/Alma/generated"
val dailyOutput = "/daily-%d.html"
val weeklyOutput = "/weekly.html"

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

saveOutputs(dailyMenus, weeklyMenu)

fun saveOutputs(dailyMenus: List<String>, weeklyMenu: String) {
    File(outputDir).mkdir()

    dailyMenus.forEachIndexed { index, html ->
        val daily = File(outputDir + dailyOutput.format(index))
        daily.writeText(html)
    }

    val weekly = File(outputDir + weeklyOutput)
    weekly.writeText(weeklyMenu)
}