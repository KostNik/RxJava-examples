package operators;


import io.reactivex.Observable;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import sources.Sources;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static operators.Combining.sleep;

/**
 * Created by Kostiuk Nikita
 */

@Slf4j
public class STWB extends Application {

    public static void main(String[] args) {

        /**The buffer() operator will gather emissions within a certain scope and emit each batch as
         //a list or another collection type*/
//        fixedSizeBuffering();

        //You can use buffer() at fixed time intervals by providing a long and TimeUnit.
//        timeBasedBuffering();

        //the arbitrary occurrence of emissions of another Observable will determine
        //when to "slice" each buffer.
//        boundaryBasedBuffering();

        /**The window() operators are almost identical to buffer(), except that they buffer into
         other Observables rather than collections. This results in an
         Observable<Observable<T>>*/
//        fixedSizeWindowing();

        //As you might be able to guess, you can cut-off windowed Observables at time intervals just
        //like buffer()
        timeBasedWindowing();

        //you can also use another Observable as boundary.
//        boundaryBasedWindowing();

        /**The buffer() and window() operators batch up emissions into collections or Observables
         based on a defined scope, which regularly consolidates rather than omits
         emissions.The throttle() operator, however, omits emissions when they occur rapidly.
         */
        //Both throttleFirst() and throttleLast() emit on the computation Scheduler !!!
        //The throttleLast() operator (which is aliased as sample()) will only emit the last item
        //at a fixed time interval.
//        throttleLast_Sample();

        //The throttleFirst() operates almost identically to throttleLast(), but it will emit the
        //first item that occurs at every fixed time interval
//        throttleFirst();

        //throttleWithTimout() (also called debounce()) accepts time interval arguments that
        //specify how long a period of inactivity (which means no emissions are coming from the
        // source) must be before the last emission can be pushed forward.
//        throttleWithTimeout_Debounce();

        /**In RxJava, there is a powerful operator called switchMap(). Its usage feels like flatMap(),
         but it has one important behavioral difference: it will emit from the latest Observable
         derived from the latest emission and dispose of any previous Observables that were
         processing*/
//        switchingOne();

//        switchFX();
    }

    private static void switchFX() {
        launch("");
    }

    private static void switchingOne() {
        log.info(">>> SwitchingOne <<<");
        Observable<String> items = Sources.just("Alpha", "Beta",
                "Gamma", "Delta", "Epsilon",
                "Zeta", "Eta", "Theta", "Iota");
        //delay each String to emulate an intense calculation
        Observable<String> processStrings = items.concatMap(s ->
                Observable.just(s)
                        .delay(randomSleepTime(), TimeUnit.MILLISECONDS));
        //run processStrings every 5 seconds, and kill each
        //previous instance to start next
        Sources.interval(5, TimeUnit.SECONDS)
                .switchMap(i -> processStrings
                        .doOnDispose(() -> log.info("Disposing! Starting next")))
                .subscribe(log::info);
        //keep application alive for 20 seconds
        sleep(20000);


        log.info(">>>---------->)(<----------<<<");
    }

    public static int randomSleepTime() {
        //returns random sleep time between 0 to 2000 milliseconds
        int sleep = ThreadLocalRandom.current().nextInt(2000);
        log.info("SLEEP {}", sleep);
        return sleep;
    }


    private static void throttleWithTimeout_Debounce() {
        log.info(">>> Throttle With Timeout_Debounce <<<");
        Observable<String> source1 = Sources.interval(100,
                TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 100) // map to elapsed time
                .map(i -> "SOURCE 1: " + i)
                .take(10);

        Observable<String> source2 = Sources.interval(300,
                TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 300) // map to elapsed time
                .map(i -> "SOURCE 2: " + i)
                .take(3);

        Observable<String> source3 = Sources.interval(2000,
                TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 2000) // map to elapsed time
                .map(i -> "SOURCE 3: " + i)
                .take(2);

        Observable.concat(source1, source2, source3)
                .throttleWithTimeout(1, TimeUnit.SECONDS)
//                .throttleWithTimeout(2, TimeUnit.SECONDS)
//                .debounce(2, TimeUnit.SECONDS) //it is the same as throttleWithTimeout(...)
//                .throttleWithTimeout(100, TimeUnit.MILLISECONDS)
                .subscribe(log::info);

        sleep(6000);

        log.info(">>>---------->)(<----------<<<");
    }

