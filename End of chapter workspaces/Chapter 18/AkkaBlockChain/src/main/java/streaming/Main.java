package streaming;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.stream.*;
import akka.stream.javadsl.*;
import akka.stream.typed.javadsl.ActorFlow;
import blockchain.ManagerBehavior;
import blockchain.MiningSystemBehavior;
import model.Block;
import model.BlockChain;
import model.HashResult;
import model.Transaction;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletionStage;

public class Main {

    private static int transId = -1;
    private static Random random = new Random();

    public static void main(String[] args) {

        BlockChain blockChain = new BlockChain();

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

        Flow<Transaction, Block, NotUsed> blockBuilder = Flow.of(Transaction.class).map (trans -> {
            List<Transaction> list = new ArrayList<>();
            list.add(trans);
            Block block = new Block(blockChain.getLastHash(), list);
            System.out.println("Created block " + block);
            return block;
        }).conflate( (block1, block2) -> {
            block1.addTransactionToList(block2.getFirstTransaction());
            System.out.println("Conflated block " + block1);
            return block1;
        });

        Flow<Block, HashResult, NotUsed> miningProcess = ActorFlow
                .ask (actorSystem, Duration.ofSeconds(30), (block, self) ->
                     new ManagerBehavior.MineBlockCommand(block, self, 5)
                );

        Source<String, NotUsed> firstHashValue = Source.single("0");

        Flow<Block, Block, NotUsed> miningFlow = Flow.fromGraph(
                GraphDSL.create(builder -> {

                    UniformFanInShape<String, String> receiveHashes = builder.add(Merge.create(2));
                    FanInShape2<String, Block, Block> applyLastHashToBlock = builder.add(
                            ZipWith.create( (hash, block) -> {
                                return new Block (hash, block.extractTransactions());
                            })
                    );

                    UniformFanOutShape<Block, Block> broadcast = builder.add(Broadcast.create(2));
                    FlowShape<Block, HashResult> mineBlock = builder.add(miningProcess);

                    UniformFanOutShape<HashResult, HashResult> duplicateHashResult = builder.add(Broadcast.create(2));

                    FanInShape2<Block, HashResult, Block> receiveHashResult =
                            builder.add(ZipWith.create( (block, hashResult) -> {
                                block.setHash(hashResult.getHash());
                                block.setNonce(hashResult.getNonce());
                                return block;
                            } ));

                    builder.from(builder.add(firstHashValue))
                            .viaFanIn(receiveHashes); //connect something else to inlet2 of receiveHashes

                    builder.from(receiveHashes).toInlet(applyLastHashToBlock.in0());
                    builder.from(applyLastHashToBlock.out())
                            .viaFanOut(broadcast);

                    builder.from(broadcast)
                            .toInlet(receiveHashResult.in0());
                    builder.from(broadcast)
                            .via(mineBlock)
                            .viaFanOut(duplicateHashResult)
                            .toInlet(receiveHashResult.in1());

                    builder.from(duplicateHashResult)
                            .via(builder.add(Flow.of(HashResult.class).map( hr -> hr.getHash())))
                            .viaFanIn(receiveHashes);

                    return FlowShape.of(applyLastHashToBlock.in1(), receiveHashResult.out());
                })
        );

        Sink<Block, CompletionStage<Done>> sink = Sink.foreach(block -> {
            blockChain.addBlock(block);
            blockChain.printAndValidate();
        });

        transactionsSource.via(blockBuilder)
                .via(miningFlow.async().addAttributes(Attributes.inputBuffer(1,1)))
                .to(sink)
                .run(actorSystem);
    }
}
