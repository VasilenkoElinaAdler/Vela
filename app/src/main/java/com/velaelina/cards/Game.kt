package com.velaelina.cards

internal data class CardData(val id: String, val category: String, val level: Int, val question: String, val options: List<String>, val correct: Int, val explanation: String)
internal data class Round(val index: Int = 0, val lives: Int = 3, val stars: Int = 0, val selected: Int? = null) {
    fun answer(choice: Int, correct: Int): Round {
        if (selected != null || lives == 0 || choice !in 0..3) return this
        return copy(lives = lives - if (choice == correct) 0 else 1, stars = stars + if (choice == correct) 1 else 0, selected = choice)
    }
    fun finished(total: Int): Boolean = selected != null && (lives == 0 || index + 1 >= total)
    fun next(total: Int): Round = if (selected == null || finished(total)) this else copy(index = index + 1, selected = null)
}