    private static void throttleFirst() {
        log.info(">>> Throttle First <<<");
        Observable<String> source1 = Sources.interval(100,
                TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 100) // map to elapsed time
                .map(i -> "SOURCE 1: " + i)
                .take(10);

        Observable<String> source2 = Sources.interval(300,
                TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 300) // map to elapsed time
                .map(i -> "SOURCE 2: " + i)
                .take(5);

        Observable<String> source3 = Sources.interval(2000,
                TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 2000) // map to elapsed time
                .map(i -> "SOURCE 3: " + i)
                .take(2);

        Observable.concat(source1, source2, source3)
                .throttleFirst(1, TimeUnit.SECONDS)
//                .throttleFirst(2, TimeUnit.SECONDS)
//                .throttleFirst(1500, TimeUnit.MILLISECONDS)
//                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(log::info);

        sleep(6000);

        log.info(">>>---------->)(<----------<<<");
    }

    private static void throttleLast_Sample() {
        log.info(">>> Throttle Last_Sample <<<");
        Observable<String> source1 = Sources.interval(100,
                TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 100) // map to elapsed time
                .map(i -> "SOURCE 1: " + i)
                .take(10);

        Observable<String> source2 = Sources.interval(300,
                TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 300) // map to elapsed time
                .map(i -> "SOURCE 2: " + i)
                .take(3);

        Observable<String> source3 = Sources.interval(2000,
                TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 2000) // map to elapsed time
                .map(i -> "SOURCE 3: " + i)
                .take(2);

        Observable.concat(source1, source2, source3)
//                .throttleLast(1, TimeUnit.SECONDS)
//                .throttleLast(2, TimeUnit.SECONDS)
                .sample(2, TimeUnit.SECONDS) //it is the same as throttleLast(...)
//                .throttleLast(500, TimeUnit.MILLISECONDS)
                .subscribe(log::info);

        sleep(6000);

        log.info(">>>---------->)(<----------<<<");
    }

    private static void boundaryBasedWindowing() {
        log.info(">>> boundary Based Windowing <<<");
        Observable<Long> cutOffs =
                Sources.interval(1, TimeUnit.SECONDS);
        Sources.interval(300, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 300) // map to elapsed time
                .window(cutOffs)
                .flatMapSingle(obs -> obs.reduce("", (total, next) -> total + (total.equals("") ? "" : "|") + next))
                .subscribe(log::info);
        sleep(5000);


        log.info(">>>---------->)(<----------<<<");
    }

    private static void timeBasedWindowing() {
        log.info(">>> Time Based Windowing <<<");
        Sources.interval(300, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 300) // map to elapsed time
                .window(1, TimeUnit.SECONDS)
                .take(5000, TimeUnit.MILLISECONDS)
                .flatMapSingle(obs -> obs.reduce("", (total, next) -> total + (total.equals("") ? "" : "|") + next))
                .subscribe(log::info);
        sleep(5000);
        log.info(">>>---------->)(<----------<<<");
    }

    private static void fixedSizeWindowing() {
        log.info(">>> Fixed Size Windowing <<<");
        Sources.range(1, 50)
                .window(8, 1)
                .flatMapSingle(obs -> obs.reduce("", (total, next) -> total + (total.equals("") ? "" : "|") + next))
                .subscribe(log::info);
        log.info(">>>---------->)(<----------<<<");
    }


