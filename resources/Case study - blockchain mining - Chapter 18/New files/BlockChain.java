package model;

import java.util.LinkedList;
import utils.BlockChainUtils;

public class BlockChain {

	private LinkedList<Block> blocks;
	
	public BlockChain() {
		blocks = new LinkedList<>();
	}
	
	public void addBlock(Block block) throws BlockValidationException {

		String lastHash = "0";
		
		if (blocks.size() > 0) {
			lastHash = blocks.getLast().getHash();
		}
		
		if (!lastHash.equals(block.getPreviousHash())) {
			System.out.println("hashes don't match");
			throw new BlockValidationException();
		}
		
		if (!BlockChainUtils.validateBlock(block)) {
			System.out.println("block doesn't validate");
			throw new BlockValidationException();
		}
		
		blocks.add(block);
	}
	
	public void printAndValidate() {
		String lastHash = "0";
		for (Block block : blocks) {
			System.out.println("model.Block with trans starting at " + block.getFirstId() + " ");
			System.out.println(block.getTransactions());
			System.out.println("Hash " + block.getHash());
			System.out.println("Lash Hash " + block.getPreviousHash());
			
			if (block.getPreviousHash().equals(lastHash)) {
				System.out.print("Last hash matches ");
			} else {
				System.out.print("Last hash doesn't match ");
			}
			
			if (BlockChainUtils.validateBlock(block)) {
				System.out.println("and hash is valid");
			} else {
				System.out.println("and hash is invalid");
			}
			
			lastHash = block.getHash();
			
		}
	}
	
	public String getLastHash() {
		if (blocks.size() > 0)
			return blocks.getLast().getHash();
		return "0";
	}

	public int getSize() {
		return blocks.size();
	}

}
