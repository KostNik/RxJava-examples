package operators;

import com.google.common.collect.Lists;
import io.reactivex.Observable;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import sources.Sources;

import java.util.Arrays;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by Kostiuk Nikita
 */

@Slf4j
public class Combining {


    public static void main(String[] args) {

//        merge();

//        flatMap();

//        concat();

//        ambiguous();

//        zip();

//        combineLatest();

//        withLatestFrom();

        grouping();

    }

    private static void grouping() {
        Observable<String> source = Sources.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon");
        Observable<GroupedObservable<Integer, String>> byLengths = source.groupBy(String::length);
        byLengths.flatMapSingle(Observable::toList)
                .map(List::toString)
                .subscribe(log::info);
    }

    private static void withLatestFrom() {
        log.info(">>> With Latest From <<<");
        Observable<String> source = Sources.fromIterable(Lists.newArrayList("Alpha", "Beta", "Gamma", "Delta", "Epsilon"));
        Observable<Integer> source1 = Sources.fromIterable(Lists.newArrayList(1, 2, 3, 4));
        source.withLatestFrom(source1, (s, s1) -> s + "  -  " + s1).subscribe(log::info);
        source1.withLatestFrom(source, (s, s1) -> s + "  -  " + s1).subscribe(log::info);

        Observable<String> source2 = Sources.interval(1, SECONDS)
                .map(l -> l + 1) // emit elapsed seconds
                .map(l -> "Source2: " + l + " seconds");
        //emit every 300 milliseconds
        Observable<String> source3 = Sources.interval(300, MILLISECONDS)
                .map(l -> (l + 1) * 300) // emit elapsed milliseconds
                .map(l -> "Source3: " + l + " milliseconds");

        source2.withLatestFrom(source3, (s, s1) -> s + "  -  " + s1).subscribe(log::info);

        sleep(3100);


        log.info("-----------------");
    }

    private static void combineLatest() {
        log.info(">>> Combine latest <<<");
        Observable<String> source = Sources.fromIterable(Lists.newArrayList("Alpha", "Beta", "Gamma", "Delta", "Epsilon"));
        Observable<Integer> source1 = Sources.fromIterable(Lists.newArrayList(1, 2, 3, 4));
        Observable.combineLatest(source, source1, (s, s1) -> s + "  -  " + s1).subscribe(log::info);

        Observable<String> source2 = Sources.interval(1, SECONDS)
                .map(l -> l + 1) // emit elapsed seconds
                .map(l -> "Source2: " + l + " seconds");
        //emit every 300 milliseconds
        Observable<String> source3 = Sources.interval(300, MILLISECONDS)
                .map(l -> (l + 1) * 300) // emit elapsed milliseconds
                .map(l -> "Source3: " + l + " milliseconds");


        Observable.combineLatest(source3, source2, source, (s3, s2, s) -> String.join(" - ", s3, s2, s))
                .take(3, SECONDS)
//                .observeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
//                .subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
//                .doOnComplete(() -> System.exit(-1))
                .subscribe(log::info);

        sleep(3100);


        log.info("-----------------");
    }

    private static void zip() {
        log.info(">>> ZIP <<<");
        Observable<String> source = Observable.fromIterable(Lists.newArrayList("Alpha", "Beta", "Gamma", "Delta", "Epsilon"));
        Observable<Integer> source1 = Observable.fromIterable(Lists.newArrayList(1, 2, 3, 4));
        Observable.zip(source, source1, (s, s1) -> s + "  -  " + s1).subscribe(log::info);


        Observable<String> source2 = Sources.interval(1, SECONDS)
                .map(l -> l + 1) // emit elapsed seconds
                .map(l -> "Source2: " + l + " seconds");
        //emit every 300 milliseconds
        Observable<String> source3 = Sources.interval(300, MILLISECONDS)
                .map(l -> (l + 1) * 300) // emit elapsed milliseconds
                .map(l -> "Source3: " + l + " milliseconds");


        Observable.zip(source3, source2, source, (s3, s2, s) -> String.join(" - ", s3, s2, s))
//                .observeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
//                .subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
                .doOnComplete(() -> System.exit(-1))
                .subscribe(log::info);

        sleep(3100);


        log.info("-----------------");


    }

    private static void flatMap() {
        log.info(">>> FLAT MAP <<<");
        Observable<String> source = Sources.just("Alpha", "Beta", "Gamma");
        source.flatMap(s -> Sources.just(s.split(""))).subscribe(log::info);
        log.info("-----------------");

        source.flatMap(s -> Sources.just(s.split("")), (s, r) -> s + "-" + r)
                .subscribe(log::info);

        log.info(">>> MAP <<<");
        source.map(s -> s.split("")).map(Arrays::toString).subscribe(log::info);
        log.info("-----------------");
    }

    private static void merge() {
        log.info(">>> MERGE <<<");
        Observable<String> source1 = Sources.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon");
        Observable<String> source2 = Sources.just("Zeta", "Eta", "Theta");
//        Observable.merge(source1, source2).subscribe(i -> log.info("RECEIVED: " + i));
//        source1.mergeWith(source2).subscribe(i -> log.info("RECEIVED: " + i));
        Observable.mergeArray(source1, source2).subscribe(i -> log.info("RECEIVED: " + i));

        //emit every second
        Observable<String> source3 = Sources.interval(1, SECONDS)
                .take(3, SECONDS)
                .map(l -> l + 1) // emit elapsed seconds
                .map(l -> "Source3: " + l + " seconds");
        //emit every 300 milliseconds
        Observable<String> source4 = Sources.interval(300, MILLISECONDS)
                .take(3, SECONDS)
                .map(l -> (l + 1) * 300) // emit elapsed milliseconds
                .map(l -> "Source4: " + l + " milliseconds");
        //merge and subscribe
        Observable.merge(source3, source4).subscribe(log::info);
        //keep alive for 3 seconds
        sleep(3000);
        log.info("-----------------");
    }

    private static void concat() {
        log.info(">>> CONCAT <<<");
        //emit every second, but only take 2 emissions
        Observable source3 = Sources.interval(1, SECONDS)
                .take(2)
                .map(l -> l + 1) // emit elapsed seconds
                .map(l -> "Source1: " + l + " seconds");
        //emit every 300 milliseconds
        Observable source4 = Sources.interval(300, MILLISECONDS)
                .map(l -> (l + 1) * 300) // emit elapsed milliseconds
                .map(l -> "Source2: " + l + " milliseconds");
        Observable.concat(source3, source4).subscribe(i -> log.info("RECEIVED: " + i));
        //keep application alive for 3 seconds
        sleep(3000);
        log.info("-----------------");
    }

    private static void ambiguous() {
        log.info(">>> Ambiguous <<<");
        //emit every second
        Observable<String> source1 = Sources.interval(1, SECONDS)
//                .take(2)
                .map(l -> l + 1) // emit elapsed seconds
                .map(l -> "Source1: " + l + " seconds");
        //emit every 300 milliseconds
        Observable<String> source2 = Sources.interval(300, MILLISECONDS)
                .map(l -> (l + 1) * 300) // emit elapsed milliseconds
                .map(l -> "Source2: " + l + " milliseconds");
        //emit Observable that emits first
        Observable.amb(Arrays.asList(source1, source2))
                .flatMap(Observable::just)
                .subscribeOn(Schedulers.computation())
                .take(4, SECONDS)
                .subscribe(i -> log.info("RECEIVED: " + i));
        //keep application alive for 3 seconds
        sleep(3100);

        log.info("-----------------");
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
            log.info("WAKE UP AFTER {} millis", millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
