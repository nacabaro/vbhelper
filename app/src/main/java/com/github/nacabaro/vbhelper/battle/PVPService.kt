package com.github.nacabaro.vbhelper.battle

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PVPService {
    @GET("api/pvp")
    // This method returns a Call object with a generic
    // type of DataModel, which represents
    // the data model for the response.
    fun getwinner(
        @Query("apiStage") apiStage: Int, 
        @Query("playerID") playerID: Long, 
        @Query("playerDigi") playerDigi: String, 
        @Query("playerStage") playerStage: Int, 
        @Query("critBar") critBar: Int, 
        @Query("opponentDigi") opponentDigi: String, 
        @Query("opponentStage") opponentStage: Int,
        @Query("action") action: String? = null  // Optional: "quit" or "rejoin"
    ): Call<PVPDataModel>
}