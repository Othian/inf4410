package ca.polymtl.inf4410.tp2.calculateur;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import ca.polymtl.inf4410.tp2.shared.Constantes;
import ca.polymtl.inf4410.tp2.shared.Paire;
import ca.polymtl.inf4410.tp2.shared.ServerInterface;

public class Calculateur implements ServerInterface {

	private int port;
	private int q;
	private int m;
	
	// 1er argument : port
	// 2ème argument : qi
	// 3ème argument : m (facultatif, 0 si non précisé)
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length < 2) {
			System.out.println("Vous devez spécifier une valeur de qi pour ce serveur");
		} else if(args.length == 2) {
			if(Integer.parseInt(args[0]) < 5000 || Integer.parseInt(args[0]) > 5050 ) {
				System.out.println("Le port d'écoute doit être entre 5000 et 5050");
			} else {
				Calculateur calculateur = new Calculateur(Integer.parseInt(args[0]), Integer.parseInt(args[1]), 0);
				calculateur.run();
			}
		} else if(args.length == 3) {
			if(Integer.parseInt(args[0]) < 5000 || Integer.parseInt(args[0]) > 5050 ) {
				System.out.println("Le port d'écoute doit être entre 5000 et 5050");
			} else {
				Calculateur calculateur = new Calculateur(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
				calculateur.run();
			}
		} else {
			System.out.println("Nombre d'arguments invalide");
		}
	}
	
	Calculateur(int port, int q, int m) {
		super();
		this.port = port;
		this.q = q;
		this.m = m;
		
		System.out.println("Lancement du serveur - Port : "+port+" - Q : "+ q +" - M :"+m);
	}

	private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			ServerInterface stub = (ServerInterface) UnicastRemoteObject
					.exportObject(this, port);

			Registry registry = LocateRegistry.getRegistry(Constantes.RMI_REGISTRY_PORT);
			registry.rebind("calculateur-"+Integer.toString(port), stub);
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
	
	public int calculerOperations(ArrayList<Paire> listeOperations) throws RemoteException {
		double tauxDeRefus = ((double) (listeOperations.size()-q))/(5.0*q);
		double tirageAuSort = Math.random();
		
		if(tirageAuSort <= tauxDeRefus) {
			throw new RemoteException("Tâche refusée");
		}
		
		int resultat = 0;
		for(Paire paire : listeOperations) {
			System.out.println("Opération : "+paire.getOperation()+" "+paire.getOperande());
			int temp = paire.performOperation();
			resultat = (resultat + temp) % 4000;
			System.out.println("Résultat : "+resultat);
		}
		
		double tauxMalicieux = (double) (m/100.0);
		double tirageAuSortMalicieux = Math.random();
		
		if(tirageAuSortMalicieux < tauxMalicieux) {
			return resultat + (int) (Math.random()*(2000-0)); // Faux résultat
		} else {
			return resultat; // Vrai résultat
		}
			
	}
	
	public boolean isAlive() throws RemoteException {
		return true;
	}
	
}
