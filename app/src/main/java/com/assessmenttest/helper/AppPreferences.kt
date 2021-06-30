package com.assessmenttest.helper

import com.assessmenttest.utility.Utils
import com.assessmenttest.constants.PreferencesKeys

object AppPreferences {
    private val mSharedPreferences = Utils.getEncryptedSharedPreferences()


    var isFirstTime: Boolean
        get() = mSharedPreferences?.getBoolean(PreferencesKeys.FIRST_TIME, false)!!
        set(value) {
            mSharedPreferences?.edit()!!
                .putBoolean(PreferencesKeys.FIRST_TIME, value)
                .apply()
        }
}