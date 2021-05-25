import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.*;
import akka.stream.javadsl.*;

import java.util.concurrent.CompletionStage;

public class ExploringConcurrency {

    public static void main(String[] args) {

        ActorSystem actorSystem = ActorSystem.create(Behaviors.empty(), "actorSystem");

        Source<Integer, NotUsed> source = Source.range(1,10);
        Flow<Integer, Integer, NotUsed> flow = Flow.of(Integer.class)
                .map( x -> {
                    System.out.println("Starting flow " + x);
                    try {
                        Thread.sleep(3000);
                    }
                    catch (Exception e) {
                    }
                    System.out.println("Ending flow " + x);
                    return x;
                });
        Sink<Integer, CompletionStage<Done>> sink = Sink.foreach (System.out::println);

        Long start = System.currentTimeMillis();

        RunnableGraph<CompletionStage<Done>> graph = RunnableGraph.fromGraph(
                GraphDSL.create(sink, (builder, out) -> {
                    SourceShape<Integer> sourceShape = builder.add(source);
                    //FlowShape<Integer, Integer> flowShape = builder.add(flow);

                    UniformFanOutShape<Integer, Integer> balance =
                            builder.add(Balance.create(4, true));
                    UniformFanInShape<Integer, Integer> merge =
                            builder.add(Merge.create(4));

                    builder.from(sourceShape)
                            .viaFanOut(balance);

                    for(int i = 0; i < 4; i++) {
                        builder.from(balance)
                                .via(builder.add(flow.async()))
                                .toFanIn(merge);
                    }

                    builder.from(merge)
                            .to(out);
                    return ClosedShape.getInstance();
                })
        );

        CompletionStage<Done> result = graph.run(actorSystem);

        result.whenComplete( (value, throwable) -> {
            Long end = System.currentTimeMillis();
            System.out.println("Time taken " + (end - start) + " ms.");
            actorSystem.terminate();
        });



    }
}
