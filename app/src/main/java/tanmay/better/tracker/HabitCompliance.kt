package tanmay.better.tracker

object HabitCompliance {
    fun isDayPassed(sentiment: Sentiment, count: Int, target: Int): Boolean = when (sentiment) {
        Sentiment.GOOD -> count >= target
        Sentiment.BAD -> count <= target
    }
}
