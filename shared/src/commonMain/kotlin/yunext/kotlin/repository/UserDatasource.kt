package yunext.kotlin.repository

import yunext.kotlin.domain.User

interface UserDatasource {

    suspend fun find(username: String): User?

    suspend fun add(user: User): Boolean

    suspend fun delete(user: User): Boolean

    suspend fun list(user: User): List<User>
}

private class UserDatasourceImpl : UserDatasource {
    override suspend fun find(username: String): User? {
        TODO("Not yet implemented")
    }

    override suspend fun add(user: User): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun delete(user: User): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun list(user: User): List<User> {
        TODO("Not yet implemented")
    }

}