package io.github.aungkothet.padc.assignment13.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import io.github.aungkothet.padc.assignment13.R
import io.github.aungkothet.padc.assignment13.data.vos.ArticleVO
import io.github.aungkothet.padc.assignment13.delegates.ArticleItemDelegate
import io.github.aungkothet.padc.assignment13.viewholders.ArticleViewHolder

class ArticlesAdapter(private val delegate: ArticleItemDelegate) :
    BaseAdapter<ArticleViewHolder, ArticleVO>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_holder_item_article, parent, false)
        return ArticleViewHolder(itemView, delegate)
    }

}