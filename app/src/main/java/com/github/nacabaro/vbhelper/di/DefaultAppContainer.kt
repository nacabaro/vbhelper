import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.di.AppContainer
import com.github.nacabaro.vbhelper.source.CurrencyRepository
import com.github.nacabaro.vbhelper.source.DataStoreSecretsRepository
import com.github.nacabaro.vbhelper.source.SecretsSerializer
import com.github.nacabaro.vbhelper.source.proto.Secrets

private const val SECRETS_DATA_STORE_NAME = "secrets.pb"
private const val USER_PREFERENCES_NAME = "user_preferences"
val Context.secretsStore: DataStore<Secrets> by dataStore(
    fileName = SECRETS_DATA_STORE_NAME,
    serializer = SecretsSerializer
)

val Context.currencyStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val db: AppDatabase by lazy {
        Room.databaseBuilder(
            context = context,
            klass = AppDatabase::class.java,
            "internalDb"
        )
            .createFromAsset("items.db")
            .build()
    }

    override val dataStoreSecretsRepository = DataStoreSecretsRepository(context.secretsStore)

    override val currencyRepository = CurrencyRepository(context.currencyStore)
}

