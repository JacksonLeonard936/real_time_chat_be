import io.javalin.http.Context
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

data class UserDataClass(val id: Int, val name: String, val email: String, val password: String){

    companion object {

        fun fromRow(resultRow: ResultRow) = UserDataClass(
            id = resultRow[User.id],
            name = resultRow[User.name],
            email = resultRow[User.email],
            password = resultRow[User.password]
        )
    }
}

object User: Table() {
    val id = integer("id").autoIncrement() // Column<Int>
    val name = varchar("name", 50) // Column<String>
    val email = varchar("email", 50)
    val password = varchar("password", 50).default("")
    override val primaryKey = PrimaryKey(id)
}

object UserController{
    fun GetAll(ctx: Context){
        Database.connect("jdbc:sqlite:C:/Users/jacks/sqlite/ex1.db")
        val userList: MutableList<UserDataClass> = ArrayList<UserDataClass>()
        transaction{
            for(user in User.selectAll()){
                userList.add(UserDataClass.fromRow(user))
            }
        }
        ctx.json(userList)
    }
    fun FindByID(ctx: Context){
        val id = ctx.pathParam("userId").toInt()
        Database.connect("jdbc:sqlite:C:/Users/jacks/sqlite/ex1.db")
        transaction{
             ctx.json(User.select{User.id eq id}.map{ UserDataClass.fromRow(it) }[0])
        }
    }
    fun FindByEmail(email: String): UserDataClass {
        Database.connect("jdbc:sqlite:C:/Users/jacks/sqlite/ex1.db")
        return transaction{
            User.select{User.email eq email}.map{ UserDataClass.fromRow(it) }[0]
        }
    }
    fun FindByEmailAndPassword(email: String, password: String): UserDataClass? {
        Database.connect("jdbc:sqlite:C:/Users/jacks/sqlite/ex1.db")
        return transaction{
            User.select{(User.email eq email) and (User.password eq password)}.map{ UserDataClass.fromRow(it) }.firstOrNull()
        }
    }

    fun Save(input_name: String, input_email: String, input_password: String){
        return transaction{
            User.insert {
                it[name] = input_name
                it[email] = input_email
                it[password] = input_password
            }[User.id]
        }
    }
}
