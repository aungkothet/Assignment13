package io.github.aungkothet.padc.assignment13.mvp.views

import com.google.firebase.auth.FirebaseUser
import io.github.aungkothet.padc.assignment13.data.vos.ArticleVO

interface ArticlesView : BaseGoogleSignInView {

    fun navigateToDetail(id: String)
    fun showArticles(data: List<ArticleVO>)
    fun showLoginUser(user: FirebaseUser)
    fun showLogoutUser()
}