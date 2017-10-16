package ca.polymtl.inf4410.tp1.shared;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

public class Utils {
	
	/**
	 * Ecrit le fichier caractérisé par file (type FullFile) sur le disque.
	 * @param file le fichier à écrire
	 * @throws IOException
	 */
	public static void writeFile(FullFile file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file.getFileInfo().getName());
		fos.write(file.getContent());
		fos.close();
	}
	
	/**
	 * Lit un fichier à partir de ses informations.
	 * @param fileInfo l'objet FileInfo représentant les informations du fichier
	 * @return le fichier complet, tel qu'il est sur le disque.
	 * @throws IOException
	 */
	public static FullFile getFileFromInfo(FileInfo fileInfo) throws IOException {
		byte[] content = Files.readAllBytes(new File(fileInfo.getName()).toPath());
		return new FullFile(fileInfo, content);
	}
	
	/**
	 * Récupère le contenu du fichier sur le disque et met à jour l'objet FullFile passé en paramètre.
	 * @param file l'objet FullFile qui sera mis à jour
	 * @throws IOException
	 */
	public static void getFileContent(FullFile file) throws IOException {
		byte[] content = Files.readAllBytes(new File(file.getFileInfo().getName()).toPath());
		file.setContent(content);
	}
	/**
	 * Crée un objet FullFile à partir d'un fichier local.
	 * @param nom le nom du fichier
	 * @param clientId le client id du client (sera inscrit dans le FileInfo du FullFile)
	 * @return le fichier complet, avec ses infos et son contenu.
	 * @throws IOException
	 */
	public static FullFile getFileFromLocal(String nom, int clientId) throws IOException {
		FileInfo fi = new FileInfo(nom);
		FullFile file = new FullFile();
		fi.setClientId(clientId);
		
		byte[] content;
		
		File f = new File(nom);
		if(f.exists() && !f.isDirectory()) {
			 content = Files.readAllBytes(f.toPath());
			 file.setFileInfo(fi);
				file.setContent(content);
				try {
					file.getFileInfo().setChecksum(file.computeChecksum());
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		} else {
			fi.setChecksum("-1");
			file.setFileInfo(fi);
		}
		
		
		return file;
	}
}
