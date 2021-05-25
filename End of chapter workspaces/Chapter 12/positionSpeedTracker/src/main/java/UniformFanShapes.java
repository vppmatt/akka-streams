import akka.Done;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.*;
import akka.stream.javadsl.*;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CompletionStage;

public class UniformFanShapes {
    public static void main(String[] args) {

        ActorSystem actorSystem = ActorSystem.create(Behaviors.empty(), "actorSystem");

        RunnableGraph<CompletionStage<Done>> graph = RunnableGraph.fromGraph(
                GraphDSL.create(Sink.foreach(System.out::println) , (builder, out) -> {

                    SourceShape<Integer> sourceShape = builder.add(
                            Source.repeat(1).throttle(1, Duration.ofSeconds(1))
                            .map(x -> {
                                Random r = new Random();
                                return 1 + r.nextInt(10);
                            })
                    );

                    FlowShape<Integer, Integer> flowShape = builder.add(
                            Flow.of(Integer.class).map( x -> {
                                System.out.println("Flowing " + x);
                                return x;
                            })
                    );

                    UniformFanOutShape<Integer, Integer> partition = builder.add(
                            Partition.create(2, x -> ( x == 1 || x == 10) ? 0 : 1)
                    );

                    UniformFanInShape<Integer, Integer> merge = builder.add(Merge.create(2));

                    builder.from(sourceShape)
                            .viaFanOut(partition);

                    builder.from(partition.out(0))
                            .via(flowShape)
                            .viaFanIn(merge)
                            .to(out);

                    builder.from(partition.out(1))
                            .viaFanIn(merge);


                    return ClosedShape.getInstance();
                })
        );

        graph.run(actorSystem);
    }
}
