package ca.polymtl.inf4410.tp1.server;

import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ca.polymtl.inf4410.tp1.shared.FileInfo;
import ca.polymtl.inf4410.tp1.shared.FullFile;
import ca.polymtl.inf4410.tp1.shared.ServerInterface;
import ca.polymtl.inf4410.tp1.shared.Utils;

public class Server implements ServerInterface {
	private int lastClientId;
	private ConcurrentHashMap<String, FileInfo> listFiles;
	
	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	public Server() {
		super();
		lastClientId = 0;
		listFiles = new ConcurrentHashMap<String, FileInfo>();
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


	/**
	 * Génère un nouvel id client par incrémentation du dernier id client attribué
	 * Synchronized pour le rendre thread-safe
	 * @return l'id du nouveau client
	 */
	public synchronized int generateclientid() throws RemoteException
	{
			return ++lastClientId;
	}
	
	/**
	 * Enregistre le fichier dans la liste des fichiers du serveur.
	 * Créé le fichier correspondant (contenu vide)
	 * @param nom Le nom du fichier à ajouter
	 * @return true si la création a réussi, false sinon.
	 */
	public boolean create(String nom) throws RemoteException {
		if(listFiles.containsKey(nom)) {
			return false;
		} else {
			FullFile file = new FullFile(nom);
			listFiles.put(nom, file.getFileInfo());
			try {
				Utils.writeFile(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
	}
	
	/**
	 * Retourne la liste des fichiers sur le serveur ainsi que les informations associées.
	 * @return une liste avec pour clés les noms des fichiers et comme valeur les FileInfo correspondants.
	 */
	public Map<String, FileInfo> list() throws RemoteException {
		if(listFiles == null) {
			throw new RemoteException("La liste des fichiers n'a pas pu être récupérée sur le serveur");
		}
		
		return listFiles;
		/*
		if(listFiles.size() == 0) {
			return "0 fichier(s) \n";
		} else {
			StringBuilder sb = new StringBuilder();
			for(Map.Entry<String, FileInfo> e : listFiles.entrySet()) {
				FileInfo fi = e.getValue();
				if(fi.isLocked()) {
					sb.append(fi.getName());
					sb.append(" est verrouillé par client ");
					sb.append(fi.getClientId());
					sb.append("\n");
				} else {
					sb.append(fi.getName());
					sb.append(" non verrouillé ");
					sb.append("\n");
				}
			}
			
			return sb.toString();
		}*/
	}
	
	/**
	 * Récupère les fichiers du serveur et prépare la transmission au client.
	 * Lit les fichiers du serveur correspondant à ceux enregistrés dans l'attribut listFiles.
	 * Génère les objets FullFile correspondants et les ajoute à la liste complète.
	 * @return la liste des fichiers.
	 */
	public List<FullFile> syncLocalDir() throws RemoteException {
		ArrayList<FullFile> listFullFiles = new ArrayList<FullFile>();
		for(Map.Entry<String, FileInfo> e : listFiles.entrySet()) {
			FullFile tempFile = null;
			try {
				tempFile = Utils.getFileFromInfo(e.getValue());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			listFullFiles.add(tempFile);
		}
		
		return listFullFiles;
	}
	
	/**
	 * Récupère la dernière version du fichier sur le serveur et la prépare pour l'envoi.
	 * La méthode compare le checksum du fichier sur le serveur à celui transmis par le client.
	 * Si le checksum est identique, la méthode notifie le client que le fichier n'a pas été mis à jour, et ne transfère 
	 * pas le contenu.
	 * Si le checksum est différent, le fichier complet est transmis au client.
	 */
	public FullFile get(String nom, String checksum) throws RemoteException {
		FileInfo fileInfo = listFiles.get(nom);
		if(fileInfo == null) {
			throw new RemoteException("Le fichier "+nom+" n'existe pas sur le serveur");
		}
		FullFile file = new FullFile();
		file.setFileInfo(fileInfo);
		if(fileInfo.getChecksum().equals(checksum)) {
			file.setContent(new byte[0]);
			file.setUpdated(false);
		} else {
			try {
				Utils.getFileContent(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			file.setUpdated(true);
		}
		
		return file;
	}
	
	/**
	 * Verrouille le fichier sur le serveur. Récupère la dernière version disponible. Compare le checksum du fichier sur le serveur
	 * avec le checksum soumis.
	 * Si le checksum est identique, le contenu n'est pas mis à jour (similairement à get)
	 * Si le checksum est différent, le contenu est récupéré et mis à jour.
	 * @param nom le nom du fichier
	 * @param clientid l'id du client faisant la demande de verrouillage
	 * @param checksum le checksum du fichier du client
	 * @return le fichier, avec éventuellement son contenu à mettre à jour.
	 */
	public FullFile lock(String nom, int clientid, String checksum) throws RemoteException {
		FileInfo fi = listFiles.get(nom);
		
		// Si fi est null, le fichier n'existe pas sur le serveur (le client doit le créer d'abord)
		if(fi == null) {
			throw new RemoteException(nom + " n'existe pas sur le serveur");
		} else {
			// Si le clientId n'est pas -1, alors le fichier est déjà verrouillé.
			// synchronized est utilisé ici pour prévenir une lecture simultanée par deux threads de clientId, 
			// qui feraient que les deux threads liraient que le fichier est non verrouillé
			synchronized(this) {
				if(fi.getClientId() != -1) {
					throw new RemoteException(nom + " est déjà verrouillé par le client "+Integer.toString(fi.getClientId()));
				}
				
				// On associe l'id du client pour locker le fichier.
				fi.setClientId(clientid);
			}
			
			// On prépare la structure de retour FullFile
			FullFile file = new FullFile();
			file.setFileInfo(fi);
			
			// Si les checksum correspondent, on ne transfère pas de contenu
			if(fi.getChecksum().equals(checksum)) {
				// On ajoute un flag pour signaler que l'absence de contenu s'explique par une non mise à jour
				file.setUpdated(false);
				file.setContent(new byte[0]);
			} else {
				// Sinon, on récupère le contenu et on le transfère, et on ajoute un flag.
				file.setUpdated(true);
				try {
					Utils.getFileContent(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			return file;
		}
	}
	
	/**
	 * Met à jour le fichier sur le serveur avec le contenu envoyé, sous réserve que le fichier ait été locké par le client avant.
	 * @param nom le nom du fichier
	 * @param contenu le contenu du fichier, qui doit remplacer le contenu actuel
	 * @param clientid l'id du client qui demande à mettre à jour le fichier
	 * @return true si l'opération réussit (sinon des exceptions donnent des détails sur les erreurs)
	 */
	public boolean push(String nom, byte[] contenu,  int clientid) throws RemoteException {
		FileInfo fi;
		FullFile file = new FullFile();
		fi = listFiles.get(nom);
		
		if(fi == null) {
			throw new RemoteException("Opération refusée : vous devez d'abord créer le fichier sur le serveur");
		}
		synchronized(this) {
			if(fi.getClientId() != clientid) {
				throw new RemoteException("Opération refusée : vous devez d'abord verrouiller le fichier");
			}
		}
		
		file.setFileInfo(fi);
		file.setContent(contenu);
		
		try {
			synchronized(this) {
				Utils.writeFile(file);
				fi.setClientId(-1);
				fi.setChecksum(file.computeChecksum());
			}
		} catch (IOException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
		
	}
	
	
	/*
	 * Méthode accessible par RMI. Méthode vide.
	 */
	
	
}
