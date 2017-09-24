package ca.polymtl.inf4410.tp1.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import ca.polymtl.inf4410.tp1.shared.ServerInterface;

public class Server implements ServerInterface {
	
	static lastClientId=0;
	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	public Server() {
		super();
	}

	private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			ServerInterface stub = (ServerInterface) UnicastRemoteObject
					.exportObject(this, 0);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("server", stub);
			System.out.println("Server ready.");
		} catch (ConnectException e) {
			System.err
					.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	/*
	 * Méthode accessible par RMI. Additionne les deux nombres passés en
	 * paramètre.
	 */
	@Override
	int synchronized generateclientid() throws RemoteException
	{
			return Server.lastClientId++;
	}
	boolean create(String nom) throws RemoteException {
		}
	List<InformationFichier> list() throws RemoteException;
	List<Fichier> syncLocalDir() throws RemoteException;
	int get(String nom, String checksum) throws RemoteException;
	boolean lock(String nom, int clientid, String checksum) throws RemoteException;
	boolean push(String nom, string contenu,  int clientid)
	
	
	/*
	 * Méthode accessible par RMI. Méthode vide.
	 */
	
	
}
