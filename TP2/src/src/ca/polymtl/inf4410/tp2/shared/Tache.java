package ca.polymtl.inf4410.tp2.shared;

import java.util.List;

import ca.polymtl.inf4410.tp2.repartiteur.Repartiteur;

public class Tache {
	
	private Repartiteur repartiteur;
	private int id;
	private List<Paire> listeOperations;
	private Statut statut;
	private int resultat;
	
	public Tache(Repartiteur repartiteur, int id, List<Paire> listeOperations, Statut statut) {
		super();
		this.repartiteur = repartiteur;
		this.id = id;
		this.listeOperations = listeOperations;
		this.statut = statut;
	}

	public Repartiteur getRepartiteur() {
		return repartiteur;
	}

	public void setRepartiteur(Repartiteur repartiteur) {
		this.repartiteur = repartiteur;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<Paire> getListeOperations() {
		return listeOperations;
	}

	public void setListeOperations(List<Paire> listeOperations) {
		this.listeOperations = listeOperations;
	}

	public Statut getStatut() {
		return statut;
	}

	public void setStatut(Statut statut) {
		this.statut = statut;
	}
	
	public void setResultat(int resultat) {
		this.resultat = resultat;
	}
	
	public int getResultat() {
		return this.resultat;
	}
}
