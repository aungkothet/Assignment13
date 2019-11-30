package io.github.aungkothet.padc.assignment13.delegates

import io.github.aungkothet.padc.assignment13.data.vos.ArticleVO

interface ArticleItemDelegate {

    fun onArticleItemClicked(data: ArticleVO)
}