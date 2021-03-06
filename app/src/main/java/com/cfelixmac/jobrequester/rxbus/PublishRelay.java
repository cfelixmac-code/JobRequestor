/**
 * Copyright 2016 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.cfelixmac.jobrequester.rxbus;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Relay that, once an {@link Observer} has subscribed, emits all subsequently observed items to the
 * subscriber.
 * <p>
 * <img width="640" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/S.PublishSubject.png" alt="">
 * <p>
 * Example usage:
 * <p>
 * <pre> {@code

PublishRelay<Object> relay = PublishRelay.create();
// observer1 will receive all events
relay.subscribe(observer1);
relay.accept("one");
relay.accept("two");
// observer2 will only receive "three"
relay.subscribe(observer2);
relay.accept("three");

} </pre>
 */
public final class PublishRelay<T> extends Relay<T> {

    /** An empty subscribers array to avoid allocating it all the time. */
    @SuppressWarnings("rawtypes")
    private static final PublishDisposable[] EMPTY = new PublishDisposable[0];

    /** The array of currently subscribed subscribers. */
    private final AtomicReference<PublishDisposable<T>[]> subscribers;

    /**
     * Constructs a PublishRelay.
     */
    public static <T> PublishRelay<T> create() {
        return new PublishRelay<T>();
    }

    /**
     * Constructs a PublishRelay.
     */
    @SuppressWarnings("unchecked")
    private PublishRelay() {
        subscribers = new AtomicReference<PublishDisposable<T>[]>(EMPTY);
    }


    @Override
    public void subscribeActual(Observer<? super T> t) {
        PublishDisposable<T> ps = new PublishDisposable<T>(t, this);
        t.onSubscribe(ps);
        add(ps);
        // if cancellation happened while a successful add, the remove() didn't work
        // so we need to do it again
        if (ps.isDisposed()) {
            remove(ps);
        }
    }

    /**
     * Adds the given subscriber to the subscribers array atomically.
     * @param ps the subscriber to add
     */
    private void add(PublishDisposable<T> ps) {
        for (; ; ) {
            PublishDisposable<T>[] a = subscribers.get();
            int n = a.length;
            @SuppressWarnings("unchecked")
            PublishDisposable<T>[] b = new PublishDisposable[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = ps;

            if (subscribers.compareAndSet(a, b)) {
                return;
            }
        }
    }

    /**
     * Atomically removes the given subscriber if it is subscribed to the subject.
     * @param ps the subject to remove
     */
    @SuppressWarnings("unchecked")
    void remove(PublishDisposable<T> ps) {
        for (; ; ) {
            PublishDisposable<T>[] a = subscribers.get();
            if (a == EMPTY) {
                return;
            }

            int n = a.length;
            int j = -1;
            for (int i = 0; i < n; i++) {
                if (a[i] == ps) {
                    j = i;
                    break;
                }
            }

            if (j < 0) {
                return;
            }

            PublishDisposable<T>[] b;

            if (n == 1) {
                b = EMPTY;
            } else {
                b = new PublishDisposable[n - 1];
                System.arraycopy(a, 0, b, 0, j);
                System.arraycopy(a, j + 1, b, j, n - j - 1);
            }
            if (subscribers.compareAndSet(a, b)) {
                return;
            }
        }
    }

    @Override
    public void accept(T value) {
        if (value == null)
            throw new NullPointerException("value == null");
        for (PublishDisposable<T> s : subscribers.get()) {
            s.onNext(value);
        }
    }

    @Override
    public boolean hasObservers() {
        return subscribers.get().length != 0;
    }

    /**
     * Wraps the actual subscriber, tracks its requests and makes cancellation
     * to remove itself from the current subscribers array.
     *
     * @param <T> the value type
     */
    static final class PublishDisposable<T> extends AtomicBoolean implements Disposable {

        private static final long serialVersionUID = 3562861878281475070L;
        /** The actual subscriber. */
        final Observer<? super T> actual;
        /** The subject state. */
        final PublishRelay<T> parent;

        /**
         * Constructs a PublishSubscriber, wraps the actual subscriber and the state.
         * @param actual the actual subscriber
         * @param parent the parent PublishProcessor
         */
        PublishDisposable(Observer<? super T> actual, PublishRelay<T> parent) {
            this.actual = actual;
            this.parent = parent;
        }

        void onNext(T t) {
            if (!get()) {
                actual.onNext(t);
            }
        }

        @Override
        public void dispose() {
            if (compareAndSet(false, true)) {
                parent.remove(this);
            }
        }

        @Override
        public boolean isDisposed() {
            return get();
        }
    }
}
