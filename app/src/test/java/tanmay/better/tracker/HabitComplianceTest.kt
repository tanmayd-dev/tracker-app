package tanmay.better.tracker

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitComplianceTest {

    @Test
    fun goodHabit_countLessThanTarget_isFalse() {
        assertFalse(HabitCompliance.isDayPassed(Sentiment.GOOD, count = 0, target = 1))
        assertFalse(HabitCompliance.isDayPassed(Sentiment.GOOD, count = 2, target = 3))
    }

    @Test
    fun goodHabit_countEqualsTarget_isTrue() {
        assertTrue(HabitCompliance.isDayPassed(Sentiment.GOOD, count = 1, target = 1))
        assertTrue(HabitCompliance.isDayPassed(Sentiment.GOOD, count = 3, target = 3))
    }

    @Test
    fun goodHabit_countGreaterThanTarget_isTrue() {
        assertTrue(HabitCompliance.isDayPassed(Sentiment.GOOD, count = 2, target = 1))
        assertTrue(HabitCompliance.isDayPassed(Sentiment.GOOD, count = 5, target = 3))
    }

    @Test
    fun badHabit_avoidance_targetZero_countZero_isTrue() {
        assertTrue(HabitCompliance.isDayPassed(Sentiment.BAD, count = 0, target = 0))
    }

    @Test
    fun badHabit_avoidance_targetZero_countGreaterThanZero_isFalse() {
        assertFalse(HabitCompliance.isDayPassed(Sentiment.BAD, count = 1, target = 0))
        assertFalse(HabitCompliance.isDayPassed(Sentiment.BAD, count = 5, target = 0))
    }

    @Test
    fun badHabit_limitHarmReduction_countLessThanOrEqualToTarget_isTrue() {
        assertTrue(HabitCompliance.isDayPassed(Sentiment.BAD, count = 0, target = 2))
        assertTrue(HabitCompliance.isDayPassed(Sentiment.BAD, count = 1, target = 2))
        assertTrue(HabitCompliance.isDayPassed(Sentiment.BAD, count = 2, target = 2))
    }

    @Test
    fun badHabit_limitHarmReduction_countGreaterThanTarget_isFalse() {
        assertFalse(HabitCompliance.isDayPassed(Sentiment.BAD, count = 3, target = 2))
        assertFalse(HabitCompliance.isDayPassed(Sentiment.BAD, count = 10, target = 2))
    }
}
