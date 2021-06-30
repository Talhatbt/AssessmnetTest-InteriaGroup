@file:Suppress("DEPRECATION")

package com.assessmenttest.extensions
import androidx.annotation.AnimRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders

import com.assessmenttest.factory.ViewModelFactory
import kotlin.reflect.KClass

// ============================Bind viewModel with lazy================================
inline fun <reified viewModelType : ViewModel> Fragment.bindViewModel() =
        bindViewModel(viewModelType::class)

@PublishedApi
internal fun <viewModelT : ViewModel> Fragment.bindViewModel(viewModelType: KClass<viewModelT>)
        : Lazy<viewModelT> = lazy {
    ViewModelProviders.of(this, ViewModelFactory.getInstance()).get(viewModelType.java)
}
// ============================Bind viewModel with lazy================================

fun Fragment.popStack() = activity?.supportFragmentManager?.popBackStack()

fun Fragment.replaceFragment(
    containerId: Int,
    fragment: Fragment,
    addToStack: Boolean = false,
    @AnimRes animRes: Array<Int>? = null) =

    activity?.supportFragmentManager?.transact {
        if (animRes != null)
            if (animRes.size == 2)
                setCustomAnimations(animRes[0], animRes[1])
            else
                setCustomAnimations(animRes[0], animRes[1], animRes[2], animRes[3])
        replace(containerId, fragment, fragment.javaClass.name)
        if (addToStack)
            addToBackStack(fragment.javaClass.name)
    }



/**
 * fragment in slide animation
 */
//@AnimRes
//fun Fragment.getInAnimation() =
//    arrayOf(R.anim.enter_from_right, R.anim.exit_to_left)

/**
 * fragment in and out slide animation
 */
//@AnimRes
//fun Fragment.getInOutAnimation() =
//    arrayOf(
//        R.anim.enter_from_right,
//        R.anim.exit_to_left,
//        R.anim.enter_from_left,
//        R.anim.exit_to_right
//    )
