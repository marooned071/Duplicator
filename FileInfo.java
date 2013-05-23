import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Klasa przechowujaca informacje o plikach:
 * - nazwa
 * - sciezka
 * - rozmiar
 * - data modyfikacji
 * - hash MD5
 * @author Piotr Byliniak
 *
 */
public class FileInfo implements Comparable<FileInfo>, Serializable{
	private String name;
	private String path;
	private int size;
	private long dateModify;
	private String MD5hash;
	private String SH1hash;

	public FileInfo(){
		
	}
	public FileInfo(String name, String path, long size, long dateModify){
		this.name=name;
		this.path=path;
		this.size=(int) size;
		this.dateModify=dateModify;
		this.MD5hash=getHash(path,"MD5");
		this.SH1hash=null;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public long getDateModify() {
		return dateModify;
	}
	public void setDateModify(long dateModify) {
		this.dateModify = dateModify;
	}
	public String getMD5hash() {
		return MD5hash;
	}
	public void setMD5hash(String mD5hash) {
		this.MD5hash = mD5hash;
	}
	
	public String getSH1hash() {
		return SH1hash;
	}
	public void setSH1hash(String SH1hash) {
		this.SH1hash = SH1hash;
	}
	
	@Override 
	public boolean equals(Object anObject){
		if (anObject == null) {
			return false;
		}
		if (anObject == this) {
			return true;
		}
		if (anObject.getClass() != getClass()) {
			return false;
		}
		FileInfo other = (FileInfo) anObject;
		if (size == other.getSize())
			if (MD5hash.equals(other.getMD5hash())) {
				if(name.equals(other.getName())){
					if(path.equals(other.getPath())){
						if(dateModify==other.getDateModify()){
							return true;
						}
					}
					
				}
				
			}
		return false;

	}
	
	/**
	 * Metoda sprawdzajaca czy zawatoc plikow jest taka sama.. Nie jest brane pod uwage sciezka ani nazwa.
	 * Jesli rozmiar plikow jest taki sam porownywane sa hashe MD5, jesli one maja taka sama wartosc dla tych plikow generowany jest hash SHA-1.
	 * Jesli nawet SHA-1 sa sobie rowne szanse na kolizje sa bardzo male. 
	 * @param anObject - inny plik do porownania
	 * @return true- jest pliki maja taka sama zawartosc, false - jest maja rozna zawartosc
	 */
	public boolean equalsContent(Object anObject){
		if (anObject == null) {
			return false;
		}
		if (anObject == this) {
			return true;
		}
		if (anObject.getClass() != getClass()) {
			return false;
		}

		FileInfo other = (FileInfo) anObject;
		if (size == other.getSize())
			if (MD5hash.equals(other.getMD5hash())) {
				if(this.SH1hash==null){
					this.SH1hash=getHash(path,"SHA-1");
					System.out.println("Tworze sha-1 this");
				}
				if(other.getSH1hash()==null){
					other.setSH1hash(getHash(other.getPath(),"SHA-1"));
					System.out.println("Tworze sha-1 other");
				}
				if(this.SH1hash.equals(other.SH1hash)){
					return true;
				}
					
				
			}
		return false;

	}
	
	@Override
	public String toString() {
		String s=super.toString();
	    return s + "(" + name + ", " +
	    path + ", " +
        size + ", " +
        dateModify + ", " +
        MD5hash + ")";
	    
	}
	
	public String getNameAndPath(){
	    return path;
	}
	
	/**
	 * Metoda porownywujaca dwa obiekty FileInfo wzgledem rozmiaru. 
	 * Jesli rozmaiary sa takie same porownuje je wzgledem sciezki. 
	 */
	@Override
	public int compareTo(FileInfo other) {
		long result = size-other.getSize();
		if(result==0){
			result = MD5hash.compareTo(other.getMD5hash());
			if(result==0)
				return path.compareTo(other.getPath());
		}
			
		return (int) result;
	}
	
	/**
	 * Metoda porownywujaca dwa obiekty wzgledem dat modyfikacji. 
	 * UWAGA: drugi obiekt jest typu FileInfoLight!
	 * @param other
	 * @return
	 */
	public int compareTo(FileInfoLight other) {
		BigInteger a=BigInteger.valueOf(this.getDateModify());
		BigInteger b=BigInteger.valueOf(other.getDateModify());
		int result = a.compareTo(b);
		if(result==0){
			return path.compareTo(other.getPath());
		}
		return result;
	}
	/**
	 * Metoda tworzy obiekt FileInfoLight o polach tego obiektu.
	 * @return obiekt FileInfoLight o polach tego obiekt
	 */
	public FileInfoLight converToLight(){
		return new FileInfoLight(this.name, this.path, this.size, this.dateModify);
	}
	
	/**
	 * Metoda zwraca hash MD5 pliku o lokalizacji path
	 * @param path - lokalizacja pliku
	 * @return
	 */
	private static String getHash(String path, String alghoritm){
			byte[] md5sum = null;
			try{
				MessageDigest digest = MessageDigest.getInstance(alghoritm);

				InputStream is = new FileInputStream(new File(path));				
				byte[] buffer = new byte[8192];
				int read = 0;
				while( (read = is.read(buffer)) >= 0){
				   digest.update(buffer, 0, read);
				}		
				is.close();
				md5sum = digest.digest();
			}catch(Exception e){
				e.printStackTrace();
			}
			String md5sumHex = new BigInteger(1, md5sum).toString(16);
			return md5sumHex;
		
		
	}
	

}

