import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class ExploringFlows {
    public static void main(String[] args) {
        ActorSystem actorSystem = ActorSystem.create(Behaviors.empty(), "actorSystem");

        Source<Integer, NotUsed> numbers = Source.range(1, 200);
        Flow<Integer, Integer, NotUsed> filterFlow = Flow.of(Integer.class).filter ( value -> value % 17 == 0);
        Flow<Integer, Integer, NotUsed> mapConcatFlow = Flow.of(Integer.class)
                .mapConcat ( value -> {
                    List<Integer> results = List.of(value, value+1, value + 2);
                    return results;
                });
        Flow<Integer, Integer, NotUsed> groupedFlow = Flow.of(Integer.class)
                .grouped(3)
                .map(value -> {
                    List<Integer> newList = new ArrayList<>(value);
                    Collections.sort(newList, Collections.reverseOrder());
                    return newList;
                })
                .mapConcat(value -> value);

        //Flow<IntegerList, Integer, NotUsed> ungroupedFlow = Flow.of(IntegerList.class).mapConcat (value -> null);
        //Flow<List, Integer, NotUsed> ungroupedFlow = Flow.of(List.class).mapConcat( value -> value);

        Sink<Integer, CompletionStage<Done>> printSink = Sink.foreach(System.out::println);

        Flow<Integer,Integer, NotUsed> chainedFlow = filterFlow.via(mapConcatFlow);

        numbers.via(chainedFlow).via(groupedFlow).to(printSink).run(actorSystem);
    }
}
