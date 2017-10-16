package ca.polymtl.inf4410.tp1.shared;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

public class FullFile implements Serializable {

	private FileInfo fileInfo;
	private byte[] content;
	private boolean updated;
	
	public FullFile() {
		
	}
	
	public FullFile(String name) {
		super();
		this.fileInfo = new FileInfo(name);
		this.content = new byte[0];
		this.updated = false;
		try {
			this.fileInfo.setChecksum(computeChecksum());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public FullFile(FileInfo fileInfo, byte[] content) {
		super();
		this.fileInfo = fileInfo;
		this.content = content;
		this.updated = false;
		try {
			this.fileInfo.setChecksum(computeChecksum());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	public FileInfo getFileInfo() {
		return fileInfo;
	}
	public void setFileInfo(FileInfo fi) {
		this.fileInfo = fi;
	}
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = Arrays.copyOf(content, content.length);
	}
	
	public boolean isUpdated() {
		return updated;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	/**
	 * Calcule le checksum à partir de l'attribut content de cet objet.
	 * @return le checksum MD5 du fichier
	 * @throws NoSuchAlgorithmException
	 */
	/* Adapted from : http://www.baeldung.com/java-md5 */
	public String computeChecksum() throws NoSuchAlgorithmException {
         
	    MessageDigest md = MessageDigest.getInstance("MD5");
	    md.update(this.content);
	    byte[] digest = md.digest();
	    String myChecksum = DatatypeConverter
	      .printHexBinary(digest);
		
	    return myChecksum;
	}
}
