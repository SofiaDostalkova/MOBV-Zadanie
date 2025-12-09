package eu.mcomputing.mobv.zadanie

import androidx.lifecycle.LiveData

class LocalCache(private val dao: DbDao) {

    suspend fun insertUserItems(items: List<UserEntity>) = dao.insertUserItems(items)

    fun getUserItem(uid: String): LiveData<UserEntity?> = dao.getUserItem(uid)

    fun getUsers(): LiveData<List<UserEntity?>> = dao.getUsers()

    suspend fun deleteUserItems() = dao.deleteUserItems()

    suspend fun clearUsers() = dao.deleteUserItems()

    suspend fun logoutUser() {
        deleteUserItems()
    }
}

