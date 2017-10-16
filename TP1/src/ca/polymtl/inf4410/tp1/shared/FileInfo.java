package ca.polymtl.inf4410.tp1.shared;

import java.io.Serializable;

public class FileInfo implements Serializable {
	private String name;
	private int clientId;
	private String checksum;
	
	
	
	public FileInfo(String name) {
		super();
		this.name = name;
		this.clientId = -1;
		this.checksum = "-1";
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getChecksum() {
		return checksum;
	}
	
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public synchronized int getClientId() {
		return clientId;
	}

	public synchronized void setClientId(int clientId) {
		this.clientId = clientId;
	}
	
}
