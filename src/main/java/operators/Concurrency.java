package operators;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import sources.Sources;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.time.LocalTime;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static operators.Combining.sleep;

/**
 * Created by Kostiuk Nikita
 */

@Slf4j
public class Concurrency extends Application {

    /*
        You can dispose the computation(), io(), newThread(), single(), and trampoline()
    Schedulers at any time by calling their shutdown() method or all of them by calling
    Schedulers.shutdown().

        You can also call their start()
    method, or Schedulers.start(), to reinitialize the Schedulers


        The subscribeOn() operator will suggest to the source Observable upstream which
    Scheduler to use and how to execute operations on one of its threads.

        Having multiple Observers to the same Observable with subscribeOn() will result in
    each one getting its own thread

        The subscribeOn() operator instructs the source Observable which Scheduler to emit
    emissions on. If subscribeOn() is the only concurrent operation in an Observable chain,
    the thread from that Scheduler will work the entire Observable chain, pushing emissions
    from the source all the way to the final Observer. The observeOn() operator, however,
    will intercept emissions at that point in the Observable chain and switch them to a
    different Scheduler going forward.


     The subscribeOn() placement isn't matter !!!
     The observeOn() placement is matter !!!


    * */

    public static void main(String[] args) {


//        computationTest();
//        newThreadTest();
//        ioTest();
//        singleTest();
//        executorServiceTest();
//        trampolineTest();

//        simpleObserveOnTest();
//        UIObserveOnTest();
//        parallelization();

        unsubscribeOn();
    }

    private static void unsubscribeOn() {
        log.info(">>> unsubscribe On <<<");
        Disposable d = Sources.interval(1, TimeUnit.SECONDS)
                .doOnDispose(() -> log.info("Disposing on thread " + Thread.currentThread().getName()))
                .unsubscribeOn(Schedulers.io())
                .subscribe(i -> log.info("Received " + i));
        sleep(3000);
        d.dispose();
        sleep(3000);

        log.info(">>>---------->)(<----------<<<");
    }

    private static void parallelization() {
        log.info(">>> Parallelization<<<");
        int coreCount = Runtime.getRuntime().availableProcessors();
        AtomicInteger assigner = new AtomicInteger(0);
        Observable.range(1, 10)
                .groupBy(i -> assigner.incrementAndGet() % coreCount)
                .flatMap(grp -> grp
                        .observeOn(Schedulers.computation())
                        .map(Concurrency::intenseCalculation)
                )
                .doFinally(() -> System.exit(-1))
                .subscribe(i -> log.info("Received " + i + " "
                        + LocalTime.now() + " on thread "
                        + Thread.currentThread().getName()));
        sleep(4000);


        log.info(">>>---------->)(<----------<<<");
    }

    private static void UIObserveOnTest() {
        log.info(">>> UI ObserveOn Test<<<");
        launch();
        log.info(">>>---------->)(<----------<<<");
    }


    private static void simpleObserveOnTest() {
        log.info(">>> Simple Observe On<<<");
        //Happens on IO Scheduler
        Sources.just("WHISKEY/27653/TANGO", "6555/BRAVO", "232352/5675675/FOXTROT")
                .subscribeOn(Schedulers.io())
                .flatMap(s -> Observable.fromArray(s.split("/")))
                .doOnNext(s -> log.info("Split out " + s + " on thread " + Thread.currentThread().getName()))
                //Happens on Computation Scheduler
                .observeOn(Schedulers.computation())
                .filter(s -> s.matches("[0-9]+"))
                .map(Integer::valueOf)
                .reduce((total, next) -> total + next)
                .doOnSuccess(i -> log.info("Calculated sum" + i + " on thread" + Thread.currentThread().getName()))
                //Switch back to IO Scheduler
                .observeOn(Schedulers.io())
                .map(Object::toString)
                .doOnSuccess(s -> log.info("Writing " + s
                        + " to file on thread "
                        + Thread.currentThread().getName()))
                .subscribe(s -> write(s, "E://output.txt"));
        sleep(1000);

        log.info(">>>---------->)(<----------<<<");
    }

    @SneakyThrows
    public static void write(String text, String path) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)))) {
            writer.append(text);
        }
    }

    // build a Scheduler off a standard Java ExecutorService
    private static void executorServiceTest() {
        log.info(">>> ExecutorService <<<");

        int numberOfThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        Scheduler scheduler = Schedulers.from(executor);
        Sources.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .subscribeOn(scheduler)
                .doFinally(executor::shutdown)
                .subscribe(log::info);

        log.info(">>>---------->)(<----------<<<");
    }

    private static void trampolineTest() {
        log.info(">>> Trampoline <<<");


        log.info(">>>---------->)(<----------<<<");
    }

    private static void singleTest() {
        log.info(">>> SINGLE <<<");


        log.info(">>>---------->)(<----------<<<");
    }

    private static void newThreadTest() {
        log.info(">>> New thread <<<");
        Observable.interval(1, TimeUnit.SECONDS, Schedulers.newThread())
                .subscribe(i -> log.info("Received " + i + " on thread " + Thread.currentThread().getName()));
        sleep(5000);

        log.info(">>>---------->)(<----------<<<");
    }

    //IO tasks such as reading and writing databases, web requests, and disk storage are less expensive on the CPU
    private static void ioTest() {
        log.info(">>> IO <<<");
        Observable.fromCallable(() -> getResponse("https://api.github.com/users/thomasnield/starred"))
                .subscribeOn(Schedulers.io())
//                .blockingSubscribe(log::info);
                .subscribe(log::info);
        sleep(5000);


        log.info(">>>---------->)(<----------<<<");
    }

    @SneakyThrows
    private static String getResponse(String path) {
        return new Scanner(new URL(path).openStream(), "UTF-8").useDelimiter("\\A").next();
    }

    //blockingSubscribe() can be used in place of subscribe() to stop and wait for onComplete()

    private static void computationTest() {
        log.info(">>> Computation <<<");
        Observable<Integer> lengths = Sources.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .subscribeOn(Schedulers.computation())
                .map(Concurrency::intenseCalculation)
                .map(String::length)
//                .publish()
//                .autoConnect(2);
                ;
        lengths.subscribe(i ->
                log.info("Received " + i + " on thread " + Thread.currentThread().getName()));
        lengths.subscribe(i ->
                log.info("Received " + i + " on thread " + Thread.currentThread().getName()));
        sleep(10000);


        log.info(">>>---------->)(<----------<<<");
    }

    public static <T> T intenseCalculation(T value) {
        sleep(ThreadLocalRandom.current().nextInt(3000));
        return value;
    }


    @Override
    public void start(Stage stage) throws Exception {
        VBox root = new VBox();
        ListView<String> listView = new ListView<>();
        Button refreshButton = new Button("REFRESH");

        JavaFxObservable.actionEventsOf(refreshButton)
                .observeOn(Schedulers.io())
                .flatMapSingle(a -> Sources.just(getResponse("https://goo.gl/S0xuOi")
                        .split("\\r?\\n"))
                        .toList())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(list -> listView.getItems().setAll(list));

        root.getChildren().addAll(listView, refreshButton);
        stage.setScene(new Scene(root));
        stage.show();

    }
}
