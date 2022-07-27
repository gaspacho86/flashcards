package flashcards

import java.io.File

fun main(args: Array<String>) {
    FlashCards(args)
}

class FlashCards(private val files: Array<String>) {

    private val log = mutableListOf<String>()

    init {
        with(mutableMapOf<String, List<String>>()) {
            files.getFile("-import").let { if (it.isNotEmpty()) importCards(it) }
            actions()
            files.getFile("-export").let { if (it.isNotEmpty()) exportCards(it) }

        }
    }

    private fun MutableMap<String, List<String>>.actions() {
        when (answerTo(
            "Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")) {
            "add" -> addCard(answerTo("The card:"))
            "remove" -> delCard(answerTo("Which card?"))
            "import" -> importCards(answerTo("File name:"))
            "export" -> exportCards(answerTo("File name:"))
            "ask" -> quiz(answerTo("How many times to ask?").toInt())
            "exit" -> message("Bye bye!").also { return }
            "log" -> log.saveLog(answerTo("File name:"))
            "hardest card" -> hardestCards()
            "reset stats" -> clearMistake()
        }
        actions()
    }

    private fun MutableMap<String, List<String>>.addCard(key: String) {
        with( { parameter: String -> "The $parameter already exists.\n" } ) {
            when {
                containsKey(key) -> message(this("card \"$key\""))
                else -> answerTo("The definition of the card:").let { value ->
                    when {
                        filter { it.value[0] == value }.isNotEmpty() -> message(this("definition \"$value\""))
                        else -> message ("The pair (\"$key\":\"${value}\") has been added.\n")
                            .also { put(key,listOf(value, "0")) }
                    }
                }
            }
        }
    }

    private fun MutableMap<String, List<String>>.delCard(key: String) = message(
        when(remove(key)) {
            null -> "Can't remove \"$key\": there is no such card.\n"
            else -> "The card has been removed.\n"
        }
    )

    private fun MutableMap<String, List<String>>.importCards(filename: String) = try {
        with(File(filename).readLines()) {
            this.map { it.split("||").let { card -> put(card[0], listOf(card[1], card[2])) } }
            message("${this.size} cards have been loaded.\n")
        }
    } catch (e: Exception) {
        message("File not found.\n")
    }

    private fun MutableMap<String, List<String>>.exportCards(filename: String) = File(filename)
        .writeText(map { "${ it.key }||${it.value[0]}||${it.value[1]}" }.joinToString("\n"))
        .also { message("$size cards have been saved.\n") }

    private fun MutableMap<String, List<String>>.quiz(number: Int) {
        repeat(number) {
            val key = toList()[kotlin.random.Random.nextInt(0, size)].first
            when (val definition = answerTo("Print the definition of \"${key}\":")) {
                this[key]!![0] -> message("Correct!")
                else -> filter { it.value[0] == definition }.keys.firstOrNull()
                    .let { message("Wrong. The right answer is \"${this[key]!![0]}\""
                            + if (!it.isNullOrEmpty()) ", but your definition is correct for \"$it\"" else ".")
                        .also { this[key] = listOf(this[key]!![0], (this[key]!![1].toInt() + 1).toString()) } }
            }
        }
    }

    private fun MutableMap<String, List<String>>.hardestCards() {
        with(filter { card -> card.value[1].toInt() == maxOf { it.value[1].toInt() } && card.value[1] != "0" } ) {
            if (this.isEmpty()) message("There are no cards with errors.\n").run { return }
            message(
                "The hardest ${if (this.size == 1) "card is" else "cards are"} " +
                        "\"${this.keys.joinToString("\", \"")}\"." +
                        " You have ${this.values.first()[1]} errors answering ${if (this.size == 1) "it" else "them"}.\n"
            )
        }
    }

    private fun MutableMap<String, List<String>>.clearMistake() {
        for (key in keys) this[key] = listOf(this[key]!![0], "0")
            .also { message("Card statistics have been reset.") }
    }

    private fun MutableList<String>.saveLog(filename: String) = File(filename).writeText(
        joinToString("\n") { it }).also { message("The log has been saved.") }

    private fun Array<String>.getFile(assign: String) = if (contains(assign)) this[indexOf(assign) + 1] else ""

    private fun answerTo(text: String) = message(text).run { readln().also { log += it } }

    private fun message(text: String) = print("${text}\n").also { log += text }
}