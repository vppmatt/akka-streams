import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.JavaFlowSupport;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class JavaInterop {
    public static void main(String[] args) {
        ActorSystem actorSystem = ActorSystem.create(Behaviors.empty(), "actorSystem");

        Source<Integer, NotUsed> source = Source.range(1,10);
        Flow<Integer, String,NotUsed> flow = Flow.of(Integer.class).map (x ->
        {
            System.out.println("flowing " + x);
            return x.toString();
        });

        java.util.concurrent.Flow.Processor<Integer, String> exposedProcessor =
                JavaFlowSupport.Flow.toProcessor(flow).run(actorSystem);

        Sink<String, NotUsed> j9Sink = JavaFlowSupport.Sink.fromSubscriber( new J9Subscriber());

        source.via(flow).to(j9Sink).run(actorSystem);
    }
}
