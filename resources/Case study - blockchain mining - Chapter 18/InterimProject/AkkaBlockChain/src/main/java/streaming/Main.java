package streaming;

import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.stream.javadsl.Source;
import blockchain.ManagerBehavior;
import blockchain.MiningSystemBehavior;
import model.Transaction;

import java.time.Duration;
import java.util.Random;

public class Main {

    private static int transId = -1;
    private static Random random = new Random();

    public static void main(String[] args) {

        ActorSystem<ManagerBehavior.Command> actorSystem =
                ActorSystem.create(MiningSystemBehavior.create(), "BlockChainMiner");

        Source<Transaction, NotUsed> transactionsSource = Source.repeat(1)
                .throttle(1, Duration.ofSeconds(1))
                .map ( x -> {
                    transId++;
                    System.out.println("Received transaction " + transId);
                    return new Transaction(transId, System.currentTimeMillis(),
                            random.nextInt(1000), random.nextDouble() * 100);
                });
    }
}
