package operators;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;
import sources.Sources;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedList;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by Kostiuk Nikita
 */

@Slf4j
public class Operations {

    public static void main(String[] args) {
        Sources.just("One", "Two", "Three", "Four")
                .skip(2)
                .filter(s -> s.length() != 3)
                .take(3)
                .map(String::toUpperCase)
                .subscribe(log::info);

        Maybe<String> o = Sources.range(1, 100)
                .takeWhile(i -> i < 5)
                .flatMap(i -> Observable.just(i.toString()))
                .elementAt(2);

        o.subscribe(i -> log.info("RECEIVED: " + i));

        Sources.just("Alpha", "Beta", "Zeta", "Eta", "Gamma", "Delta")
                .startWithArray("---START---", "******")
                .skipLast(4)
                .repeat(2)
                .subscribe(i -> log.info("RECEIVED: " + i));

        Sources.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .filter(s -> s.contains("Z"))
                .switchIfEmpty(Observable.just("Zeta", "Eta", "Theta"))
                .sorted(Comparator.reverseOrder())
                .delay(1, SECONDS)
                .subscribe(i -> log.info("RECEIVED: " + i), e -> log.info("RECEIVED ERROR: " + e));

        sleep(4000);

        Sources.just(5, 3, 7, 10, 2, 14)
                .scan(0, (accumulator, next) -> accumulator + next)
                .subscribe(s -> log.info("Received: " + s));

        Sources.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .count()
                .subscribe(s -> log.info("Count: " + s));

        Sources.just("2016-01-01", "2016-05-02", "2016-09-12", "2016-04-03")
                .map(LocalDate::parse)
                .any(dt -> dt.getMonthValue() >= 6)
                .doOnSuccess(i -> log.info("Emitting: " + i))
                .subscribe(s -> log.info("Received any getMonthValue() >= 6 {}", s));

        Sources.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .toList(LinkedList::new)
//                .collect(LinkedList::new, LinkedList::add)
                .subscribe(s -> log.info("Received list: " + s));

        Sources.just(5, 2, 4, 0, 3, 2, 8)
                .doOnError(e -> log.info("Source failed!"))
                .doOnSubscribe(d -> log.info("Subscribing!"))
                .doOnDispose(() -> log.info("Disposing!"))
                .map(i -> 10 / i)
                .doOnError(e -> log.info("Division failed!"))
                .retry(1)
//                .onErrorResumeNext(Observable.empty())
                .subscribe(i ->log.info("RECEIVED: " + i),
                        e -> log.info("RECEIVED ERROR: " + e));


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

