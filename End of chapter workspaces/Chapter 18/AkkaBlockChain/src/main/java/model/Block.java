package model;

import java.util.ArrayList;
import java.util.List;

public class Block {
	private String previousHash;
	private List<Transaction> transactions;
	private int nonce;
	private String hash;
	
	public Block(String previousHash, List<Transaction> transactions) {
		this.previousHash = previousHash;
		this.transactions = transactions;
	}

	public void setNonce(int nonce) {
		this.nonce = nonce;
	}
	
	public void setHash(String hash) {
		this.hash = hash;
	}
		
	public String getHash() {
		return hash;
	}

	public String getPreviousHash() {
		return previousHash;
	}

	public String getTransactions() {
		return transactions.toString();
	}

	public Long getFirstTimestamp() {
		return transactions.get(0).getTimestamp();
	}

	public Long getLastTimestamp() {
		return transactions.get(transactions.size()-1).getTimestamp();
	}

	public Integer getFirstId() {
		return transactions.get(0).getId();
	}

	public Transaction getFirstTransaction() {
		return transactions.get(0);
	}

	public void addTransactionToList(Transaction transaction) {
		if (this.hash != null) throw new RuntimeException("Cannot add a transaction to a mined block");
		transactions.add(transaction);
	}

	public List<Transaction> extractTransactions() {
		return new ArrayList<>(this.transactions);
	}

	public int getNonce() {
		return nonce;
	}

	public String transactionIds() {
		StringBuilder sb = new StringBuilder();
		for (Transaction t : transactions) {
			sb.append(t.getId() + " ");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return "model.Block{" +
				"previousHash='" + previousHash + '\'' +
				", transaction=" + transactionIds() +
				", nonce=" + nonce +
				", hash='" + hash + '\'' +
				'}';
	}
}
