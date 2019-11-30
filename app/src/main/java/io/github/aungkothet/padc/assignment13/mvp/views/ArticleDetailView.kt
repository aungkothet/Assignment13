package io.github.aungkothet.padc.assignment13.mvp.views

import android.net.Uri
import io.github.aungkothet.padc.assignment13.data.vos.ArticleVO

interface ArticleDetailView : BaseGoogleSignInView {

    fun showArticle(data: ArticleVO)
    fun showCommentInputView()
    fun showPickedImage(uri: Uri)
}