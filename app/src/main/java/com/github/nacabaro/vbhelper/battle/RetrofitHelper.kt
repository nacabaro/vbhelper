package com.github.nacabaro.vbhelper.battle

import android.content.Context
import retrofit2.Retrofit
import android.widget.Toast
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.github.nacabaro.vbhelper.battle.BattleAuthContainer

class RetrofitHelper {
    
    /**
     * Creates an OkHttpClient with authentication interceptor for game endpoints.
     * Requires a non-null, non-empty token.
     */
    private fun createAuthenticatedClient(token: String): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(token))
            .addInterceptor(loggingInterceptor)
            .build()
    }
    
    /**
     * Gets the session token from AuthRepository for API calls.
     * Falls back to nacatech token if session token is not available (backward compatibility).
     */
    private fun getAuthToken(context: Context): String? {
        return try {
            val authContainer = BattleAuthContainer(context)
            runBlocking {
                // Prefer session token, fall back to nacatech token for backward compatibility
                val sessionToken = authContainer.authRepository.sessionToken.first()
                if (!sessionToken.isNullOrEmpty()) {
                    println("RetrofitHelper: Using sessionToken for API call")
                    sessionToken
                } else {
                    // Fallback to nacatech token (slower, but works)
                    val nacatechToken = authContainer.authRepository.authToken.first()
                    if (!nacatechToken.isNullOrEmpty()) {
                        println("RetrofitHelper: No sessionToken found, falling back to nacatechToken")
                    }
                    nacatechToken
                }
            }
        } catch (e: Exception) {
            println("RetrofitHelper: Error getting auth token: ${e.message}")
            null
        }
    }
    
    /**
     * Creates a Retrofit instance with authentication for game endpoints.
     */
    private fun createAuthenticatedRetrofit(context: Context): Retrofit? {
        val token = getAuthToken(context)
        if (token.isNullOrEmpty()) {
            println("RetrofitHelper: No auth token available")
            return null
        }
        
        val client = createAuthenticatedClient(token)
        return Retrofit.Builder()
            .baseUrl("http://battle.io-void.com:8080/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Handles HTTP error responses (401, 403, 429).
     * For 401/403, clears authentication state to trigger re-authentication.
     */
    private fun handleErrorResponse(context: Context, response: Response<*>, errorMessage: String) {
        when (response.code()) {
            401 -> {
                println("RetrofitHelper: Authentication failed (401) - token may be expired")
                clearAuthAndNotify(context, "Authentication failed. Please log in again.")
            }
            403 -> {
                println("RetrofitHelper: Access forbidden (403) - token may be expired or invalid")
                // 403 could mean expired token, so clear auth state to trigger re-authentication
                clearAuthAndNotify(context, "Session expired. Please log in again.")
            }
            429 -> {
                println("RetrofitHelper: Rate limit exceeded (429)")
                Toast.makeText(context, "Too many requests. Please wait a moment.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                println("RetrofitHelper: API error (${response.code()}): $errorMessage")
                Toast.makeText(context, "Request failed: ${response.code()}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Clears authentication state and shows a message.
     * This will trigger BattlesScreen to detect the auth state change and open the login page.
     */
    private fun clearAuthAndNotify(context: Context, message: String) {
        try {
            val authContainer = BattleAuthContainer(context)
            CoroutineScope(Dispatchers.IO).launch {
                authContainer.authRepository.logout()
                println("RetrofitHelper: Cleared authentication state due to expired/invalid token")
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            println("RetrofitHelper: Error clearing auth state: ${e.message}")
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    fun getOpponents(context: Context, stage: String, callback: (OpponentsDataModel) -> Unit) {
        //println("RetrofitHelper: Starting API call for stage: $stage")

        try {
            // Create an authenticated Retrofit instance
            val retrofit = createAuthenticatedRetrofit(context)
            if (retrofit == null) {
                println("RetrofitHelper: Cannot create authenticated Retrofit - no token available")
                Toast.makeText(context, "Authentication required. Please log in.", Toast.LENGTH_SHORT).show()
                return
            }

            // Create an ApiService instance from the Retrofit instance.
            val service: OpponentService = retrofit.create<OpponentService>(OpponentService::class.java)
            //println("RetrofitHelper: Service created")

            // Call the getopponents() method of the ApiService
            // to make an API request.
            val call: Call<OpponentsDataModel> = service.getopponents(stage)
            //println("RetrofitHelper: API call created, enqueueing...")

            // Use the enqueue() method of the Call object to
            // make an asynchronous API request.
            call.enqueue(object : Callback<OpponentsDataModel> {
                override fun onFailure(call: Call<OpponentsDataModel>, t: Throwable) {
                    println("RetrofitHelper: API call failed: ${t.message}")
                    t.printStackTrace()
                    Toast.makeText(context, "Request Fail", Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<OpponentsDataModel>, response: Response<OpponentsDataModel>) {
                    println("RetrofitHelper: API response received - Code: ${response.code()}")
                    println("RetrofitHelper: Response body: ${response.body()}")

                    if(response.isSuccessful){
                        //println("RetrofitHelper: Response successful, calling callback")
                        val opponentsList: OpponentsDataModel = response.body() as OpponentsDataModel
                        callback(opponentsList)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        println("RetrofitHelper: Response not successful - Error: $errorBody")
                        handleErrorResponse(context, response, errorBody ?: "Unknown error")
                    }
                }
            })
        } catch (e: Exception) {
            println("RetrofitHelper: Exception in getOpponents: ${e.message}")
            e.printStackTrace()
            Toast.makeText(context, "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /*
    fun getCombatWinner(context: Context, stage: String, callback: (CombatDataModel) -> Unit) {

        // Create a Retrofit instance with the base URL and
        // a GsonConverterFactory for parsing the response.
        val retrofit: Retrofit = Retrofit.Builder().baseUrl("http://battle.io-void.com:8080/").addConverterFactory(
            GsonConverterFactory.create()).build()

        // Create an ApiService instance from the Retrofit instance.
        val service: CombatService = retrofit.create<CombatService>(CombatService::class.java)

        // Call the getwinner() method of the ApiService
        // to make an API request.
        val call: Call<CombatDataModel> = service.getwinner(stage)

        // Use the enqueue() method of the Call object to
        // make an asynchronous API request.
        call.enqueue(object : Callback<CombatDataModel> {
            // This is an anonymous inner class that implements the Callback interface.

            override fun onFailure(call: Call<CombatDataModel>, t: Throwable) {
                // This method is called when the API request fails.
                Toast.makeText(context, "Request Fail", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<CombatDataModel>, response: Response<CombatDataModel>) {
                // This method is called when the API response is received successfully.

                if(response.isSuccessful){
                    // If the response is successful, parse the
                    // response body to a DataModel object.
                    val winner: CombatDataModel = response.body() as CombatDataModel

                    // Call the callback function with the DataModel
                    // object as a parameter.
                    callback(winner)
                }
            }
        })
    }

    fun getBattleWinner(context: Context, playerDigi: String, playerStage: Int, opponentDigi: String, opponentStage: Int, callback: (BattleDataModel) -> Unit) {

        // Create a Retrofit instance with the base URL and
        // a GsonConverterFactory for parsing the response.
        val retrofit: Retrofit = Retrofit.Builder().baseUrl("http://battle.io-void.com:8080/").addConverterFactory(
            GsonConverterFactory.create()).build()

        // Create an ApiService instance from the Retrofit instance.
        val service: BattleService = retrofit.create<BattleService>(BattleService::class.java)

        // Call the getwinner() method of the ApiService
        // to make an API request.
        val call: Call<BattleDataModel> = service.getwinner(playerDigi, playerStage, opponentDigi, opponentStage)

        // Use the enqueue() method of the Call object to
        // make an asynchronous API request.
        call.enqueue(object : Callback<BattleDataModel> {
            // This is an anonymous inner class that implements the Callback interface.

            override fun onFailure(call: Call<BattleDataModel>, t: Throwable) {
                // This method is called when the API request fails.
                Toast.makeText(context, "Request Fail", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<BattleDataModel>, response: Response<BattleDataModel>) {
                // This method is called when the API response is received successfully.

                if(response.isSuccessful){
                    // If the response is successful, parse the
                    // response body to a DataModel object.
                    val winner: BattleDataModel = response.body() as BattleDataModel

                    // Call the callback function with the DataModel
                    // object as a parameter.
                    callback(winner)
                }
            }
        })
    }
     */

    fun getPVPWinner(context: Context, apiStage: Int, playerID: Long, playerDigi: String, playerStage: Int, critBar: Int, opponentDigi: String, opponentStage: Int, callback: (PVPDataModel) -> Unit) {
        getPVPWinner(context, apiStage, playerID, playerDigi, playerStage, critBar, opponentDigi, opponentStage, null, callback)
    }

    fun getPVPWinner(context: Context, apiStage: Int, playerID: Long, playerDigi: String, playerStage: Int, critBar: Int, opponentDigi: String, opponentStage: Int, action: String?, callback: (PVPDataModel) -> Unit) {

        try {
            // Create an authenticated Retrofit instance
            val retrofit = createAuthenticatedRetrofit(context)
            if (retrofit == null) {
                println("RetrofitHelper: Cannot create authenticated Retrofit - no token available")
                Toast.makeText(context, "Authentication required. Please log in.", Toast.LENGTH_SHORT).show()
                return
            }

            // Create an ApiService instance from the Retrofit instance.
            val service: PVPService = retrofit.create<PVPService>(PVPService::class.java)

            // Call the getwinner() method of the ApiService
            // to make an API request.
            val call: Call<PVPDataModel> = service.getwinner(apiStage, playerID, playerDigi, playerStage, critBar, opponentDigi, opponentStage, action)

            // Use the enqueue() method of the Call object to
            // make an asynchronous API request.
            call.enqueue(object : Callback<PVPDataModel> {
                // This is an anonymous inner class that implements the Callback interface.

                override fun onFailure(call: Call<PVPDataModel>, t: Throwable) {
                    // This method is called when the API request fails.
                    println("RetrofitHelper: PVP API call failed: ${t.message}")
                    t.printStackTrace()
                    Toast.makeText(context, "Request Fail", Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<PVPDataModel>, response: Response<PVPDataModel>) {
                    // This method is called when the API response is received successfully.
                    println("RetrofitHelper: PVP API response received - Code: ${response.code()}")

                    if(response.isSuccessful){
                        // If the response is successful, parse the
                        // response body to a DataModel object.
                        val apiResults: PVPDataModel = response.body() as PVPDataModel

                        // Call the callback function with the DataModel
                        // object as a parameter.
                        callback(apiResults)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        println("RetrofitHelper: PVP API response not successful - Code: ${response.code()}, Error: $errorBody")
                        handleErrorResponse(context, response, errorBody ?: "Unknown error")
                    }
                }
            })
        } catch (e: Exception) {
            println("RetrofitHelper: Exception in getPVPWinner: ${e.message}")
            e.printStackTrace()
            Toast.makeText(context, "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun authenticate(context: Context, token: String, callback: (AuthenticateResponse) -> Unit) {
        //println("RetrofitHelper: Starting validate API call with token: $token")
        
        if (token.isEmpty()) {
            println("RetrofitHelper: ERROR - Token is empty!")
            Toast.makeText(context, "Authentication failed: Token is empty", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Add logging interceptor to see the actual HTTP request
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()
            
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("http://battle.io-void.com:8080/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: AuthService = retrofit.create<AuthService>(AuthService::class.java)
            val request = AuthenticateRequest(userToken = token)
            // Use login endpoint instead of validate to get sessionToken
            val call: Call<AuthenticateResponse> = service.login(request)

            call.enqueue(object : Callback<AuthenticateResponse> {
                override fun onFailure(call: Call<AuthenticateResponse>, t: Throwable) {
                    println("RetrofitHelper: Validate API call failed: ${t.message}")
                    t.printStackTrace()
                    Toast.makeText(context, "Authentication failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<AuthenticateResponse>, response: Response<AuthenticateResponse>) {

                    if (response.isSuccessful) {
                        val authResponse: AuthenticateResponse? = response.body()
                        if (authResponse != null) {
                            callback(authResponse)
                        } else {
                            println("RetrofitHelper: Validation failed: Invalid response body")
                            Toast.makeText(context, "Authentication failed: Invalid response", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        println("RetrofitHelper: Validate response not successful - Code: ${response.code()}, Error: $errorBody")
                        Toast.makeText(context, "Authentication failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        } catch (e: Exception) {
            println("RetrofitHelper: Exception in validate: ${e.message}")
            e.printStackTrace()
            Toast.makeText(context, "Authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}