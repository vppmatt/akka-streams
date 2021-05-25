import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import scala.math.BigInt;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletionStage;

public class BigPrimes {
    public static void main(String[] args) {
        ActorSystem actorSystem = ActorSystem.create(Behaviors.empty(), "actorSystem");

        Source<Integer, NotUsed> source = Source.range(1,10);
        //Source<Integer, NotUsed> source = Source.repeat(1).take(10);

        Flow<Integer, BigInteger, NotUsed> bigIntegerGenerator = Flow.of(Integer.class)
                .map( input -> new BigInteger(2000, new Random()));

        Flow<BigInteger, BigInteger, NotUsed> primeGenerator = Flow.of(BigInteger.class)
                .map ( input -> {
                   BigInteger prime = input.nextProbablePrime();
                    System.out.println("Prime : " + prime);
                    return prime;
                });

        Flow<BigInteger, List<BigInteger>, NotUsed> createGroup = Flow.of(BigInteger.class)
                .grouped(10)
                .map ( list -> {
                    List<BigInteger> outputList = new ArrayList<>(list);
                    Collections.sort(outputList);
                    return outputList;
                });

        Sink<List<BigInteger>, CompletionStage<Done>> printSink = Sink.foreach(System.out::println);

        source.via(bigIntegerGenerator).via(primeGenerator).via(createGroup).to(printSink).run(actorSystem);
    }
}
