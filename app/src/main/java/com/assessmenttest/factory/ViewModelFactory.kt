package com.assessmenttest.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.assessmenttest.models.TravellingViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>) =
            with(modelClass) {
                when {
                    isAssignableFrom(TravellingViewModel::class.java) -> TravellingViewModel()
                    else -> throw IllegalArgumentException("Unknown viewModel class $modelClass")
                }
            } as T


    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null

        fun getInstance() =
                instance
                        ?: synchronized(ViewModelFactory::class.java) {
                            instance
                                    ?: ViewModelFactory().also { instance = it }
                        }
    }


}