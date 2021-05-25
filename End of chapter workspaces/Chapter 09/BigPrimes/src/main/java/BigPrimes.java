import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.Attributes;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class BigPrimes {
    public static void main(String[] args) {
        Long start = System.currentTimeMillis();
        ActorSystem actorSystem = ActorSystem.create(Behaviors.empty(), "actorSystem");

        Source<Integer, NotUsed> source = Source.range(1,10);

        Flow<Integer, BigInteger, NotUsed> bigIntegerGenerator = Flow.of(Integer.class)
                .map( input -> {
                    BigInteger result = new BigInteger(3000, new Random());
                    System.out.println("Big integer : " + result);
                    return result;
                });

        Flow<BigInteger, BigInteger, NotUsed> primeGenerator = Flow.of(BigInteger.class)
                .map ( input -> {
                   BigInteger prime = input.nextProbablePrime();
                    System.out.println("Prime : " + prime);
                    return prime;
                });

        Flow<BigInteger, BigInteger, NotUsed> primeGeneratorAsync = Flow.of(BigInteger.class)
                .mapAsyncUnordered ( 4 , input -> {
                    CompletableFuture<BigInteger> futurePrime = new CompletableFuture<>();
                    futurePrime.completeAsync( () -> {
                        BigInteger prime = input.nextProbablePrime();
                        System.out.println("Prime : " + prime);
                        return prime;
                    } );
                    return futurePrime;
                });

        Flow<BigInteger, List<BigInteger>, NotUsed> createGroup = Flow.of(BigInteger.class)
                .grouped(10)
                .map ( list -> {
                    List<BigInteger> outputList = new ArrayList<>(list);
                    Collections.sort(outputList);
                    return outputList;
                });

        Sink<List<BigInteger>, CompletionStage<Done>> printSink = Sink.foreach(System.out::println);

        CompletionStage<Done> result = source
                .via(bigIntegerGenerator)
                //.buffer(16, OverflowStrategy.backpressure())
                .async()
                .via(primeGeneratorAsync)
                .async()
                .via(createGroup)
                .toMat(printSink, Keep.right())
                .run(actorSystem);

        result.whenComplete( (value, throwable) -> {
            Long end = System.currentTimeMillis();
            System.out.println("The application ran in " + (end - start) + " ms.");
            actorSystem.terminate();
        });
    }
}
