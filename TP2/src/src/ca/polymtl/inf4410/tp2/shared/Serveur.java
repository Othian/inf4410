package ca.polymtl.inf4410.tp2.shared;

public class Serveur implements Comparable<Serveur> {
	private String adresseIP;
	private int port;
	private int capacite;
	private ServerInterface stub;
	
	
	public Serveur(String adresseIP, int port, int capacite, ServerInterface stub) {
		super();
		this.adresseIP = adresseIP;
		this.port = port;
		this.capacite = capacite;
		this.stub = stub;
	}
	
	public String getAdresseIP() {
		return adresseIP;
	}
	
	public void setAdresseIP(String adresseIP) {
		this.adresseIP = adresseIP;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getCapacite() {
		return capacite;
	}
	
	public void setCapacite(int capacite) {
		this.capacite = capacite;
	}
	
	public ServerInterface getStub() {
		return stub;
	}
	
	public void setStub(ServerInterface stub) {
		this.stub = stub;
	}

	@Override
	public int compareTo(Serveur o) {
		// TODO Auto-generated method stub
		if(capacite < o.capacite) {
			return -1;
		} else if(capacite > o.capacite) {
			return 1;
		} else {
			return 0;
		}
	}
	
	
}
