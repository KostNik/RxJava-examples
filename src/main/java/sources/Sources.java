package sources;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kostiuk Nikita
 */

@Slf4j
public class Sources {

    public static Observable<Integer> range(int start, int count) {
        log.info(">>> Observable range was generated from: {}, to: {}", start, start + count);
        return Observable.range(start, count);
    }

    public static Observable<Integer> range() {
        return range(5, 15);
    }

    public static Observable<Long> interval(int period, TimeUnit unit) {
        log.info(">>> Observable interval was generated with period: {} {}", period, unit);
        return Observable.interval(period, unit);
    }

    public static <T> Observable<T> fromFuture(Future<T> future) {
        log.info(">>> Observable fromFuture was generated with future: {}", future);
        return Observable.fromFuture(future);
    }

    public static <T> Observable<T> defer(Callable<ObservableSource<T>> observableSource) {
        log.info(">>> Observable defer was generated from observable source: {}", observableSource);
        return Observable.defer(observableSource);
    }

    public static <T> Observable<T> fromCallable(Callable<T> callable) {
        log.info(">>> Observable defer was generated from callable: {}", callable);
        return Observable.fromCallable(callable);
    }

    public static <T> Observable<T> just(T... items) {
        log.info(">>> Observable just was generated from items: {}", Arrays.toString(items));
        return Observable.fromArray(items);
    }

    public static <T> Observable<T> fromIterable(Iterable<T> iterable) {
        log.info(">>> Observable fromIterable was generated from iterable: {}", iterable);
        return Observable.fromIterable(iterable);
    }


}
