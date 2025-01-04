import android.content.Context
import androidx.room.Room
import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.di.AppContainer

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val db: AppDatabase by lazy {
        Room.databaseBuilder(
            context = context,
            klass = AppDatabase::class.java,
            "internalDb"
        ).build()
    }
}