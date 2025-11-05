package eu.mcomputing.mobv.zadanie

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DbDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserItems(items: List<UserEntity>)

    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserItem(uid: Int): LiveData<UserEntity?>

    @Query("SELECT * FROM users")
    fun getUsers(): LiveData<List<UserEntity?>>

    @Query("DELETE FROM users")
    suspend fun deleteUserItems()

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?
}