    private static void boundaryBasedBuffering() {
        log.info(">>> Boundary-based Buffering <<<");
        Observable<Long> cutOffs =
                Sources.interval(1, TimeUnit.SECONDS);

        Sources.interval(300, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 300) // map to elapsed time
                .buffer(cutOffs)
                .map(List::toString)
                .subscribe(log::info);
        sleep(5000);


        log.info(">>>---------->)(<----------<<<");
    }

    private static void fixedSizeBuffering() {
        log.info(">>> Fixed-size Buffering <<<");
        Sources.range(1, 50)
                .buffer(8)
                .map(List::toString) //just for Slf4j log
                .subscribe(log::info);

        Sources.range(1, 50)
                .buffer(8, HashSet::new)
                .map(HashSet::toString)
                .subscribe(log::info);

        Sources.range(1, 10)
                .buffer(2, 3)
                .map(List::toString)
                .subscribe(log::info);

        // skip less than count
        Sources.range(1, 10)
                .buffer(3, 1)
                .map(List::toString)
                .subscribe(log::info);

        Sources.range(1, 10)
                .buffer(2, 1)
                .filter(c -> c.size() == 2)
                .map(List::toString)
                .subscribe(log::info);


        log.info(">>>---------->)(<----------<<<");
    }

    private static void timeBasedBuffering() {
        log.info(">>> Time-based Buffering <<<");
        Sources.interval(300, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 300) // map to elapsed time
                .buffer(1, TimeUnit.SECONDS, 2)
                .map(List::toString)
                .subscribe(log::info);
        sleep(4000);


        log.info(">>>---------->)(<----------<<<");
    }

    @Override
    public void start(Stage stage) throws Exception {
//        toggleTiming(stage);
        groupingKeystrokes(stage);
    }

    private void groupingKeystrokes(Stage stage) {
        VBox root = new VBox();
        root.setMinSize(200, 300);
        Label typedTextLabel = new Label("");
        root.getChildren().addAll(typedTextLabel);
        Scene scene = new Scene(root);
        // Multicast typed keys
        Observable<String> typedLetters = JavaFxObservable.eventsOf(scene, KeyEvent.KEY_TYPED)
                .map(KeyEvent::getCharacter)
                .share();
        // Signal 300 milliseconds of inactivity
        Observable<String> restSignal = typedLetters
                .throttleWithTimeout(500, TimeUnit.MILLISECONDS).startWith(""); //trigger initial
        // switchMap() each period of inactivity to
        // an infinite scan() concatenating typed letters
        restSignal.switchMap(s -> typedLetters.scan("", (rolling, next) -> rolling + next))
                .observeOn(JavaFxScheduler.platform())
                .subscribe(s -> {
                    typedTextLabel.setText(s);
                    System.out.println(s);
                });
        stage.setScene(scene);
        stage.show();
    }

    private void toggleTiming(Stage stage) {
        VBox root = new VBox();
        Label counterLabel = new Label("");
        ToggleButton startStopButton = new ToggleButton();
        // Multicast the ToggleButton's true/false selected state
        Observable<Boolean> selectedStates =
                JavaFxObservable.valuesOf(startStopButton.selectedProperty())
                        .publish()
                        .autoConnect(2);
        // Using switchMap() with ToggleButton's selected state will drive
        // whether to kick off an Observable.interval(),
        // or dispose() it by switching to empty Observable
        selectedStates.switchMap(selected -> {
            if (selected) {
                return Observable.interval(1, TimeUnit.MILLISECONDS);
            } else {
                return Observable.empty();
            }
        }).observeOn(JavaFxScheduler.platform()) // Observe
                //on JavaFX UI thread
                .map(Object::toString)
                .subscribe(counterLabel::setText);
        // Change ToggleButton's text depending on its state
        selectedStates.subscribe(selected -> startStopButton.setText(selected ? "STOP" : "START"));
        root.getChildren().addAll(counterLabel, startStopButton);
        stage.setScene(new Scene(root));
        stage.show();
    }


}
