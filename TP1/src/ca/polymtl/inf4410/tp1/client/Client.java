package ca.polymtl.inf4410.tp1.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ca.polymtl.inf4410.tp1.shared.FileInfo;
import ca.polymtl.inf4410.tp1.shared.FullFile;
import ca.polymtl.inf4410.tp1.shared.ServerInterface;
import ca.polymtl.inf4410.tp1.shared.Utils;

public class Client {
	private int clientId;
	
	public static void main(String[] args) {
		String distantHostname = loadIPFile();
		Client client = new Client(distantHostname);
		//client.run();

		if (args.length == 0) {
			// Si aucun argument, on affiche toutes les commandes possibles
			displayCommands();
			return;
		} else if(args.length == 1) {
			switch(args[0]) {
				case "list":
					client.list();
					break;
				case "syncLocalDir":
					client.syncLocalDir();
					break;
				default:
					System.out.println("Commande incorrecte (commande non reconnue, argument manquant");
			}
		} else if(args.length == 2) {
			switch(args[0]) {
			case "create":
				client.create(args[1]);
				break;
			case "get":
				client.get(args[1]);
				break;
			case "lock":
				client.lock(args[1]);
				break;
			case "push":
				client.push(args[1]);
				break;
			default:
				System.out.println("Commande inconnue (commande non reconnue, argument manquant)");
			}
		}

		
	}

	/**
	 * Affiche les commandes disponibles dans le client. Affichée lorsqu'aucune commande n'est entrée (appel ./client)
	 * 
	 */
	public static void displayCommands() {
		System.out.println("Vous devez entrer une commande : ");
		System.out.println("- create nom_fichier ");
		System.out.println("\t Ajoute le fichier nom_fichier sur le serveur (vous devez ensuite verrouiller et pusher le fichier local sur le serveur");
		System.out.println("- list ");
		System.out.println("\t Liste les fichiers sur le serveur ainsi que les clients les ayant verrouillé");
		System.out.println("- syncLocalDir ");
		System.out.println("\t Récupère le contenu de tous les fichiers sur le serveur, et les écrit en local");
		System.out.println("- get nom_fichier ");
		System.out.println("\t Récupère le fichier nom_fichier du serveur et met à jour son contenu si nécessaire");
		System.out.println("- lock nom_fichier");
		System.out.println("\t Verrouille le fichier nom_fichier sur le serveur");
		System.out.println("- push nom_fichier");
		System.out.println("\t Envoie le fichier nom_fichier sur le serveur (nécessite d'avoir verrouillé le fichier)");
	}
	
	/**
	 * Traite une exception pour en extraire le seul message de détails, puis l'affiche dans un format facile à comprendre pour l'humain.
	 * @param ex L'exception à traiter
	 */
	public static void displayError(Exception ex) {
		Throwable cause = ex.getCause();
		if(cause == null) {
			System.out.println(ex.getMessage());
		} else {
			// On obtient le texte complet de l'exception :
			String errorMsg = cause.toString();
			int startMsg = errorMsg.indexOf(":");
			// On extrait le message détaillé à partir du message complet, et on supprime le contexte (Exception thrown ...)
			String detailedMsg = errorMsg.substring(startMsg+1);
			System.out.println(detailedMsg.trim());
		}
	}
	
	private ServerInterface distantServerStub = null;

