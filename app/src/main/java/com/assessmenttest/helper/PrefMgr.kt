package com.assessmenttest.helper

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * Class to handle Local Db for App Preferences objects...
 * AppPreferences class will be removed gradually
 */
class PrefMgr(context: Context) {
    private val TAG = PrefMgr::class.java.simpleName
    private var preferences: SharedPreferences? = null

    init {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    companion object {
        private var factory: PrefMgr? = null
        fun get(context: Context) =
                factory ?: synchronized(PrefMgr::class.java) {
                    factory ?: PrefMgr(context).also { factory = it }
                }
    }

    /**
     * Clear preference.. NOT in use right now
     */
    fun clearPrefs(): Boolean {
        return preferences?.edit()?.clear()?.commit()!!
    }

    /**
     * Put String object in local db
     * @param field key
     * @param value value
     */
    fun putString(field: String, value: String?) {
        preferences?.edit()?.putString(field, value)?.apply()
    }

    /**
     * Get String object from local db
     * @param field         key
     * @param defaultValue  value
     */
    fun getString(field: String, defaultValue: String? = null): String? {
        return preferences?.getString(field, defaultValue)
    }

    /**
     * Put Boolean object in local db
     * @param field key
     * @param value value
     */
    fun putBoolean(field: String, value: Boolean) {
        preferences?.edit()?.putBoolean(field, value)?.apply()
    }

    /**
     * Get Boolean object from local db
     * @param field         key
     * @param defaultValue  value
     */
    fun getBoolean(field: String, defaultValue: Boolean = false): Boolean {
        return preferences?.getBoolean(field, defaultValue)!!
    }

    /**
     * Preferences Keys
     */
    object Keys {
        const val FIRST_TIME = "FIRST_TIME"
    }

}