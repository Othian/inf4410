package ca.polymtl.inf4410.tp2.shared;

import java.io.Serializable;

public class Paire implements Serializable {
	
	private String operation;
	private int operande;
	
	public Paire(String operation, int operande) {
		super();
		this.operation = operation;
		this.operande = operande;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public int getOperande() {
		return operande;
	}

	public void setOperande(int operande) {
		this.operande = operande;
	}
	
	public int performOperation() {
		if(operation.equals("pell")) {
			return Operations.pell(operande);
		} else if(operation.equals("prime")) {
			return Operations.prime(operande);
		} else {
			return 0;
		}
	}
}