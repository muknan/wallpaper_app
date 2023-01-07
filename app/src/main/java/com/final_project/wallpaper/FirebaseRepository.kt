package com.final_project.wallpaper

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot


//Authenticating user for Firestore access using Anonymous user type on Firebase Console
class FirebaseRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    var lastVisible: DocumentSnapshot? = null
    private val pageSize: Long = 6

    fun getUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun queryWallpapers(): Task<QuerySnapshot> {
        if(lastVisible == null){
            //Load First Page
            return firebaseFirestore
                    .collection("images")
                    .orderBy("id", Query.Direction.DESCENDING)
                    .limit(6)
                    .get()
        } else {
            //Load Next Page
            return firebaseFirestore
                    .collection("images")
                    .orderBy("id", Query.Direction.DESCENDING)
                    .startAfter(lastVisible!!)
                    .limit(pageSize)
                    .get()
        }
    }

}