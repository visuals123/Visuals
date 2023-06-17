package com.uc.ccs.visuals.utils.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.uc.ccs.visuals.screens.admin.tabs.users.UserItem
import com.uc.ccs.visuals.screens.settings.CsvData

class FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun saveMultipleData(collectionPath: String, data: List<CsvData>, onSuccess: () -> Unit, onFailure: (e: Exception) -> Unit) {
        val batch = firestore.batch()

        data.map { csvData ->
            val csvDataMap = hashMapOf<String, Any>()
            csvDataMap["id"] = csvData.id
            csvDataMap["code"] = csvData.code
            csvDataMap["title"] = csvData.title
            csvDataMap["description"] = csvData.description
            csvData.position.let { position ->
                val latLong = position.split("-")
                if (latLong.size == 2) {
                    val latitude = latLong[0].toDoubleOrNull()
                    val longitude = latLong[1].toDoubleOrNull()
                    if (latitude != null && longitude != null) {
                        val positionMap = hashMapOf<String, Any>(
                            "latitude" to latitude,
                            "longitude" to longitude
                        )
                        csvDataMap["position"] = positionMap
                    }
                }
            }
            csvDataMap["iconImageUrl"] = csvData.iconImageUrl ?: ""

            val documentReference = firestore.collection(collectionPath)
                .document(csvData.id)
            batch.set(documentReference,csvDataMap)
        }
        batch.commit()
            .addOnSuccessListener { documentReference ->
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun getCsvData(collectionPath: String, onSuccess: (List<CsvData>) -> Unit, onFailure: (e: Exception) -> Unit) {
        firestore.collection(collectionPath)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val csvDataList = mutableListOf<CsvData>()

                for (document in querySnapshot) {
                    val csvDataMap = document.data
                    val id = csvDataMap["id"] as? String
                    val code = csvDataMap["code"] as? String
                    val title = csvDataMap["title"] as? String
                    val description = csvDataMap["description"] as? String
                    val positionMap = csvDataMap["position"] as? Map<String, Any>
                    val iconImageUrl = csvDataMap["iconImageUrl"] as? String

                    if (id != null && title != null && description != null && positionMap != null && code != null) {
                        val latitude = positionMap["latitude"] as? Double
                        val longitude = positionMap["longitude"] as? Double

                        if (latitude != null && longitude != null) {
                            val position = "$latitude-$longitude"
                            val csvData = CsvData(
                                id = id,
                                code = code,
                                title = title,
                                description = description,
                                position = position,
                                iconImageUrl = iconImageUrl
                            )
                            csvDataList.add(csvData)
                        }
                    }
                }

                onSuccess(csvDataList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getUsers(collectionPath: String, onSuccess: (List<UserItem>) -> Unit, onFailure: (e: Exception) -> Unit) {
        firestore.collection(collectionPath)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val userList = mutableListOf<UserItem>()

                for (document in querySnapshot) {
                    val userDataMap = document.data
                    val id = document.id as? String
                    val firstname = userDataMap["firstName"] as? String
                    val lastname = userDataMap["lastName"] as? String
                    val email = userDataMap["email"] as? String

                    if (id != null && firstname != null && lastname != null && email != null) {
                        val user = UserItem(
                            id = id,
                            firstName = firstname,
                            lastName = lastname,
                            email = email
                        )
                        userList.add(user)
                    }
                }

                onSuccess(userList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getUserByEmail(collectionPath: String, email: String, onSuccess: (UserItem?) -> Unit, onFailure: (e: Exception) -> Unit) {
        firestore.collection(collectionPath)
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0]
                    val userDataMap = documentSnapshot.data
                    val id = documentSnapshot.id as? String
                    val firstname = userDataMap?.get("firstName") as? String
                    val lastname = userDataMap?.get("lastName") as? String
                    val userEmail = userDataMap?.get("email") as? String
                    val roles = userDataMap?.get("roles") as? Long

                    if (id != null && firstname != null && lastname != null && userEmail != null
                        && roles != null) {
                        val user = UserItem(
                            id = id,
                            firstName = firstname,
                            lastName = lastname,
                            email = userEmail,
                            roles = roles.toInt()
                        )
                        onSuccess(user)
                    } else {
                        onSuccess(null) // com.uc.ccs.visuals.screens.signup.User data is incomplete
                    }
                } else {
                    onSuccess(null) // com.uc.ccs.visuals.screens.signup.User not found
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

}