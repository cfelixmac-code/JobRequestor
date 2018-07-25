package com.cfelixmac.jobrequester;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.cfelixmac.jobrequester.di.DaggerDemoComponent;
import com.cfelixmac.jobrequester.job.Job1;
import com.cfelixmac.jobrequester.job.Job2;
import com.cfelixmac.jobrequester.job.Job3;
import com.cfelixmac.jobrequester.job.Job4;
import com.cfelixmac.jobrequester.rxbus.RxBus;
import com.cfelixmac.req.requester.BaseJob;
import com.cfelixmac.req.requester.MxJobManager;
import com.cfelixmac.req.requester.cache.CachePolicy;
import com.cfelixmac.req.requester.event.ResultEvent;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.cfelixmac.req.requester.MxJobManagerKt.TYPE_PARALLEL;
import static com.cfelixmac.req.requester.MxJobManagerKt.TYPE_SERIAL;

public class MainActivity extends AppCompatActivity {

    MxJobManager manager;

    @Inject
    RxBus globalBus;
    @Inject
    DemoApi api;

    @BindView(R.id.output_text)
    TextView outputTextView;

    Disposable disposable;

    StringBuilder sb = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        DaggerDemoComponent.create().inject(this);

        manager = MxJobManager.create(this, false);

        disposable = globalBus.toObservable(ResultEvent.class).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResultEvent>() {
                    @Override
                    public void accept(ResultEvent resultEvent) {
                        sb.append(resultEvent.isSuccess ?
                                (resultEvent.result != null ? getSimplifiedString(resultEvent.result.toString()) : "null") :
                                (resultEvent.e != null ? getSimplifiedString(resultEvent.e.getMessage()) : "null")).append("\n\n");
                        outputTextView.setText(sb);
                    }
                });
    }

    @OnClick({R.id.parallel_action, R.id.serial_action, R.id.two_two_group_action})
    public void clicks(View v) {
        sb.setLength(0);
        switch (v.getId()) {
            case R.id.parallel_action:
                sb.append("  =Parallel Requests, Random Return Order=  \n\n\n");
                manager.sendJob(new Job1(api, "parallel_1", globalBus));
                manager.sendJob(new Job2(api, "parallel_2", globalBus));
                manager.sendJob(new Job3(api, "parallel_3", globalBus));
                manager.sendJob(new Job4(api, "parallel_4", globalBus, new BaseJob.Config().type(TYPE_PARALLEL)));
                break;
            case R.id.serial_action:
                sb.append("  =Serial Requests,  Return Order: 4-3-2-1=  \n\n\n");
                manager.sendJob(new Job4(api, "serial_4", globalBus, new BaseJob.Config().type(TYPE_SERIAL)));
                manager.sendJob(new Job3(api, "serial_3", globalBus, TYPE_SERIAL));
                manager.sendJob(new Job2(api, "serial_2", globalBus, TYPE_SERIAL));
                manager.sendJob(new Job1(api, "serial_1", globalBus, TYPE_SERIAL));
                break;
            case R.id.two_two_group_action:
                sb.append("  =Group Requests,  Return Order: 1 before 2, 3 before 4=  \n\n\n");
                manager.sendJob(new Job1(api, "group_a_1", globalBus, TYPE_SERIAL, CachePolicy.NO_CACHE, null, "a"));
                manager.sendJob(new Job2(api, "group_a_2", globalBus, TYPE_SERIAL, CachePolicy.NO_CACHE, null, "a"));
                manager.sendJob(new Job3(api, "group_b_1", globalBus, TYPE_SERIAL, CachePolicy.NO_CACHE, null, "b"));
                manager.sendJob(new Job4(api, "group_b_2", globalBus,
                        new BaseJob.Config().type(TYPE_SERIAL).cachePolicy(CachePolicy.NO_CACHE).group("b")));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (disposable != null) {
            disposable.dispose();
        }
        manager.clear();
        super.onDestroy();
    }

    private String getSimplifiedString(String input) {
        if (input != null && input.length() > 50) {
            return input.substring(0, 50) + " ......";
        }
        return input;
    }
}
