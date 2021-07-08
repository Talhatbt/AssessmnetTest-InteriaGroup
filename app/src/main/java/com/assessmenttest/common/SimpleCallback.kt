package com.assessmenttest.common

import com.assessmenttest.models.TravelDestinations

interface SimpleCallback<T> {
    fun onCallBack(list: MutableList<TravelDestinations>)
}