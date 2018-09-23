package menu

import khronos.day
import khronos.isMonday
import khronos.plus
import khronos.toDate
import java.util.*
import kotlin.system.exitProcess

abstract class AlmaParser<T> {

    protected var currentTokenIndex: Int = 0
    private var tokens: MutableList<String> = mutableListOf()
    private var currentCategory: String? = null

    fun parseFile(file: String): T {
        tokens.addAll(file.split("\n"))
        println("tokens $tokens")
        consumeTokens()
        return build()
    }

    protected abstract fun build(): T

    protected open fun onConsumeTokens() {
    }

    private fun consumeTokens() {
        while (currentTokenIndex < tokens.size) {
            consumeCurrentToken()
        }
        onConsumeTokens()
    }

    protected open fun canConsumeToken(token: String): Boolean = false

    protected open fun consumeToken(token: String) {
    }

    protected open fun onCategoryItem(item: Menu.Item) {
    }

    protected fun currentCategory(): Menu.Category = Menu.Category.categoryFromString(currentCategory!!)

    private fun currentToken(): String? = if (currentTokenIndex < tokens.size) tokens[currentTokenIndex] else null

    private fun previousToken(): String? = if (currentTokenIndex - 1 >= 0) tokens[currentTokenIndex - 1] else null

    private fun consumeCurrentToken() {
        val currentToken = currentToken()
        if (currentToken != null) {
            when {
                canConsumeToken(currentToken) -> consumeToken(currentToken)
                currentToken.startsWith("-") -> currentTokenIndex++
                previousToken()?.startsWith("-") ?: false -> consumeCategory()
                else -> consumeItemsForCategory(currentCategory!!)
            }
        }
    }

    private fun consumeCategory() {
        currentCategory = currentToken()
        println("current category: $currentCategory!!")
        currentTokenIndex++
    }

    private fun consumeItemsForCategory(category: String) {
        var currentToken = currentToken()
        while (currentToken != null && !currentToken.startsWith("-")) {
            println("consuming $category")
            var itemTokens = currentToken.split(" - ")
            var name = itemTokens[0]
            var price = itemTokens[1]
            onCategoryItem(Menu.Item(Menu.Category.categoryFromString(category), name, price))
            currentTokenIndex++
            currentToken = currentToken()
        }
    }
}

class CommonsParser: AlmaParser<Menu>() {

    private var items: MutableList<Menu.Item> = mutableListOf()

    override fun build(): Menu = Menu(items)

    override fun canConsumeToken(token: String): Boolean = token.startsWith("Всеки")

    override fun consumeToken(token: String) {
        if (token.startsWith("Всеки")) {
            currentTokenIndex++
        }
    }

    override fun onCategoryItem(item: Menu.Item) {
        items.add(item)
    }
}

class MenuParser(private val commons: Menu): AlmaParser<List<MenuDate>>() {
    private class MenuDateBuilder {
        private lateinit var date: Date
        private var items: MutableList<Menu.Item> = mutableListOf()

        fun date(date: Date) = apply { this.date = date }

        fun addItem(item: Menu.Item) = apply { items.add(item) }

        fun build(): MenuDate = MenuDate(Menu(items), date)

        fun isMenuInProgress() = items.size > 0
    }

    private var currentMenuBuilder = MenuDateBuilder()
    private lateinit var date: Date
    private var menuDates: MutableList<MenuDate> = mutableListOf()

    override fun build(): List<MenuDate> = menuDates

    override fun canConsumeToken(token: String): Boolean {
        return token.startsWith("Обедно меню") ||
                token.startsWith("*") ||
                token.startsWith("<<")
    }

    override fun consumeToken(token: String) {
        when {
            token.startsWith("Обедно меню") -> consumeDate(token.substringAfter("- "))
            token.startsWith("*") -> beginMenu()
            token.startsWith("<<") -> copyCommonsToCurrentCategory()
        }
    }

    override fun onConsumeTokens() {
        menuDates.add(currentMenuBuilder.build())
    }

    override fun onCategoryItem(item: Menu.Item) {
        currentMenuBuilder.addItem(item)
    }

    private fun consumeDate(dateToken: String) {
        date = dateToken.toDate("dd.MM.yyyy")
        if (!date.isMonday()) {
            println("Начална дата трябва да е понеделник.")
            exitProcess(1)
        }
        currentTokenIndex += 1
    }

    private fun beginMenu() {
        if (currentMenuBuilder.isMenuInProgress()) {
            menuDates.add(currentMenuBuilder.build())
        }
        currentMenuBuilder = MenuDateBuilder()
        currentMenuBuilder.date(date)
        date += 1.day
        currentTokenIndex++
    }

    private fun copyCommonsToCurrentCategory() {
        commons.categories[currentCategory()]?.forEach {
            currentMenuBuilder.addItem(it)
        }
        currentTokenIndex++
    }
}
