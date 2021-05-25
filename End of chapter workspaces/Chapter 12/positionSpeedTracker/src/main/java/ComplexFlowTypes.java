import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.*;
import akka.stream.javadsl.*;

import java.util.concurrent.CompletionStage;

public class ComplexFlowTypes {
    public static void main(String[] args) {
        ActorSystem actorSystem = ActorSystem.create(Behaviors.empty(), "actorSystem");

        Source<Integer, NotUsed> source = Source.range(1,10);
        Flow<Integer, Integer, NotUsed> flow1 = Flow.of(Integer.class)
                .map (x -> {
                    System.out.println("Flow 1 is processing " + x);
                    return (x * 2);
                });
        Flow<Integer, Integer, NotUsed> flow2 = Flow.of(Integer.class)
                .map (x -> {
                    System.out.println("Flow 2 is processing " + x);
                    return (x + 1);
                });
        Sink<Integer, CompletionStage<Done>> sink = Sink.foreach(System.out::println);

        RunnableGraph<CompletionStage<Done>> graph = RunnableGraph.fromGraph(
                GraphDSL.create(sink, (builder, out) -> {
                    SourceShape<Integer> sourceShape = builder.add(source);
                    FlowShape<Integer, Integer> flow1Shape = builder.add(flow1);
                    FlowShape<Integer, Integer> flow2Shape = builder.add(flow2);
                    UniformFanOutShape<Integer, Integer> broadcast =
                            builder.add(Broadcast.create(2));
                    UniformFanInShape<Integer, Integer> merge =
                        builder.add(Merge.create(2));

//                    builder.from(sourceShape)
////                            .viaFanOut(broadcast);
////
////                    builder.from(broadcast.out(0))
////                            .via(flow1Shape);
////
////                    builder.from(broadcast.out(1))
////                            .via(flow2Shape);
////
////                    builder.from(flow1Shape)
////                            .toInlet(merge.in(0));
////
////                    builder.from(flow2Shape)
////                            .toInlet(merge.in(1));
////
////                    builder.from(merge).to(out);

                    builder.from(sourceShape)
                            .viaFanOut(broadcast)
                            .via(flow1Shape);

                    builder.from(broadcast).via(flow2Shape);

                    builder.from(flow1Shape)
                            .viaFanIn(merge)
                            .to(out);

                    builder.from(flow2Shape).viaFanIn(merge);


                    return ClosedShape.getInstance();
                } )
        );

        graph.run(actorSystem);
    }
}
