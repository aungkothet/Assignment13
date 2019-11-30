package io.github.aungkothet.padc.assignment13.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var data: T? = null
        set(value) {
            field = value

            // non-null data pass to bind with views
            value?.let {
                bindData(value)
            }
        }

    protected abstract fun bindData(data: T)
}