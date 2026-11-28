package com.velaelina.cards

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily

// Single source of truth for the UI font family.
// Android's generic sans-serif normally resolves to Roboto.
// To adopt an approved custom font, bundle licensed local font resources
// and replace only this declaration, preserving weight mappings and offline use.
private val VelaFontFamily = FontFamily.SansSerif

internal val VelaTypography: Typography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = VelaFontFamily),
        displayMedium = base.displayMedium.copy(fontFamily = VelaFontFamily),
        displaySmall = base.displaySmall.copy(fontFamily = VelaFontFamily),
        headlineLarge = base.headlineLarge.copy(fontFamily = VelaFontFamily),
        headlineMedium = base.headlineMedium.copy(fontFamily = VelaFontFamily),
        headlineSmall = base.headlineSmall.copy(fontFamily = VelaFontFamily),
        titleLarge = base.titleLarge.copy(fontFamily = VelaFontFamily),
        titleMedium = base.titleMedium.copy(fontFamily = VelaFontFamily),
        titleSmall = base.titleSmall.copy(fontFamily = VelaFontFamily),
        bodyLarge = base.bodyLarge.copy(fontFamily = VelaFontFamily),
        bodyMedium = base.bodyMedium.copy(fontFamily = VelaFontFamily),
        bodySmall = base.bodySmall.copy(fontFamily = VelaFontFamily),
        labelLarge = base.labelLarge.copy(fontFamily = VelaFontFamily),
        labelMedium = base.labelMedium.copy(fontFamily = VelaFontFamily),
        labelSmall = base.labelSmall.copy(fontFamily = VelaFontFamily)
    )
}
