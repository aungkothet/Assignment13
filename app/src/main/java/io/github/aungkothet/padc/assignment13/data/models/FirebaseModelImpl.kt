package io.github.aungkothet.padc.assignment13.data.models

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.github.aungkothet.padc.assignment13.data.vos.ArticleVO
import io.github.aungkothet.padc.assignment13.data.vos.CommentVO
import io.github.aungkothet.padc.assignment13.data.vos.UserVO
import io.github.aungkothet.padc.assignment13.utils.REF_KEY_CLAP_COUNT
import io.github.aungkothet.padc.assignment13.utils.REF_KEY_COMMENTS
import io.github.aungkothet.padc.assignment13.utils.REF_PATH_ARTICLES
import io.github.aungkothet.padc.assignment13.utils.STORAGE_FOLDER_PATH

object FirebaseModelImpl : FirebaseModel {

    const val TAG = "FirebaseModel"

    private val databaseRef = FirebaseDatabase.getInstance().reference
//    private val dbFirestore = FirebaseFirestore.getInstance().collection("articlesBK")

    override fun getAllArticles(cleared: LiveData<Unit>): LiveData<List<ArticleVO>> {
        val liveData = MutableLiveData<List<ArticleVO>>()

        val articlesRef = databaseRef.child(REF_PATH_ARTICLES)

        // Read from the database
        val realTimeListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Log.d(TAG, "Key is: ${dataSnapshot.key}")

                val articles = ArrayList<ArticleVO>()

                for (articleData in dataSnapshot.children) {
                    val article = articleData.getValue(ArticleVO::class.java)
                    article?.let {

                        articles.add(article)
//                        val fsArticle = hashMapOf(
//                            "body" to article.body,
//                            "claps" to article.claps,
//                            "comments" to article.comments,
//                            "comment_count" to article.comments_count,
//                            "date" to article.date,
//                            "id" to article.id,
//                            "img" to article.img,
//                            "title" to article.title
//                        )
//
//                        dbFirestore
//                            .document(article.id)
//                            .set(fsArticle)
//                            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
//                            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                    }
                }
                Log.d(TAG, "Value is: $articles")

                liveData.value = articles
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        }

        // Start real-time data observing
        articlesRef.addValueEventListener(realTimeListener)

        // Stop real-time data observing when Presenter's onCleared() was called
        cleared.observeForever(object : Observer<Unit> {
            override fun onChanged(unit: Unit?) {
                unit?.let {
                    articlesRef.removeEventListener(realTimeListener)
                    cleared.removeObserver(this)
                }
            }
        })

        return liveData
    }

    override fun getArticleById(id: String, cleared: LiveData<Unit>): LiveData<ArticleVO> {
        val liveData = MutableLiveData<ArticleVO>()

        val articleRef = databaseRef.child(REF_PATH_ARTICLES).child(id)

        // Read from the database
        val realTimeListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Log.d(TAG, "Key is: ${dataSnapshot.key}")

                val article: ArticleVO? = dataSnapshot.getValue(ArticleVO::class.java)?.also {
                    liveData.value = it
                }

                Log.d(TAG, "Value is: $article")

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        }

        // Start real-time data observing
        articleRef.addValueEventListener(realTimeListener)

        // Stop real-time data observing when Presenter's onCleared() was called
        cleared.observeForever(object : Observer<Unit> {
            override fun onChanged(unit: Unit?) {
                unit?.let {
                    articleRef.removeEventListener(realTimeListener)
                    cleared.removeObserver(this)
                }
            }
        })

        return liveData
    }

    override fun updateClapCount(count: Int, article: ArticleVO) {
        val articleRef = databaseRef.child(REF_PATH_ARTICLES).child(article.id)
        articleRef.child(REF_KEY_CLAP_COUNT).setValue(count + article.claps)
            .addOnSuccessListener {
                Log.d(TAG, "Clap Count ++")
            }
            .addOnFailureListener {
                Log.e(TAG, "Clap Count ++ error ${it.localizedMessage}")
            }
    }

    override fun addComment(comment: String, pickedImage: Uri?, article: ArticleVO) {

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
            databaseRef.child(REF_PATH_ARTICLES).child(article.id).child(REF_KEY_COMMENTS)

        val key = comment.id

        commentsRef.child(key).setValue(comment)
            .addOnSuccessListener {
                Log.d(TAG, "Add Comment")
            }
            .addOnFailureListener {
                Log.e(TAG, "Add Comment error ${it.localizedMessage}")
            }

    }
}