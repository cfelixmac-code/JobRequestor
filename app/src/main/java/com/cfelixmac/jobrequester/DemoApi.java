package com.cfelixmac.jobrequester;

import com.google.gson.JsonElement;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface DemoApi {

    @GET("posts/1")
    Observable<JsonElement> getSinglePost();

    @GET("posts/2")
    Observable<JsonElement> getSinglePost2();

    @GET("posts/3")
    Observable<JsonElement> getSinglePost3();

    @GET("posts/4")
    Observable<JsonElement> getSinglePost4();
}
