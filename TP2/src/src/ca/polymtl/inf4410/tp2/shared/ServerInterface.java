package ca.polymtl.inf4410.tp2.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ServerInterface extends Remote {
	int calculerOperations(ArrayList<Paire> listeOperations) throws RemoteException;
}