	/**
	 * Constructeur de la classe Client.
	 * Essaye également de retrouver l'id du client si le client a déjà été lancé dans ce dossier.
	 * @param distantServerHostname l'adresse IP ou le hostname du serveur
	 */
	public Client(String distantServerHostname) {
		super();
		
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		

		if (distantServerHostname != null) {
			distantServerStub = loadServerStub(distantServerHostname);
		}
		
		loadClientId();
		
		if(this.clientId == -1) {
			try {
				int id = distantServerStub.generateclientid();
				this.clientId = id;
				saveClientId();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Retrouve l'objet associé au serveur dans le RMI registry 
	 * @param hostname l'adresse IP ou le hostname du serveur
	 * @return le stub du serveur (de type ServerInterface), sur lequel les appels de fonctions RMI seront faites
	 */
	private ServerInterface loadServerStub(String hostname) {
		ServerInterface stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (ServerInterface) registry.lookup("server");
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage()
					+ "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	}
	
	/**
	 * Lit le fichier .server_ip pour récupérer l'IP du serveur. 
	 * @return l'ip contenue dans le fichier .server_ip, ou 127.0.0.1 si celui ci n'existe pas.
	 */
	private static String loadIPFile() {
		Path path = Paths.get(".server_ip");
	    try {
			List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
			if(lines.size() == 1) {
				return lines.get(0);
			} else {
				return "127.0.0.1";
			}
		} catch (IOException e) {
			return "127.0.0.1";
		}
	}

	/**
	 * Enregistre la valeur actuelle de l'attribut clientId dans un fichier .client_id dans le dossier pour les appels ultérieurs au client.
	 */
	private void saveClientId() {
		PrintWriter writer;
		try {
			writer = new PrintWriter(".client_id", "UTF-8");
			writer.println(Integer.toString(clientId));
			writer.close();
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * Lit le fichier .client_id du dossier courant et met à jour l'attribut clientId selon la valeur retrouvée (
	 * -1 si le fichier n'existe pas)
	 */
	private void loadClientId() {
		Path path = Paths.get(".client_id");
	    try {
			List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
			if(lines.size() == 1) {
				this.clientId = Integer.parseInt(lines.get(0));
			} else {
				this.clientId = -1;
			}
		} catch (IOException e) {
			this.clientId = -1;
		}
	}
	
	/**
	 * Appelle la fonction du serveur permettant de créer un fichier.
	 * @param nom le nom du fichier à créer
	 */
	private void create(String nom) {
		boolean result;
		
		try {
			result = distantServerStub.create(nom);
			if(result) {
				System.out.println(nom + " ajouté");
			} else {
				System.out.println(nom + " existe déjà");
			}
		} catch (RemoteException e) {
			displayError(e);
		}
	}
	
	/**
	 * Appelle la fonction get du serveur distant, qui permet d'obtenir la version la plus récente du fichier sur le serveur.
	 * @param nom le nom du fichier à obtenir.
	 */
	private void get(String nom) {
		FullFile file = null;
		try {
			file = Utils.getFileFromLocal(nom, this.clientId);
		} catch (IOException e) {
			displayError(e);
		}
		
		FullFile responseFile = null;
		try {
			responseFile = distantServerStub.get(nom, file.getFileInfo().getChecksum());
		} catch (RemoteException e) {
			displayError(e);
			return;
		}
	
		if(responseFile.isUpdated()) {
			try {
				Utils.writeFile(responseFile);
			} catch (IOException e) {
				displayError(e);
			}
		}
		
		
		System.out.println(nom + " synchronisé");
	}
	
	/**
	 * Appelle la fonction list du serveur distant. 
	 * Affiche la liste des fichiers sur le serveur ainsi que des informations sur le verrouillage ou non des fichiers.
	 */
	private void list() {
		
		ConcurrentHashMap<String, FileInfo> listFiles = null;
		try {
			listFiles = (ConcurrentHashMap<String, FileInfo>) distantServerStub.list();
		} catch (RemoteException e1) {
			displayError(e1);
		}
		
		if(listFiles.size() == 0) {
			System.out.println("0 fichier(s)");
		} else {
			StringBuilder sb = new StringBuilder();
			for(Map.Entry<String, FileInfo> e : listFiles.entrySet()) {
				FileInfo fi = e.getValue();
				// Le fichier est verrouillé
				if(fi.getClientId() != -1) {
					sb.append("* ");
					// On formate le nom à 20 caractères pour aligner le texte
					String name = String.format("%-20s", fi.getName());
					sb.append(name);
					sb.append(" verrouillé par client ");
					sb.append(fi.getClientId());
					sb.append("\n");
				} else {
					// Le fichier n'est pasd verrouillé
					sb.append("* ");
					String name = String.format("%-20s", fi.getName());
					sb.append(name);
					sb.append(" non verrouillé ");
					sb.append("\n");
				}
			}
			
			System.out.print(sb.toString());
			System.out.println(listFiles.size() + " fichier(s)");
		}
	}
	
	/**
	 * Appelle la fonction syncLocalDir du serveur distant.
	 * Enregistre dans des fichiers locaux les dernières versions des fichiers obtenues du serveur.
	 */
	private void syncLocalDir() {
		ArrayList<FullFile> listFiles = null;
		
		try {
			listFiles = (ArrayList<FullFile>) distantServerStub.syncLocalDir();
		} catch (RemoteException e) {
			displayError(e);
		}
		
		for(FullFile file : listFiles) {
			try {
				Utils.writeFile(file);
			} catch (IOException e) {
				displayError(e);
			}
		}
	}
	
	/**
	 * Appelle la fonction lock du serveur distant.
	 * Verrouille un fichier, donnant à ce client l'exclusivité pour modifier le fichier et le mettre à jour sur le serveur.
	 * @param nom le nom du fichier à verrouiller.
	 */
	private void lock(String nom) {
		FullFile file = null;
		boolean exceptionOccurred = false;
		try {
			file = Utils.getFileFromLocal(nom, this.clientId);
		} catch (IOException e) {
			displayError(e);
		}
		
		FullFile updatedFile = new FullFile();
		try {
			updatedFile = distantServerStub.lock(nom, this.clientId, file.getFileInfo().getChecksum());
		} catch (RemoteException e) {
			exceptionOccurred = true;
			displayError(e);
		}
		
		// Si le fichier a été mis à jour au cours de l'opération, on l'écrit sur le disque
		if(updatedFile.isUpdated()) {
			try {
				Utils.writeFile(updatedFile);
			} catch (IOException e) {
				displayError(e);
			}
		}
		
		// Si l'exécution de lock n'a pas rencontré de problème, on affiche le message de réussite
		if(!exceptionOccurred) {
			System.out.println(nom + " verrouillé");
		}
	}
	
	/**
	 * Appelle la fonction push du serveur distant.
	 * Lit le fichier local et récupère son contenu pour l'envoyer au serveur.
	 * @param nom le nom du fichier
	 */
	private void push(String nom) {
		FullFile file = null;
		try {
			file = Utils.getFileFromLocal(nom, this.clientId);
			distantServerStub.push(nom, file.getContent(), this.clientId);
		} catch (IOException e) {
			displayError(e);
		}
		
	}
	
}

