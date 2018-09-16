package menu

import com.beust.klaxon.*
import com.sun.tools.javac.util.Convert
import khronos.*
import java.util.*

data class MenuDate(val menu: Menu, val date: Date)

data class Menu(val items: List<Item>) {
    enum class Category {
        SALAD, SOUP, MAIN, DESSERT;

        companion object {
            fun categoryAsString(category: Category): String = when (category) {
                SALAD -> "САЛАТИ"
                SOUP -> "СУПИ"
                MAIN -> "ОСНОВНИ ЯСТИЯ"
                DESSERT -> "ДЕСЕРТИ"
            }

            fun categoryFromString(string: String): Category = when (string) {
                "САЛАТИ" -> SALAD
                "СУПИ" -> SOUP
                "ОСНОВНИ ЯСТИЯ" -> MAIN
                "ДЕСЕРТИ" -> DESSERT
                else -> {
                    throw Exception("Invalid category $string")
                }
            }
        }
    }

    data class Item(val category: Category, val name: String, val price: String) {

        private val jsonConverter = object: Converter {
            override fun canConvert(cls: Class<*>): Boolean = cls == Item::class.java

            override fun fromJson(jv: JsonValue): Any {
                jv.obj?.let {
                    return Item(
                            Category.categoryFromString(it.string("category") as String),
                            it.string("name") as String,
                            it.string("price") as String
                    )
                }

                throw Exception("Invalid jv: $jv")
            }

            override fun toJson(value: Any): String = json {
                obj(
                        "category" to Category.categoryAsString(category),
                        "name" to name,
                        "price" to price
                )
            }.toJsonString()
        }

        fun asJsonObject(): JsonObject = Klaxon().parseJsonObject(jsonConverter.toJson(this).reader())
    }

    val categories: Map<Category, List<Item>> = items.groupBy { it.category }

    private val jsonConverter = object: Converter {
        override fun canConvert(cls: Class<*>): Boolean = cls == Menu::class.java

        override fun fromJson(jv: JsonValue): Any {
            var categories = jv.obj?.array<JsonObject>("categories")
            var items = ArrayList<Item>()
            if (categories != null) {
                categories.forEach {
                    items.add(
                            Item(
                                    Category.categoryFromString(it.string("category") as String),
                                    it.string("name") as String,
                                    it.string("price") as String
                            )
                    )
                }
            }
            return Menu(items)
        }

        override fun toJson(value: Any): String {
            if (value is Menu) {
                return json {
                    val categories = value.categories.map {
                        val items = it.value.map {
                            obj(
                                    "name" to it.name,
                                    "price" to it.price
                            )
                        }
                        obj(
                                "category" to Category.categoryAsString(it.key),
                                "items" to array(items)
                        )

                    }
                    array(categories)
                }.toJsonString()
            }

            throw Exception("Invalid type.")
        }

    }

    fun asJsonArray(): JsonArray<*> {
        return Klaxon().parseJsonArray(jsonConverter.toJson(this).reader())
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        categories
            .forEach { category, items ->
                stringBuilder.append(Category.categoryAsString(category))
                stringBuilder.append("\n")
                stringBuilder.append("------------------------")
                stringBuilder.append("\n")
                items.forEachIndexed { index, item ->
                    stringBuilder.append("${index + 1}. ${item.name} ${item.price}лв.")
                    stringBuilder.append("\n")
                }
                stringBuilder.append("\n")
            }
        return stringBuilder.toString()
    }
}