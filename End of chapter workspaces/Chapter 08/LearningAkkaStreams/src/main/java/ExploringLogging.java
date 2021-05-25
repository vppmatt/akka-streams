import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.concurrent.CompletionStage;

public class ExploringLogging {
    public static void main(String[] args) {
        ActorSystem actorSystem = ActorSystem.create(Behaviors.empty(), "actorSystem");
        Source<Integer, NotUsed> source = Source.range(1,10);
        Flow<Integer, Integer, NotUsed> flow = Flow.of(Integer.class)
                .log("flow input")
                .map(x -> x * 2)
                .log("flow output");

        Flow<Integer, Integer, NotUsed> loggedFlow = Flow.of(Integer.class)
                .map(x -> {
                    actorSystem.log().debug("flow input " + x);
                    int y = x *2;
                    actorSystem.log().debug("flow output " + y);
                    return y;
                });


        Sink<Integer, CompletionStage<Done>> sink = Sink.foreach(System.out::println);

        source.via(loggedFlow).to(sink).run(actorSystem);
    }
}
