package com.example.easy_memorization.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// ── Brand: calm indigo-violet accent, minimal modern AI-app feel ──

// Light
val IndigoPrimaryLight = Color(0xFF4A5CD6)
val IndigoOnPrimaryLight = Color(0xFFFFFFFF)
val IndigoPrimaryContainerLight = Color(0xFFE1E5FF)
val IndigoOnPrimaryContainerLight = Color(0xFF1B1F4E)
val IndigoSecondaryLight = Color(0xFF5A5D72)
val IndigoSecondaryContainerLight = Color(0xFFDFE1F5)
val IndigoOnSecondaryContainerLight = Color(0xFF171A2C)
val IndigoTertiaryLight = Color(0xFF7A4FE0)
val IndigoTertiaryContainerLight = Color(0xFFEBDDFF)
val IndigoOnTertiaryContainerLight = Color(0xFF2C0E6E)

// Dark
val IndigoPrimaryDark = Color(0xFFB9C3FF)
val IndigoOnPrimaryDark = Color(0xFF2B3072)
val IndigoPrimaryContainerDark = Color(0xFF3E47A8)
val IndigoOnPrimaryContainerDark = Color(0xFFE1E5FF)
val IndigoSecondaryDark = Color(0xFFC3C5DD)
val IndigoSecondaryContainerDark = Color(0xFF434659)
val IndigoOnSecondaryContainerDark = Color(0xFFE0E2F8)
val IndigoTertiaryDark = Color(0xFFD6BFFF)
val IndigoTertiaryContainerDark = Color(0xFF4E2A9E)
val IndigoOnTertiaryContainerDark = Color(0xFFF2E8FF)

// ── Semantic success colors (theme-aware, for correct answers) ──
val SuccessLight = Color(0xFF2F7D33)
val OnSuccessLight = Color(0xFFFFFFFF)
val SuccessContainerLight = Color(0xFFD2EED6)
val OnSuccessContainerLight = Color(0xFF0E3B16)

val SuccessDark = Color(0xFF8BD89A)
val OnSuccessDark = Color(0xFF0A3A16)
val SuccessContainerDark = Color(0xFF1C5A28)
val OnSuccessContainerDark = Color(0xFFD2EED6)

/** Picks the matching success tone for the current (light/dark) color scheme. */
val ColorScheme.success: Color
    get() = if (background.luminance() < 0.5f) SuccessDark else SuccessLight

val ColorScheme.onSuccess: Color
    get() = if (background.luminance() < 0.5f) OnSuccessDark else OnSuccessLight

val ColorScheme.successContainer: Color
    get() = if (background.luminance() < 0.5f) SuccessContainerDark else SuccessContainerLight

val ColorScheme.onSuccessContainer: Color
    get() = if (background.luminance() < 0.5f) OnSuccessContainerDark else OnSuccessContainerLight
