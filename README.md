# JobRequestor

JobRequestor Android Library - Manage your network requests by jobs conveniently.   

Support schedule requests parallelly or serially, you can also group requests and cache any request easily.  

More features will be introduced below.

### Features

* Send requests every where, manage result in one place (by use of, for example, RxBus).

  *you can have results returned in one place for whole project, or multiple places wherever you want.*

* Send requests serially (next request will be sent until the previous return).

  *especially useful for scenarios like 'like', 'collect', where users can click one button rapidly.*

* Send requests parallelly.

* Send requests by group.

* Cache. Support **offline-first** feature.

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

### License

### Dependencies

This library uses following projects as dependencies:  

* [android-priority-queue (by yigit)](https://github.com/yigit/android-priority-jobqueue)
* [ig-disk-cache (by Instagram)](https://github.com/Instagram/ig-disk-cache)

---

Issues are welcome.~