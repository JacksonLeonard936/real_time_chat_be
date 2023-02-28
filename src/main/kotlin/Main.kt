import io.javalin.Javalin
import io.javalin.core.util.Header
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Cities : Table() {
    val id = integer("id").autoIncrement() // Column<Int>
    val name = varchar("name", 50) // Column<String>

    override val primaryKey = PrimaryKey(id, name = "PK_Cities_ID")
}

fun main() {
    val app = Javalin.create().apply {
        before { ctx ->
            ctx.header(Header.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
        }
    }.start(7070)

    app.get("/") { ctx ->
        Database.connect("jdbc:sqlite:C:/Users/jacks/sqlite/ex1.db")
        transaction {
            SchemaUtils.createMissingTablesAndColumns(User)
            SchemaUtils.createMissingTablesAndColumns(AuthToken)
            SchemaUtils.createMissingTablesAndColumns(Message)
        }
        ctx.result("Hello World")
    }
    app.get("/api/users", UserController::GetAll)
    app.get("/api/users/{userId}", UserController::FindByID)
    app.get("/api/users/email/{userEmail}") {ctx ->
        UserController.FindByEmail(
            ctx.pathParam("userEmail")
        )?.let {
            ctx.json(
                it
        )}
    }
    app.post("/api/users") {ctx ->
        Database.connect("jdbc:sqlite:C:/Users/jacks/sqlite/ex1.db")
        UserController.Save(ctx.formParam("name")!!, ctx.formParam("email")!!, ctx.formParam("password")!!)
            ?.let {
                ctx.json(
                    it
                )}
    }
    app.get("/api/messages/{userId}", MessageController::GetConversations)
    app.post("/api/messages") {ctx ->
        Database.connect("jdbc:sqlite:C:/Users/jacks/sqlite/ex1.db")
        MessageController.Save(ctx.formParam("sender_id")!!.toInt(), ctx.formParam("receiver_id")!!.toInt(), (ctx.formParam("text")!!), (ctx.formParam("created_at")!!))
            ?.let {
                ctx.json(
                    it
                )}
    }
    app.get("/api/messages", MessageController::FindConversation)
    app.post("/api/login") {ctx ->
        Database.connect("jdbc:sqlite:C:/Users/jacks/sqlite/ex1.db")
        val email = ctx.formParam("email")
        val password = ctx.formParam("password")
        val user = UserController.FindByEmailAndPassword(email!!, password!!)
        if(user != null){
            AuthTokenController.CreateByUserID(user.id)
                ?.let {
                    ctx.json(
                        it
                    )}
        }
    }
    app.get("/api/authtokens", AuthTokenController::GetAll)
}


