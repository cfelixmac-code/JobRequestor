package com.cfelixmac.jobrequester.job;

import com.cfelixmac.jobrequester.DemoApi;
import com.cfelixmac.req.requester.BaseJob;
import com.cfelixmac.req.requester.IBus;
import com.google.gson.JsonElement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.reactivex.Observable;

public class Job4 extends BaseJob<JsonElement> {

    private DemoApi api;

    public Job4(DemoApi api, @NotNull String identifier, @Nullable IBus bus, @NotNull Config config) {
        super(identifier, bus, config);
        this.api = api;
    }

    @NotNull
    @Override
    public Observable<JsonElement> buildRequestObservable() {
        return api.getSinglePost4();
    }
}
