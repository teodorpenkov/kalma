package menu

import com.beust.klaxon.*
import khronos.*
import org.jtwig.JtwigModel
import org.jtwig.JtwigTemplate
import org.jtwig.environment.DefaultEnvironmentConfiguration
import org.jtwig.environment.EnvironmentFactory
import org.jtwig.resource.reference.ResourceReference
import java.util.*

class Renderer {

    fun renderDaily(menu: MenuDate): String {
        val model = KlaxonJson().obj(
                "day" to dayFromDate(menu.date),
                "date" to menu.date.toString("dd.MM.YYYY"),
                "categories" to menu.menu.asJsonArray()
        )


        return render("/template_daily.jtwig", model)
    }

    fun renderWeekly(commons: Menu, menus: List<MenuDate>): String {
        // soups - name price
        // salads - name price
        // days - day. day, items. name price

        val days = json {
            menus.map {
                val date = it.date.toString("dd.MM.") + " " + dayFromDate(it.date).toUpperCase()
                obj(
                    "day" to date,
                    "items" to array(
                            it.menu.items
                                    .filter { !commons.items.contains(it) }
                                    .map {
                                        obj(
                                                "name" to it.name,
                                                "price" to it.price
                                        )
                                    }
                    )
                )
            }
        }

        val model = json {
            obj(
                    "soups" to array(
                            commons.categories[Menu.Category.SOUP]!!.map { it.asJsonObject() }
                    ),
                    "salads" to array(
                            commons.categories[Menu.Category.SALAD]!!.map { it.asJsonObject() }
                    ),
                    "desserts" to array(
                            commons.categories[Menu.Category.DESSERT]!!.map { it.asJsonObject()}
                    ),
                    "days" to array(days)
            )
        }
        return render("/template_weekly.jtwig", model)
    }

    private fun render(template: String, model: JsonObject): String {
        val config = DefaultEnvironmentConfiguration()
        val factory = EnvironmentFactory()
        val environment = factory.create(config)

        val resource = ResourceReference(ResourceReference.FILE, javaClass.getResource(template).path)
        val twigTemplate = JtwigTemplate(environment, resource)
        val twigModel = JtwigModel.newModel(model)

        return twigTemplate.render(twigModel)
    }

    private fun dayFromDate(date: Date): String = when {
        date.isMonday() -> "Понеделник"
        date.isTuesday() -> "Вторник"
        date.isWednesday() -> "Сряда"
        date.isThursday() -> "Четвъртък"
        date.isFriday() -> "Петък"
        date.isSaturday() -> "Събота"
        date.isSunday() -> "Неделя"
        else -> {
            throw Exception("Invalid date.")
        }
    }
}