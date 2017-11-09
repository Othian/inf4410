package ca.polymtl.inf4410.tp2.shared;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Utils {
	
	public static List<Serveur> lireFichierServeurs(String chemin) {
		ArrayList<Serveur> listeServeurs = new ArrayList<Serveur>();
		try{
			InputStream flux=new FileInputStream(chemin); 
			InputStreamReader lecture=new InputStreamReader(flux);
			BufferedReader buffer=new BufferedReader(lecture);
			String ligne;
			while ((ligne=buffer.readLine())!=null){
				String[] strings = ligne.split(":");
				Serveur temp_serv = new Serveur(strings[0], Integer.parseInt(strings[1]), Integer.parseInt(strings[2]), null);
				listeServeurs.add(temp_serv);
			}
			buffer.close(); 
			}		
			catch (Exception e){
			System.out.println(e.toString());
			}
		
		return listeServeurs;
	}
	
	public static Queue<Paire> lireFichierOperations(String chemin) {
		ConcurrentLinkedQueue<Paire> listePaires = new ConcurrentLinkedQueue<Paire>();
		try{
			InputStream flux=new FileInputStream(chemin); 
			InputStreamReader lecture=new InputStreamReader(flux);
			BufferedReader buffer=new BufferedReader(lecture);
			String ligne;
			while ((ligne=buffer.readLine())!=null){
				String[] strings = ligne.split(" ");
				Paire temp_paire = new Paire(strings[0], Integer.parseInt(strings[1]));
				listePaires.add(temp_paire);
			}
			buffer.close(); 
			}		
			catch (Exception e){
			System.out.println(e.toString());
			}
		
		return listePaires;
	}
}
