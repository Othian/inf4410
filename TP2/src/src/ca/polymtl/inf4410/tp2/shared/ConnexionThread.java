package ca.polymtl.inf4410.tp2.shared;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class ConnexionThread extends Thread {

	private Serveur serveur;
	private TacheThread tacheThread;
	
	
	public ConnexionThread(Serveur serveur, TacheThread tacheThread) {
		super();
		this.serveur = serveur;
		this.tacheThread = tacheThread;
	}


	public void run() {
		try {
			int resultat = serveur.getStub().calculerOperations((ArrayList<Paire>) tacheThread.getTache().getListeOperations());
			if(tacheThread.getTache().getRepartiteur().isDebug()) {
				System.out.println("["+tacheThread.getTache().getId()+"] Serveur "+serveur.getAdresseIP()+":"+serveur.getPort()+" - Résultat : "+resultat);
			}
			tacheThread.onConnexionEvent(this, Statut.REUSSIE, resultat);
		} catch (RemoteException e) {
			if(tacheThread.getTache().getRepartiteur().isDebug()) {
				System.out.println("["+tacheThread.getTache().getId()+"] Serveur "+serveur.getAdresseIP()+":"+serveur.getPort()+" - Injoignable");
			}
			tacheThread.onConnexionEvent(this, Statut.ECHEC, -1);
		}
	}


	public Serveur getServeur() {
		return serveur;
	}


	public void setServeur(Serveur serveur) {
		this.serveur = serveur;
	}


	public TacheThread getTacheThread() {
		return tacheThread;
	}


	public void setTacheThread(TacheThread tacheThread) {
		this.tacheThread = tacheThread;
	}
	
	
}
