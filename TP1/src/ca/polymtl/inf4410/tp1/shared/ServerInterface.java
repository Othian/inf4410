package ca.polymtl.inf4410.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
	
	
	int generateclientid() throws RemoteException;
	boolean create(String nom) throws RemoteException;
	List<InformationFichier> list() throws RemoteException;
	List<Fichier> syncLocalDir() throws RemoteException;
	int get(String nom, String checksum) throws RemoteException;
	boolean lock(String nom, int clientid, String checksum) throws RemoteException;
	boolean push(String nom, string contenu,  int clientid)
	
}
