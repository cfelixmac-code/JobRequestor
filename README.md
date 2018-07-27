# JobRequestor

Job Requestor Android Library - Manage your network requests by jobs conveniently.   

Support schedule requests parallelly or serially, you can also group requests and cache any request easily.  

**This library DOES NOT include any Network Request Library, it's a MANAGE/SCHEDULE library.**

You can use other library like Retrofit to do the detail request work.

### Features

* **Send requests every where, manage result in one place (by use of, for example, RxBus).**

  you can have results returned in one place for whole project, or multiple places wherever you want. For example, you can have a "Global Bus" to receive global requests' results in Application class and several "Activity Buses" for each individual activity.

* **Send requests serially (next request will be sent until the previous return).**

  especially useful for scenarios like 'like', 'collect', where users can click one button rapidly. By serial requests, you can get result in the same order as send order.

* **Send requests parallelly.**

  send parallel requests, the results' return order will be determined by network and server status.

* **Send requests by group.**

  group your requests by specifying group names. groups are executed parallelly, you can make jobs(requests) in same group sent serially at the same time. For example, there are 4 requests, R1, R2, R3, R4. R1 and R2 in one group, R3 and R4 in another group. In-group requests are configured as serial. The return result is R1 returns before R2, R3 returns before R4, but R1 and R3 which returns first is determined by network or server.

* **Cache. Support offline-first feature.**

  If you don't have any special needs, you can use the default implementation of cache in this library. Or, you can implement your own cache. CachePolicy can be used to config return behavior. For example, you can use `CachePolicy.UPDATE_CACHE` to enable an offline-first feature.

### Download

`implementation 'com.cfelixmac.req.requester:jobrequestor:1.0.0'`

this library uses android support library v25, but only few of it. If you got an "...must use the exact same version specification..." error/warning, you can add the following code in your app's gradle file to force to use same version :

```groovy
configurations.all {
    resolutionStrategy.eachDependency { DependencyResolveDetails details ->
        def requested = details.requested
        if (requested.group == 'com.android.support') {
            if (!requested.name.startsWith("multidex")) {
                details.useVersion '27.1.1' // change version here.
            }
        }
    }
}
```

###Usage

##### Create Job Manager

Create an instance of `ReqJobManager` by calling `ReqJobManager.create(..)`

* If you don't need cache, just call `ReqJobManager.create(context)`
* If you want to use default cache, call `ReqJobManager.create(context, true)`
* If you want to use your own cache, call ``ReqJobManager.create(context, true, impl)`

Your own cache implementation needs to implement interface `ICache`

`ReqJobManager` is recommended to be singleton in one application.

##### Create Bus(Result Receiver)

implement `IBus` to get a bus as result receiver, implantation should be determined by user. 

Or, you can use `RxBus` in app module as well.

there can be several `IBus` instances. 

For global requests, you can have a singleton instance to receive.

For local request, like request current list's data, you can have a instance created in current activity.

##### Create Jobs(requests)

A job represents a requests. Extend `BaseJob` to create a job.

For Kotlin,  using constructor is simpler

For Java, using `BaseJob.Config` in `BaseJob(id, bus, config, callback)` may be better.

Here are details about each parameters in constructor.  

| Parameter   | Description                                                  |
| ----------- | ------------------------------------------------------------ |
| identifier  | unique id, shouldn't be same as any other job                |
| bus         | implementation of `IBus`, results receiver                   |
| type        | `TYPE_SERIAL` or `TYPE_PARALLEL`                             |
| cachePolicy | `CachePolicy.NO_CACHE`, `ALWAYS_CACHE`, `UPDATE_CACHE`,`ONLY_UPDATE` |
| callback    | result callback, if not null, result will be returned by it. Bus receiver still works normally |
| group       | group name. Jobs in same group should have same group name   |
| priority    | higher priority can be executed sooner, only available when type is `TYPE_PARALLEL ` |

Inside your Job class, implement the request detail, like set parameters and execute REST request by other network library (Retrofit or any other lib you like~)

##### Send jobs(execute)

If you don't use cache, just `reqJobManager.sendJob(job)`

If you use cache, another parameter represents return data type is needed:

``reqJobManager.sendJob(job, MyData.class)`

##### Receive Results

Using `RxBus` as an example:

```java
bus.toObservable(ResultEvent.class).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResultEvent>() {
                    @Override
                    public void accept(ResultEvent resultEvent) {
                        switch (resultEvent.identifier){ 
                            // use identifier distinguish different request
                            // ...... .......
                        }
                    }
                });
```

If you want to get cache fail result, you can add the following codes:

```java
bus.toObservable(CacheFailEvent.class)
   .subscribe(......);
```

### Demo



### License

[MIT](https://opensource.org/licenses/MIT)

### Dependencies

This library uses following projects as dependencies:  

* [android-priority-queue (by yigit)](https://github.com/yigit/android-priority-jobqueue)
* [ig-disk-cache (by Instagram)](https://github.com/Instagram/ig-disk-cache)

---

Issues are welcome.~