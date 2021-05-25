import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.ClosedShape;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

public class AdvancedBackpressure {

    public static void main(String[] args) {

        ActorSystem actorSystem = ActorSystem.create(Behaviors.empty(), "actorSystem");

        Source<Integer, NotUsed> source = Source.fromIterator(() -> Stream.iterate(1, i -> i + 1).iterator())
                .throttle(5, Duration.ofSeconds(1));

        Flow<Integer, List, NotUsed> conflateWithSeedFlow = Flow.of(Integer.class)
                .conflateWithSeed(
                        a -> {
                            List<Integer> list = new ArrayList<>();
                            list.add(a);
                            return list;
                        },
                        (list, a) -> {
                            list.add(a);
                            return list;
                        }
                );

        Flow<Integer, Integer, NotUsed> conflateFlow = Flow.of(Integer.class)
                .conflate( (accumulator, element)  -> {
                    return accumulator + element;
                }) ;

        Flow<Integer, String, NotUsed> flow = Flow.of(Integer.class).map(x -> {
            System.out.println("Flowing " + x);
            return x.toString();
        }).throttle(1, Duration.ofSeconds(1));

        Flow<String, String, NotUsed> extrapolateFlow = Flow.of(String.class)
                .extrapolate( x -> List.of(x).iterator() );

        Sink<String, CompletionStage<Done>> sink = Sink.foreach(x -> System.out.println("Sinking " + x));

        source.via(conflateFlow).via(flow).async().via(extrapolateFlow).to(sink).run(actorSystem);
        
    }
}
