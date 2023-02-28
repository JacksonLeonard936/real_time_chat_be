import io.javalin.http.Context
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Timestamp

data class MessageDataClass(val id: Int, val text: String, val sender_id: Int, val receiver_id: Int, val created_at: String){

    companion object {

        fun fromRow(resultRow: ResultRow) = MessageDataClass(
            id = resultRow[Message.id],
            text = resultRow[Message.text],
            sender_id = resultRow[Message.sender_id],
            receiver_id = resultRow[Message.receiver_id],
            created_at = resultRow[Message.created_at]
        )
    }
}
object Message: Table() {
    val id = integer("id").autoIncrement() // Column<Int>
    val text = varchar("text", 50) // Column<String>
    val sender_id = integer("sender_id")
    val receiver_id = integer("receiver_id")
    val created_at = varchar("created_at", 50)
    override val primaryKey = PrimaryKey(id)
}
/*
3 different user interactions with messages
conversation = sender_id and receiver_id pair
-Send
-View all conversations
-View specific conversation
 */
object MessageController{
    fun GetConversations(ctx: Context){
        val id = ctx.pathParam("userId").toInt()
        Database.connect("jdbc:sqlite:C:/Users/jacks/sqlite/ex1.db")
        transaction{
            ctx.json(Message.select{ (Message.sender_id eq id) or (Message.receiver_id eq id) }.groupBy(Message.sender_id, Message.receiver_id).distinct().map{ MessageDataClass.fromRow(it) })
        }
    }
    fun FindConversation(ctx: Context){
        val firstUserId = ctx.queryParam("firstUserId")!!.toInt()
        val secondUserId = ctx.queryParam("secondUserId")!!.toInt()
        Database.connect("jdbc:sqlite:C:/Users/jacks/sqlite/ex1.db")
        transaction{
            ctx.json(Message.select{ ((Message.sender_id eq firstUserId) and (Message.receiver_id eq secondUserId)) or ((Message.sender_id eq secondUserId) and (Message.receiver_id eq firstUserId))}.map{ MessageDataClass.fromRow(it) })
        }
    }

    fun Save(input_sender_id: Int, input_receiver_id: Int, input_text: String, input_created_at: String){
        return transaction{
            Message.insert {
                it[sender_id] = input_sender_id
                it[receiver_id] = input_receiver_id
                it[text] = input_text
                it[created_at] = input_created_at
            }
        }
    }
}