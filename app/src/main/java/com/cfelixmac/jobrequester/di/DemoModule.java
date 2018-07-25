package com.cfelixmac.jobrequester.di;

import com.cfelixmac.jobrequester.DemoApi;
import com.cfelixmac.jobrequester.rxbus.RxBus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class DemoModule {

    @Singleton
    @Provides
    RxBus provideRxBus() {
        return RxBus.get();
    }

    @Singleton
    @Provides
    DemoApi provideDemoApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("http://jsonplaceholder.typicode.com/")
                .build();
        return retrofit.create(DemoApi.class);
    }
}
