package com.assessmenttest.common

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.assessmenttest.R
import com.assessmenttest.models.TravellingData
import com.assessmenttest.ui.MainApp


/**
 * Binding widgets and do some functionality according to requirements
 */
object BindingAdapters {

    @BindingAdapter("app:items")
    @JvmStatic
    fun setItems(recyclerView: RecyclerView, list: List<Any>) {
        with(recyclerView.adapter as LastAdapter<Any>) {
            items = list
        }
    }

    @BindingAdapter("app:goneUnless")
    @JvmStatic
    fun setGoneUnless(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }


    @BindingAdapter("app:iconRes")
    @JvmStatic
    fun setImageResource(imageView: AppCompatImageView, imgRes: Int?) {
        if (imgRes == null) {
            imageView.visibility = View.GONE
        } else {
            imageView.visibility = View.VISIBLE
            imageView.setImageResource(imgRes)
        }
    }

    @BindingAdapter("app:setNumberBg")
    @JvmStatic
    fun setDestinationNumberBg(textview: AppCompatTextView, id:String) {
        if (textview == null) {
            textview.visibility = View.GONE
        } else {
            textview.visibility = View.VISIBLE
            textview.setBackgroundResource(R.drawable.bg_number)

            textview.text=id
            when {
                id.toInt() == 1 -> textview.backgroundTintList =
                    ContextCompat.getColorStateList(MainApp.getContext(), R.color.blue)
//                id.toInt() == size -> textview.backgroundTintList =
//                    ContextCompat.getColorStateList(MainApp.getContext(), R.color.orange_red)
                else -> textview.backgroundTintList =
                    ContextCompat.getColorStateList(MainApp.getContext(), R.color.forest_green)
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @BindingAdapter("app:imgWithUrl")
    @JvmStatic
    fun setImageWithURL(imageView: AppCompatImageView, imgUrl: String?) {
        if (imgUrl == org.apache.commons.lang.StringUtils.EMPTY) {
//            imageView.setBackgroundResource(R.drawable.attach)
        } else {
            imageView.visibility = View.VISIBLE
//            Glide.with(MainApp.context)
//                .load(Backend.card_image_URL + imgUrl)
//                .placeholder(R.drawable.ic_stethoscope) //placeholder
//                .error(R.drawable.ic_stethoscope) //error
//                .into(imageView);
        }
    }
}
