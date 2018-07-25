package com.cfelixmac.jobrequester.di;

import com.cfelixmac.jobrequester.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = DemoModule.class)
public interface DemoComponent {

    void inject(MainActivity activity);
}
