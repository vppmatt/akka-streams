import akka.Done;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.japi.tuple.Tuple3;
import akka.stream.*;
import akka.stream.javadsl.*;

import java.util.concurrent.CompletionStage;

public class FanInAndFanOut {
    public static void main(String[] args) {

        ActorSystem actorSystem = ActorSystem.create(Behaviors.empty(), "actorSystem");

        RunnableGraph<CompletionStage<Done>> graph = RunnableGraph.fromGraph(
                GraphDSL.create( Sink.foreach(System.out::println), (builder, out) -> {

                    SourceShape<Integer> source = builder.add(Source.range(1,10));
                    FlowShape<Integer, Integer> integerFlow = builder.add(
                            Flow.of(Integer.class).map (x -> {
                                System.out.println("Integer flow " + x);
                                return x;
                            })
                    );
                    FlowShape<Boolean, Boolean> booleanFlow = builder.add(
                            Flow.of(Boolean.class).map (x -> {
                                System.out.println("Boolean flow " + x);
                                return x;
                            })
                    );
                    FlowShape<String, String> stringFlow = builder.add(
                            Flow.of(String.class).map (x -> {
                                System.out.println("String flow " + x);
                                return x;
                            })
                    );

                    FanOutShape3<Integer, Integer, Boolean, String> fanOut = builder.add(
                            UnzipWith.create3( input -> new Tuple3<>(input, input % 2 == 0, "it's " + input))
                    );

                    FanInShape3<Integer, Boolean, String, String> fanIn = builder.add(
                            ZipWith.create3( (a,b,c) -> {
                               StringBuilder sb = new StringBuilder();
                               sb.append("The number was " + a);
                               sb.append(", which is " + (b ? "even" : "odd")  );
                               sb.append(", and teh string was " + c);
                               return sb.toString();
                            })
                    );

                    builder.from(source).toInlet(fanOut.in());
                    builder.from(fanOut.out0()).via(integerFlow).toInlet(fanIn.in0());
                    builder.from(fanOut.out1()).via(booleanFlow).toInlet(fanIn.in1());
                    builder.from(fanOut.out2()).via(stringFlow).toInlet(fanIn.in2());
                    builder.from(fanIn.out()).to(out);

                    return ClosedShape.getInstance();
                })
        );

        graph.run(actorSystem);

    }
}
