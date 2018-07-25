package com.cfelixmac.req.requester.cache

import android.content.Context
import com.cfelixmac.req.requester.cache.ig.IgDiskCache
import com.google.common.io.CharStreams
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
import java.util.concurrent.Callable

/**
 * @author cfelixmac at 2018.
 */
internal class DefaultCacheImpl(private val context: Context) : ICache {

    private var igDiskCache: IgDiskCache? = null

    init {
        Observable.fromCallable { igDiskCache = IgDiskCache(context.cacheDir) }
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe()
    }

    override fun has(identifier: String): Boolean {
        return igDiskCache!!.has(identifier)
    }


    override fun save(identifier: String, obj: Any?) {
        Observable.fromCallable {
            val outputStream = igDiskCache!!.edit(identifier)
            if (outputStream.isPresent) {
                try {
                    val content = GsonBuilder().create().toJson(obj)
                    writeFileToStream(outputStream.get(), content)
                    outputStream.get().commit()
                } finally {
                    outputStream.get().abortUnlessCommitted()
                }
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe()
    }

    override fun <T> read(identifier: String, objClass: Class<T>): Observable<T> {
        return Observable.fromCallable(Callable<T> {
            val inputStream = igDiskCache!!.get(identifier)
            if (inputStream.isPresent) {
                try {
                    val result = CharStreams.toString(InputStreamReader(inputStream.get(), Charsets.UTF_8))
                    return@Callable GsonBuilder().create().fromJson(result, objClass)
                } finally {
                    try {
                        inputStream.get().close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
            null
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    override fun delete(identifier: String) {
    }

    private fun writeFileToStream(outputStream: FileOutputStream, content: String) {
        try {
            val p = PrintWriter(outputStream)
            p.println(content)
            p.flush()
            p.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}