package tanmay.better.tracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Semantic Habit Colors
val EmeraldGreen = Color(0xFF10B981)
val EmeraldDark = Color(0xFF047857)
val EmeraldLight = Color(0xFF34D399)

val CrimsonAlert = Color(0xFFDC2626)
val CrimsonDark = Color(0xFF991B1B)
val CrimsonLight = Color(0xFFF87171)

val AmberAlert = Color(0xFFF59E0B)
val AmberDark = Color(0xFFD97706)
val AmberLight = Color(0xFFFBBF24)

object HabitColors {
    @Composable
    fun emeraldPrimary(): Color = if (isSystemInDarkTheme()) EmeraldLight else EmeraldDark

    @Composable
    fun emeraldContainer(): Color = if (isSystemInDarkTheme()) Color(0xFF064E3B) else Color(0xFFD1FAE5)

    @Composable
    fun onEmeraldContainer(): Color = if (isSystemInDarkTheme()) Color(0xFFA7F3D0) else Color(0xFF065F46)

    @Composable
    fun crimsonPrimary(): Color = if (isSystemInDarkTheme()) CrimsonLight else CrimsonAlert

    @Composable
    fun crimsonContainer(): Color = if (isSystemInDarkTheme()) Color(0xFF450A0A) else Color(0xFFFEE2E2)

    @Composable
    fun onCrimsonContainer(): Color = if (isSystemInDarkTheme()) Color(0xFFFECACA) else Color(0xFF991B1B)

    @Composable
    fun amberPrimary(): Color = if (isSystemInDarkTheme()) AmberLight else AmberDark

    @Composable
    fun amberContainer(): Color = if (isSystemInDarkTheme()) Color(0xFF451A03) else Color(0xFFFEF3C7)

    @Composable
    fun onAmberContainer(): Color = if (isSystemInDarkTheme()) Color(0xFFFDE68A) else Color(0xFF92400E)

    @Composable
    fun adherenceColor(rate: Float): Color = when {
        rate >= 0.80f -> emeraldPrimary()
        rate >= 0.50f -> amberPrimary()
        else -> crimsonPrimary()
    }

    @Composable
    fun adherenceContainer(rate: Float): Color = when {
        rate >= 0.80f -> emeraldContainer()
        rate >= 0.50f -> amberContainer()
        else -> crimsonContainer()
    }

    @Composable
    fun onAdherenceContainer(rate: Float): Color = when {
        rate >= 0.80f -> onEmeraldContainer()
        rate >= 0.50f -> onAmberContainer()
        else -> onCrimsonContainer()
    }

    @Composable
    fun calmGreenContainer(): Color = if (isSystemInDarkTheme()) Color(0xFF064E3B) else Color(0xFFDCFCE7)

    @Composable
    fun calmGreenText(): Color = if (isSystemInDarkTheme()) Color(0xFF86EFAC) else Color(0xFF15803D)

    @Composable
    fun calmGreenBorder(): Color = if (isSystemInDarkTheme()) Color(0xFF059669) else Color(0xFF86EFAC)

    @Composable
    fun calmContainer(): Color = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFF1F5F9)

    @Composable
    fun onCalmContainer(): Color = if (isSystemInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF475569)
}
