import io.javalin.http.Context
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

data class AuthTokenDataClass(val id: Int, val user_id: Int){
    companion object{
        fun fromRow(resultRow: ResultRow) = AuthTokenDataClass(
            id = resultRow[AuthToken.id],
            user_id = resultRow[AuthToken.user_id]
        )
    }
}

object AuthToken: Table(){
    val id = integer("id").autoIncrement()
    val user_id = integer("user_id")
    override val primaryKey = PrimaryKey(id)
}

object AuthTokenController{
    fun GetAll(ctx: Context){
        Database.connect("jdbc:sqlite:C:/Users/jacks/sqlite/ex1.db")
        val authTokenList: MutableList<AuthTokenDataClass> = ArrayList<AuthTokenDataClass>()
        transaction{
            for(authToken in AuthToken.selectAll()){
                authTokenList.add(AuthTokenDataClass.fromRow(authToken))
            }
        }
        ctx.json(authTokenList)
    }
    fun CreateByUserID(input_user_id: Int): AuthTokenDataClass {
        return transaction{
             val token_id = AuthToken.insert {
                it[user_id] = input_user_id
            }[AuthToken.id]
            AuthToken.select{AuthToken.id eq token_id}.map{ AuthTokenDataClass.fromRow(it) }[0]
        }
    }
}