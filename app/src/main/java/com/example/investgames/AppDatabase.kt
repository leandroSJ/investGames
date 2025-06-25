import android.content.Context
import androidx.room.*
import com.example.investgames.AporteDao
import com.example.investgames.AporteEntity
import com.example.investgames.User
import com.example.investgames.UserDao
import com.example.investgames.MetaDao
import com.example.investgames.data.MetaEntity


@Database(entities = [User::class, MetaEntity::class, AporteEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun metasDao(): MetaDao
    abstract fun aporteDao(): AporteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "investgames_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
