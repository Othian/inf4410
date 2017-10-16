package ca.polymtl.inf4410.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface ServerInterface extends Remote {
	
	
	int generateclientid() throws RemoteException;
	boolean create(String nom) throws RemoteException;
	Map<String, FileInfo> list() throws RemoteException;
	List<FullFile> syncLocalDir() throws RemoteException;
	FullFile get(String nom, String checksum) throws RemoteException;
	FullFile lock(String nom, int clientid, String checksum) throws RemoteException;
	boolean push(String nom, byte[] contenu,  int clientid) throws RemoteException;
	
}
