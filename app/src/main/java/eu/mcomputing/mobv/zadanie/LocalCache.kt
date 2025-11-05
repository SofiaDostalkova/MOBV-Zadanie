package eu.mcomputing.mobv.zadanie

import androidx.lifecycle.LiveData

class LocalCache(private val dao: DbDao) {

    suspend fun logoutUser() {
        deleteUserItems()
    }

    suspend fun insertUserItems(items: List<UserEntity>) {
        dao.insertUserItems(items)
    }

    fun getUserItem(uid: Int): LiveData<UserEntity?> {
        return dao.getUserItem(uid)
    }

    fun getUsers(): LiveData<List<UserEntity?>> = dao.getUsers()

    suspend fun getUserByUsername(username: String): UserEntity? {
        return dao.getUserByUsername(username)
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return dao.getUserByEmail(email)
    }

    suspend fun deleteUserItems() {
        dao.deleteUserItems()
    }
}
