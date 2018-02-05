package operators;

import io.reactivex.Observable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.subjects.*;
import lombok.extern.slf4j.Slf4j;
import sources.Sources;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static operators.Combining.sleep;

/**
 * Created by Kostiuk Nikita
 */

@Slf4j
public class Multicast {


    public static void main(String[] args) {


//        connect();

        autoConnect();

//        refCount();

//        replaying();

//        caching();

//        publishSubject();

//        serializingSubjects();

//        behaviorSubject();

//        replaySubject();

//        asyncSubject();

//        unicastSubject();


    }

    //it will buffer all the emissions it receives until an Observer subscribes to it, and then it will
    //release all these emissions to the Observer and clear its cache:
    private static void unicastSubject() {
        log.info(">>> UnicastSubject <<<");
        Subject<String> subject = UnicastSubject.create();
        Sources.interval(300, TimeUnit.MILLISECONDS)
                .map(l -> ((l + 1) * 300) + " milliseconds")
                .subscribe(subject);
        sleep(2000);
        subject.subscribe(s -> log.info("Observer 1: " + s));
        sleep(2000);

        log.info(">>>---------->)(<----------<<<");
    }

    //it will only push the last value it receives, followed by an onComplete() event:
    private static void asyncSubject() {
        log.info(">>> AsyncSubject <<<");
        Subject<String> subject = AsyncSubject.create();
        subject.subscribe(s -> log.info("Observer 1: " + s),
                Throwable::printStackTrace,
                () -> log.info("Observer 1 done!"));
        subject.onNext("Alpha");
        subject.onNext("Beta");
        subject.onNext("Gamma");
        subject.onComplete();
        subject.subscribe(s -> log.info("Observer 2: " + s),
                Throwable::printStackTrace,
                () -> log.info("Observer 2 done!"));

        log.info(">>>---------->)(<----------<<<");
    }

    // is similar to PublishSubject followed by a cache() operator
    private static void replaySubject() {
        log.info(">>> ReplaySubject <<<");
        Subject<String> subject = ReplaySubject.create();
        subject.subscribe(s -> log.info("Observer 1: " + s));
        subject.onNext("Alpha");
        subject.onNext("Beta");
        subject.onNext("Gamma");
        subject.onComplete();
        subject.subscribe(s -> log.info("Observer 2: " + s));
        log.info(">>>---------->)(<----------<<<");
    }


    //it will replay the last emitted item to each new Observer downstream
    private static void behaviorSubject() {
        log.info(">>> BehaviorSubject <<<");
        Subject<String> subject = BehaviorSubject.create();
        subject.subscribe(s -> log.info("Observer 1: " + s));
        subject.onNext("Alpha");
        subject.onNext("Beta");
        subject.onNext("Gamma");
        subject.subscribe(s -> log.info("Observer 2: " + s));
//        subject.onComplete();
        subject.onNext("Theta");

        for (int i = 0; i < 4; i++) {
            subject.onNext("number: " + i);
        }
        log.info(">>>---------->)(<----------<<<");
    }

    private static void serializingSubjects() {
        //For threadsafing
        Subject<String> subject =
                PublishSubject.<String>create().toSerialized();

    }

    private static void publishSubject() {
        log.info(">>> PublishSubject <<<");
        Subject<String> subject = PublishSubject.create();
        subject
//                .map(String::length)
//                .map(Long::toString)
                .subscribe(log::info);
        subject.onNext("Alpha");
        subject.onNext("Beta");
        subject.onNext("Gamma");
        subject.onComplete();


        Observable<String> source1 = Sources.interval(1, TimeUnit.SECONDS).map(l -> (l + 1) + " seconds");
        Observable<String> source2 = Sources.interval(300, TimeUnit.MILLISECONDS).map(l -> ((l + 1) * 300) + " milliseconds");
        Subject<String> subject1 = PublishSubject.create();
        subject1.subscribe(log::info);
        source1.subscribe(subject1);
        source2.subscribe(subject1);
        sleep(3000);

        log.info(">>>---------->)(<----------<<<");
    }

    private static void caching() {
        log.info(">>> CACHING <<<");
        Observable<String> cachedRollingTotals =
                Sources.just(6, 2, 5, 7, 1, 4, 9, 8, 3)
                        .scan(0, (total, next) -> total + next)
                        .map(i -> "" + i)
                        .cache();
        cachedRollingTotals.subscribe(log::info);

        Observable<Long> seconds = Sources.interval(300, TimeUnit.MILLISECONDS)
                .map(t -> (t + 1) * 300)
//                .cache()
                .cacheWithInitialCapacity(3); //just for optimize
        //Observer 1
        seconds.subscribe(l -> log.info("Observer 1: " + l));
        sleep(2000);
        //Observer 2
        seconds.subscribe(l -> log.info("Observer 2: " + l));
        sleep(1000);


        log.info(">>>---------->)(<----------<<<");
    }

    private static void replaying() {
        log.info(">>> REPLAYING <<<");
        Observable<Long> seconds = Sources.interval(300, TimeUnit.MILLISECONDS)
                .map(t -> (t + 1) * 300)
                .replay(3, 1, TimeUnit.SECONDS) //1.2..and time period
                .autoConnect();
        //Observer 1
        seconds.subscribe(l -> log.info("Observer 1: " + l));
        sleep(2000);
        //Observer 2
        seconds.subscribe(l -> log.info("Observer 2: " + l));
        sleep(2000);

        log.info(">>>---------->)(<----------<<<");
    }

    private static void refCount() {
        log.info(">>> PUBLISH AND REF_COUNT or SHARE <<<");
        Observable<Long> seconds =
                Sources.interval(1, TimeUnit.SECONDS)
                        .share();
//                        .publish()
//                        .refCount();
        //Observer 1
        seconds.take(5).subscribe(l -> log.info("Observer 1: " + l));
        sleep(3000);
        //Observer 2
        seconds.take(2).subscribe(l -> log.info("Observer 2: " + l));
        sleep(3000);
        //there should be no more Observers at this point
        //Observer 3
        seconds.subscribe(l -> log.info("Observer 3: " + l));
        sleep(3000);

        log.info(">>>---------->)(<----------<<<");
    }

    private static void autoConnect() {
        log.info(">>> PUBLISH AND AUTO_CONNECT <<<");
        Observable<Integer> threeRandoms = Sources.range(1, 3)
                .map(i -> randomInt())
                .publish()
                .autoConnect(2); // 0 - start firing immediately
        //Observer 1 - print each random integer
        threeRandoms.subscribe(i -> log.info("Observer 1: " + i));
        //Observer 2 - sum the random integers, then print
        threeRandoms.reduce(0, (total, next) -> total + next).subscribe(i -> log.info("Observer 2: " + i));
        //Observer 3 - receives nothing
        threeRandoms.subscribe(i -> log.info("Observer 3:" + i));

        log.info(">>>---------->)(<----------<<<");
    }

    private static void connect() {
        log.info(">>> PUBLISH AND CONNECT <<<");
        ConnectableObservable<Integer> threeInts = Sources.range(1, 3).publish();
        Observable<Integer> threeRandoms = threeInts.map(i -> randomInt());
        threeRandoms.subscribe(i -> log.info("Observer 1: " + i));
        threeRandoms.subscribe(i -> log.info("Observer 2: " + i));
        threeRandoms.reduce(0, (total, next) -> total + next).subscribe(i -> log.info("Observer 3 (reduce): " + i));
        threeInts.connect();
        log.info(">>>--------------------<<<");
    }

    public static int randomInt() {
        return ThreadLocalRandom.current().nextInt(100000);
    }

}
