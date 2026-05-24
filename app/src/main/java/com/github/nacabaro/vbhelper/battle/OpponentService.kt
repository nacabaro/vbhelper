package com.github.nacabaro.vbhelper.battle

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpponentService {
    @GET("api/opponents")
    // This method returns a Call object with a generic
    // type of DataModel, which represents
    // the data model for the response.
    fun getopponents(@Query("stage") stage: String): Call<OpponentsDataModel>
}