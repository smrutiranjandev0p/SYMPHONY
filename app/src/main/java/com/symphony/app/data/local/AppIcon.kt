package com.symphony.app.data.local

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.symphony.app.R

/**
 * SYMPHONY has one official visual identity. Kept as an enum so saved
 * preferences from older releases migrate safely to the single DEFAULT icon.
 */
enum class AppIcon(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val componentName: String,
    @DrawableRes val logoRes: Int,
    @ColorRes val backgroundRes: Int,
) {
    DEFAULT(
        id = "default",
        titleRes = R.string.app_icon_default,
        subtitleRes = R.string.app_icon_default_sub,
        componentName = "com.symphony.app.MainActivity",
        logoRes = R.drawable.ic_launcher_purple_glass,
        backgroundRes = R.color.ic_launcher_background,
    );

    companion object {
        /** Any legacy saved choice (Desktop/White/Black) resolves to Purple Glass. */
        fun fromId(id: String?): AppIcon = DEFAULT
    }
}
