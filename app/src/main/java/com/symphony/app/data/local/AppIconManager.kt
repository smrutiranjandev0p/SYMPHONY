package com.symphony.app.data.local

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

/**
 * Keeps the single official SYMPHONY launcher entry enabled.
 *
 * Older releases exposed Desktop, Pure White and Obsidian Black aliases.
 * Those aliases are removed from the manifest; this startup reconciliation
 * also repairs a previously selected alternate icon by re-enabling the
 * surviving MainActivity launcher alias after an in-place app update.
 */
object AppIconManager {
    private const val TAG = "AppIconManager"
    private const val MAIN_LAUNCHER_ALIAS = "com.symphony.app.MainActivity"
    private val lock = Any()

    private fun mainLauncher(context: Context) =
        ComponentName(context.packageName, MAIN_LAUNCHER_ALIAS)

    fun isIconActive(context: Context, icon: AppIcon): Boolean = try {
        context.packageManager.getComponentEnabledSetting(mainLauncher(context)) !=
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    } catch (_: Exception) {
        false
    }

    /** Re-enable the only launcher alias; safe to call at every app start. */
    fun reconcile(context: Context, icon: AppIcon = AppIcon.DEFAULT) {
        synchronized(lock) {
            try {
                val pm = context.packageManager
                val component = mainLauncher(context)
                if (pm.getComponentEnabledSetting(component) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                    pm.setComponentEnabledSetting(
                        component,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP,
                    )
                    Log.i(TAG, "Restored the official Purple Glass launcher icon")
                }
            } catch (error: Exception) {
                Log.w(TAG, "Could not reconcile the official launcher icon", error)
            }
        }
    }

    /** Compatibility entry point for any older caller; there is only one icon now. */
    fun applyIcon(context: Context, icon: AppIcon) = reconcile(context, AppIcon.DEFAULT)
}
