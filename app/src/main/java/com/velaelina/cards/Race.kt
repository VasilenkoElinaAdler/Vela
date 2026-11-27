package com.velaelina.cards
internal const val FINISH_ADVANCES = 5
internal const val INITIAL_LIVES = 3
internal enum class RaceOutcome { WIN, TIE, SECOND, LOST }
internal data class Race(val index: Int=0,val lives: Int=INITIAL_LIVES,val advances: Int=0,val attempts: Int=0,val selected: Int?=null,val lastWrong: Boolean=false) {
    val rival: Float get()=(attempts*FINISH_ADVANCES/6f).coerceAtMost(FINISH_ADVANCES.toFloat())
    val outcome: RaceOutcome? get()=when {
        lives==0 -> RaceOutcome.LOST
        advances<FINISH_ADVANCES -> null
        attempts<6 -> RaceOutcome.WIN
        attempts==6 -> RaceOutcome.TIE
        else -> RaceOutcome.SECOND
    }
    fun answer(choice:Int,correct:Int):Race {
        if(selected!=null||outcome!=null||choice !in 0..3) return this
        val wrong=choice!=correct
        return copy(lives=lives-if(wrong) 1 else 0,advances=advances+if(wrong) 0 else 1,attempts=attempts+1,selected=choice,lastWrong=wrong)
    }
    fun next():Race=if(selected==null||outcome!=null) this else copy(index=index+1,selected=null,lastWrong=false)
}
internal data class Knowledge(val discovered:Set<String> = emptySet(),val collected:Set<String> = emptySet()) {
    // Each unique card appears at most once in a race; answer locking prevents duplicate credit.
    fun correct(id:String):Knowledge=if(id in discovered) copy(collected=collected+id) else copy(discovered=discovered+id)
}
