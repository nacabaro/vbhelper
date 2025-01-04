import android.content.Context
import androidx.room.Room
import com.github.nacabaro.vbhelper.database.AppDatabase.AppDatabase
import com.github.nacabaro.vbhelper.di.AppContainer

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val db: AppDatabase by lazy {
        Room.databaseBuilder(
            context = context,
            AppDatabase::class.java,
            "internalDb"
        )
    }
}