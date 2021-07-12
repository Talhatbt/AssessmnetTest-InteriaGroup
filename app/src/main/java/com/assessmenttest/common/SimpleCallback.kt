package com.assessmenttest.common

import com.assessmenttest.models.TravelDestinations

interface SimpleCallback<T> {
    @JvmDefault
    fun onCallBack(list: MutableList<TravelDestinations>){}

    @JvmDefault
    fun onCallBack(value:Boolean){}
}