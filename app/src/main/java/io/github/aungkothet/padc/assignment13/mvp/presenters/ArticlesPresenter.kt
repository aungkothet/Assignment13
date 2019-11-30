package io.github.aungkothet.padc.assignment13.mvp.presenters

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import io.github.aungkothet.padc.assignment13.data.models.*
import io.github.aungkothet.padc.assignment13.mvp.views.ArticlesView
import io.github.aungkothet.padc.assignment13.data.vos.ArticleVO
import io.github.aungkothet.padc.assignment13.delegates.ArticleItemDelegate

class ArticlesPresenter : BaseGoogleSignInPresenter<ArticlesView>(), ArticleItemDelegate {


    private val model: FirebaseModel = FireStoreModelImpl
//    private val model: FirebaseModel = FirebaseModelImpl
    private val userModel: UserAuthenticationModel = UserAuthenticationModelImpl
    private val clearedLiveData = MutableLiveData<Unit>()

    fun onUIReady(owner: LifecycleOwner) {
        model.getAllArticles(clearedLiveData).observe(owner, Observer {
            mView.showArticles(it)
        })
    }

    override fun onArticleItemClicked(data: ArticleVO) {
        mView.navigateToDetail(data.id)
    }

    override fun onCleared() {
        clearedLiveData.value = Unit
        super.onCleared()
    }

    fun onStart() {
        userModel.currentUser?.let {
            mView.showLoginUser(it)
        } ?: mView.showLogoutUser()
    }

    fun onUserProfileClicked(context: Context) {
        if (userModel.isLoginUser()) {
            userModel.logOut()
            onStart()

        } else {
            googleSignIn(context)
        }
    }
}