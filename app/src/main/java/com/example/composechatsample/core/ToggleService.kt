package com.example.composechatsample.core

import android.content.Context
import android.content.SharedPreferences

class ToggleService private constructor(private val sharedPreferences: SharedPreferences) {

    public fun isEnabled(featureKey: String): Boolean =
        sharedPreferences.getBoolean(featureKey, false)

    public fun setToggle(featureKey: String, value: Boolean) {
        sharedPreferences.edit()
            .putBoolean(featureKey, value)
            .commit()
    }

    @Suppress("UNCHECKED_CAST")
    public fun getToggles(): Map<String, Boolean> =
        sharedPreferences.all.filter { it.value is Boolean }.toMap() as Map<String, Boolean>

    public companion object {
        private const val PREFS_NAME = "toggle_storage"

        private var instance: ToggleService? = null

        internal fun isInitialized() = instance != null

        public fun instance(): ToggleService = requireNotNull(instance) {
            "Toggle service must be initialized via the init method!"
        }

        public fun init(appContext: Context, predefinedValues: Map<String, Boolean> = emptyMap()): ToggleService {
            val sp = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).also {
                predefinedValues.entries.forEach { (key, value) ->
                    if (it.contains(key).not()) {
                        it.edit().putBoolean(key, value).apply()
                    }
                }
            }

            return ToggleService(sp).also { instance = it }
        }

        public fun isEnabled(featureKey: String): Boolean {
            return instance?.isEnabled(featureKey) ?: false
        }
    }
}