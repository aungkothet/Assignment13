package io.github.aungkothet.padc.assignment13.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import io.github.aungkothet.padc.assignment13.R
import io.github.aungkothet.padc.assignment13.data.vos.CommentVO
import io.github.aungkothet.padc.assignment13.viewholders.CommentViewHolder

class CommentsAdapter : BaseAdapter<CommentViewHolder, CommentVO>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.viewholder_comment, parent, false
        )

        return CommentViewHolder(view)
    }
}