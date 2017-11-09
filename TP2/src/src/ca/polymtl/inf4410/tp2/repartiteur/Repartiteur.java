package ca.polymtl.inf4410.tp2.repartiteur;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import ca.polymtl.inf4410.tp2.shared.Constantes;
import ca.polymtl.inf4410.tp2.shared.Paire;
import ca.polymtl.inf4410.tp2.shared.ServerInterface;
import ca.polymtl.inf4410.tp2.shared.Serveur;
import ca.polymtl.inf4410.tp2.shared.Statut;
import ca.polymtl.inf4410.tp2.shared.Tache;
import ca.polymtl.inf4410.tp2.shared.TacheThread;
import ca.polymtl.inf4410.tp2.shared.Utils;

public class Repartiteur {
	
	private boolean securise;
	private int tailleTaches;
	private ConcurrentLinkedQueue<Paire> listeOperations;
	private ConcurrentLinkedQueue<Tache> listeTaches;
	private List<Serveur> listeServeurs;
	private List<TacheThread> listeThreads;
	private int currentServeur = 0;
	private int resultat = 0;
	
	private long debut;
	private long fin;
	
	// 1er argument : Mode (-n ou -s)
	// 2eme argument : Fichier des serveurs
	// 3ème argument : Fichier des opérations
	public static void main(String[] args) {
		boolean temp_securise;
		if(args.length != 3) {
			System.out.println("Le nombre d'arguments n'est pas valide");
		} else {
			Repartiteur repartiteur = new Repartiteur(args[0], args[1], args[2]);
			repartiteur.run();
		}
	}

	Repartiteur(String mode, String fichierServeurs, String fichierOperations) {
		if(mode.equals("-s")) {
			securise = true;
		} else {
			securise = false;
		}
		
		listeServeurs = (ArrayList<Serveur>) Utils.lireFichierServeurs(fichierServeurs);
		listeOperations = (ConcurrentLinkedQueue<Paire>) Utils.lireFichierOperations(fichierOperations);
		listeTaches = new ConcurrentLinkedQueue<Tache>();
		listeThreads = new ArrayList<TacheThread>();
	}
	
	private void run() {
		debut = System.currentTimeMillis();
		int capacites = 0;
		for(Serveur serveur : listeServeurs) {
			ServerInterface stub = null;
			capacites += serveur.getCapacite();
			try {
				Registry registry = LocateRegistry.getRegistry(serveur.getAdresseIP(), Constantes.RMI_REGISTRY_PORT);
				stub = (ServerInterface) registry.lookup("calculateur-"+Integer.toString(serveur.getPort()));
				serveur.setStub(stub);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Définition de la taille des tâches
		tailleTaches = Math.round((float)capacites/listeServeurs.size());
		
		// Création des taches
		int i = 1;
		int id = 0;
		ArrayList<Paire> operations = new ArrayList<Paire>();
		while(!listeOperations.isEmpty()) {
			if(i % tailleTaches == 0) {
				operations.add(listeOperations.poll());
				Tache tache = new Tache(this, id, operations, Statut.PRET);
				System.out.println("Création de la tache "+id+" avec "+operations.size()+" opérations");
				listeTaches.add(tache);
				operations = new ArrayList<Paire>();
				i = 1;
				id++;
			} else {
				operations.add(listeOperations.poll());
				i++;
			}
		}
		
		// Création des threads de taches
		if(securise) {
			for(int k=0; k<listeServeurs.size(); k++) {
				TacheThread thread = creerThread(listeTaches.poll());
				thread.start();
			}
		} else {
			for(int k=0; k<(listeServeurs.size()+1)/2; k++) {
				TacheThread thread = creerThread(listeTaches.poll());
				thread.start();
			}
		}
		
	}
	
	private synchronized TacheThread creerThread(Tache tache) {
		TacheThread thread = new TacheThread(tache);
		listeThreads.add(thread);
		System.out.println("Création du thread de la tache "+tache.getId());
		return thread;
	}
	
	private synchronized boolean retirerThread(TacheThread thread) {
		listeThreads.remove(thread);
		return listeThreads.size() == 0;
	}
	
	public synchronized void onTacheCompleted(Tache tache, TacheThread tacheThread, int resultat) {
		synchronized(this) {
			this.resultat = (this.resultat + resultat) % 4000;
		}
		System.out.println("Tache "+tache.getId()+" terminée");
		listeTaches.remove(tache);
		retirerThread(tacheThread);
		
		if(!listeTaches.isEmpty()) {
			TacheThread thread = creerThread(listeTaches.poll());
			thread.start();
		} else {
			travailFait(tacheThread);
		}
	}
	
	private void travailFait(TacheThread thread) {
		if(retirerThread(thread)) {
			fin = System.currentTimeMillis();
			System.out.println("Calcul terminé - Résultat : ");
			System.out.println(resultat);
			System.out.println("Temps écoulé : ");
			double tempsEcoule = ((double)(fin-debut)/1000.0);
			System.out.println(tempsEcoule+" secondes");
		}
		
	}

	public boolean getSecurise() {
		return securise;
	}
	
	public Serveur getNouveauServeur() {
		Serveur srv = listeServeurs.get(currentServeur);
		synchronized(this) {
			currentServeur = (currentServeur + 1) % listeServeurs.size();
		}
		
		return srv;
	}
	
	public int getNombreServeurs() {
		return listeServeurs.size();
	}
}

class ComparateurServeurs implements Comparator<Serveur> {
    @Override
    public int compare(Serveur o1, Serveur o2) {
        return o1.compareTo(o2);
    }
}