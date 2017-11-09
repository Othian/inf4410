package ca.polymtl.inf4410.tp2.shared;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class TacheThread extends Thread {
	private Tache tache;
	private List<Serveur> serveursUtilises;
	private List<Serveur> serveursCourants;
	private List<Integer> resultats;
	private List<ConnexionThread> listeThreads;
	private boolean resultatObtenu;
	
	public TacheThread(Tache tache) {
		super();
		this.tache = tache;
		this.serveursUtilises = new ArrayList<Serveur>();
		this.serveursCourants = new ArrayList<Serveur>();
		this.resultats = new ArrayList<Integer>();
		this.listeThreads = new ArrayList<ConnexionThread>();
		this.resultatObtenu = false;
	}
	
	public Tache getTache() {
		return tache;
	}
	
	public void run() {
		selectionnerServeurs();
		lancerThreads();
		/*while(!resultatObtenu) {
			verifierServeurs();
			try {
				synchronized(this) {
					this.wait(1000);
				}
			} catch (InterruptedException e) {
			}
		}*/
	}
	
	private synchronized void lancerThreads() {
		for(Serveur srv : serveursCourants) {
			lancerThread(srv);
		}
	}
	
	/*private synchronized void verifierServeurs() {
		for(Serveur srv : serveursCourants) {
			try {
				srv.getStub().isAlive();
			} catch (RemoteException e) {
				Serveur newSrv = ajouterServeur();
				lancerThread(newSrv);
			}
		}
	}*/
	
	private void lancerThread(Serveur srv) {
		ConnexionThread srvThread = new ConnexionThread(srv, this);
		if(tache.getRepartiteur().isDebug()) {
			System.out.println("["+tache.getId()+"] Serveur sélectionné : "+srv.getAdresseIP()+":"+srv.getPort());
		}
		listeThreads.add(srvThread);
		srvThread.start();
	}
	
	private void selectionnerServeurs() {
		if(tache.getRepartiteur().getSecurise()) {
			ajouterServeur();
		} else {
			ajouterServeur();
			ajouterServeur();
		}
	}
	
	private Serveur ajouterServeur() {
		if(serveursUtilises.size() == tache.getRepartiteur().getNombreServeurs()) {
			serveursUtilises = new ArrayList<Serveur>();
		}
		
		Serveur serveur;
		do {
			serveur = tache.getRepartiteur().getNouveauServeur();
		} while(serveursUtilises.contains(serveur));
		
		synchronized(this) {
			serveursCourants.add(serveur);
			serveursUtilises.add(serveur);
		}
		return serveur;
	}
	
	public synchronized void onConnexionEvent(ConnexionThread thread, Statut statut, int resultat) {
		serveursCourants.remove(thread.getServeur());
		if(statut == Statut.REUSSIE) {
			resultats.add(resultat);
			if(!verifierResultats() && resultats.size() > 1) {
				Serveur srv = ajouterServeur();
				lancerThread(srv);
			}
		} else if(statut == Statut.ECHEC) {
			Serveur srv = ajouterServeur();
			lancerThread(srv);
		}
		
		listeThreads.remove(thread);
	}
	
	public synchronized boolean verifierResultats() {
		if(tache.getRepartiteur().getSecurise()) {
			resultatObtenu = true;
			tache.getRepartiteur().onTacheCompleted(tache, this, resultats.get(0));
			return true;
		} else {
			int j = 0;
			
			while(j < resultats.size()) {
				int target = resultats.get(j);
				for(int i=j+1; i<resultats.size(); i++) {
					if(resultats.get(i) == target) {
						resultatObtenu = true;
						tache.getRepartiteur().onTacheCompleted(tache, this, target);
						return true;
					}
				}
				
				j++;
			}
			return false;
		}
	}
		
}
	
	/*
	public void run() {
		if(tache.getRepartiteur().getSecurise()) {
			Serveur serveur = tache.getRepartiteur().getNouveauServeur();
			ConnexionThread srvThread = new ConnexionThread(serveur, this);
		} else {
			Serveur serveur = tache.getRepartiteur().getNouveauServeur();
			ConnexionThread srvThread = new ConnexionThread(serveur, this);
			Serveur serveur2 = tache.getRepartiteur().getNouveauServeur();
			ConnexionThread srvThread2 = new ConnexionThread(serveur2, this);
		}
	}
	
	public void onConnexionFailed() {
		
	}

	public void onConnexionSuccess(int resultat) {
		resultats.add(resultat);
		verifierResultats();
	}

	private void verifierResultats() {
		if(tache.getRepartiteur().getSecurise()) {
			tache.getRepartiteur().onTacheCompleted(tache, resultats.get(0));
		} else {
			boolean trouve = false;
			int j=0;
			while(!trouve) {
				int firstValue = resultats.get(j);
				for(int i=1; i<resultats.size(); i++) {
					if(resultats.get(i) == firstValue) {
						trouve = true;
						tache.getRepartiteur().onTacheCompleted(tache, firstValue);
					}
				}
				j++;
			}
		}
	}
*/
