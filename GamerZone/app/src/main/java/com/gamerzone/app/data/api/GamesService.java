package com.gamerzone.app.data.api;

import com.gamerzone.app.model.Game;
import com.gamerzone.app.model.Result;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GamesService {
    @GET("games")
    Call<Result> searchGames(@Query("key") String key, @Query("search") String search,
                             @Query("page_size") String pageSize);
}
