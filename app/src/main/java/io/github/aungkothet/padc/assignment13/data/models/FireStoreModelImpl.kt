package io.github.aungkothet.padc.assignment13.data.models

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import io.github.aungkothet.padc.assignment13.data.vos.ArticleVO
import io.github.aungkothet.padc.assignment13.data.vos.CommentVO
import io.github.aungkothet.padc.assignment13.data.vos.UserVO
import io.github.aungkothet.padc.assignment13.utils.REF_KEY_CLAP_COUNT
import io.github.aungkothet.padc.assignment13.utils.REF_KEY_COMMENTS
import io.github.aungkothet.padc.assignment13.utils.REF_PATH_ARTICLES
import io.github.aungkothet.padc.assignment13.utils.STORAGE_FOLDER_PATH


object FireStoreModelImpl : FirebaseModel {

    const val TAG = "FireStoreModel"

    private val databaseRef = FirebaseFirestore.getInstance()

    override fun getAllArticles(cleared: LiveData<Unit>): LiveData<List<ArticleVO>> {
        val liveData = MutableLiveData<List<ArticleVO>>()

        val articlesRef = databaseRef.collection(REF_PATH_ARTICLES)

        // Read from the database
        val realTimeListener = object : EventListener<QuerySnapshot> {
            override fun onEvent(
                documentSnapshots: QuerySnapshot?,
                e: FirebaseFirestoreException?
            ) {
                if (e != null) {
                    Log.w(TAG, "onEvent:error", e)
                    return
                }
                val articles = ArrayList<ArticleVO>()
                for (snapshot in documentSnapshots?.documents!!) {
                    val article = snapshot.toObject(ArticleVO::class.java)
                    article?.let {
                        articles.add(article)
                    }
                }

                Log.d(TAG, "Value is: $articles")

                liveData.value = articles
            }
        }

        // Start real-time data observing
        val listenerRegister = articlesRef.addSnapshotListener(realTimeListener)

        // Stop real-time data observing when Presenter's onCleared() was called
        cleared.observeForever(object : Observer<Unit> {
            override fun onChanged(unit: Unit?) {
                unit?.let {
                    listenerRegister.remove()
                    cleared.removeObserver(this)
                }
            }
        })

        return liveData
    }

    override fun getArticleById(id: String, cleared: LiveData<Unit>): LiveData<ArticleVO> {
        val liveData = MutableLiveData<ArticleVO>()

        val articleRef = databaseRef.collection(REF_PATH_ARTICLES).document(id)

        // Start real-time data observing
        val listenerRegister = articleRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: ${snapshot.data}")
                val article = snapshot.toObject(ArticleVO::class.java)
                article?.also {
                    liveData.value = it
                }

            } else {
                Log.d(TAG, "Current data: null")
            }
        }

        // Stop real-time data observing when Presenter's onCleared() was called
        cleared.observeForever(object : Observer<Unit> {
            override fun onChanged(unit: Unit?) {
                unit?.let {
                    listenerRegister.remove()
                    cleared.removeObserver(this)
                }
            }
        })

        return liveData
    }

    override fun updateClapCount(count: Int, article: ArticleVO) {
        Log.d(TAG," updateClapCount Count $count, aritcleVo: $article")
        val articleRef = databaseRef.collection(REF_PATH_ARTICLES).document(article.id)
        val data = hashMapOf(REF_KEY_CLAP_COUNT to count + article.claps)
        articleRef.set(data, SetOptions.merge())
    }

    override fun addComment(comment: String, pickedImage: Uri?, article: ArticleVO) {

        Log.d(TAG,"addCOmment $comment, article: $article")
        if (pickedImage != null) {
            uploadImageAndAddComment(comment, pickedImage, article)

        } else {
            val currentUser = UserAuthenticationModelImpl.currentUser!!
            val newComment = CommentVO(
                System.currentTimeMillis().toString(), "", comment, UserVO(
                    currentUser.providerId,
                    currentUser.displayName ?: "",
                    currentUser.photoUrl.toString()
                )
            )
            addComment(newComment, article)
        }
    }

    private fun uploadImageAndAddComment(comment: String, pickedImage: Uri, article: ArticleVO) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imagesFolderRef = storageRef.child(STORAGE_FOLDER_PATH)


        val imageRef = imagesFolderRef.child(
            pickedImage.lastPathSegment ?: System.currentTimeMillis().toString()
        )

        val uploadTask = imageRef.putFile(pickedImage)

        uploadTask.addOnFailureListener {
            Log.e(TAG, it.localizedMessage)
        }
            .addOnSuccessListener {
                // get comment image's url

                imageRef.downloadUrl.addOnCompleteListener {
                    Log.d(TAG, "Image Uploaded ${it.result.toString()}")

                    val currentUser = UserAuthenticationModelImpl.currentUser!!
                    val newComment = CommentVO(
                        System.currentTimeMillis().toString(), it.result.toString(), comment,
                        UserVO(
                            currentUser.providerId,
                            currentUser.displayName ?: "",
                            currentUser.photoUrl.toString()
                        )
                    )

                    addComment(newComment, article)
                }

            }
    }

    private fun addComment(comment: CommentVO, article: ArticleVO) {
        val commentsRef =
            databaseRef.collection(REF_PATH_ARTICLES).document(article.id)
//                .collection(REF_KEY_COMMENTS)

        val key = comment.id

        val comments = article.comments.toMutableMap()
        comments[key] = comment

        val dataWrap = mapOf(REF_KEY_COMMENTS to comments)

        commentsRef.update(dataWrap)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document addComment", e) }

    }
}