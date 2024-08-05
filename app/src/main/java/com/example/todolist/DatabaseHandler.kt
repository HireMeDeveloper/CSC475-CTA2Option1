import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.todolist.Todo

class DatabaseHandler(context: Context) : SQLiteOpenHelper(context, NAME, null, VERSION) {

    companion object {
        const val VERSION = 1
        const val NAME = "todoListDatabase"
        const val TODO_TABLE = "todo"
        const val ID = "id"
        const val TASK = "task"
        const val STATUS = "status"

        const val CREATE_TODO_TABLE = "CREATE TABLE IF NOT EXISTS $TODO_TABLE($ID INTEGER PRIMARY KEY AUTOINCREMENT, $TASK TEXT, $STATUS INTEGER)"
    }

    private lateinit var db: SQLiteDatabase

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TODO_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TODO_TABLE")
        onCreate(db)
    }

    fun openDatabase() {
        db = this.writableDatabase
    }

    fun insertTask(task: Todo) {
        val cv = ContentValues().apply {
            put(TASK, task.title)
            put(STATUS, if (task.isChecked) 1 else 0)
        }
        db.insert(TODO_TABLE, null, cv)
    }

    @SuppressLint("Range")
    fun getAllTasks(): List<Todo> {
        val taskList: MutableList<Todo> = ArrayList()
        var cur: Cursor? = null

        try {
            db.beginTransaction()
            cur = db.query(TODO_TABLE, null, null, null, null, null, null)
            if (cur != null && cur.moveToFirst()) {
                do {
                    val title = cur.getString(cur.getColumnIndex(TASK))
                    val isChecked = cur.getInt(cur.getColumnIndex(STATUS)) == 1
                    val task = Todo(title, isChecked)
                    taskList.add(task)
                } while (cur.moveToNext())
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("DatabaseHandler", "Error while trying to get tasks from database", e)
        } finally {
            db.endTransaction()
            cur?.close()
        }
        return taskList
    }

    fun updateStatus(id: Int, status: Int) {
        val cv = ContentValues().apply {
            put(STATUS, status)
        }
        db.update(TODO_TABLE, cv, "$ID=?", arrayOf(id.toString()))
    }

    fun updateTask(id: Int, title: String) {
        val cv = ContentValues().apply {
            put(TASK, title)
        }
        db.update(TODO_TABLE, cv, "$ID=?", arrayOf(id.toString()))
    }

    fun deleteTask(id: Int) {
        db.delete(TODO_TABLE, "$ID=?", arrayOf(id.toString()))
    }
}
