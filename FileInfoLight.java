import java.math.BigInteger;
/**
 * Klasa rozszerzajaca klase FileInfo.
 * 	Zawierajaca podstawowe informacje o plikach:
 * - nazwa
 * - sciekza
 * - rozmiar
 * - data modyfikacji
 *  Rozni sie od klasy FileInfo tym, ze nie generuje hasha MD5 (co jest czasochlonne). 
 * @author Piotr Byliniak
 *
 */
public class FileInfoLight  extends FileInfo implements Comparable<FileInfo>{
	
	private String name;
	private String path;
	private int size;
	private long dateModify;


	FileInfoLight(String name, String path, long size, long dateModify){
		this.name=name;
		this.path=path;
		this.size=(int) size;
		this.dateModify=dateModify;
		
	}
	
	@Override
	/**
	 * Metoda porownywujaca dwa obiekty wzgledem dat modyfikacji. 
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
	 * Metoda porownywujaca dwa obiekty wzgledem dat modyfikacji. 
	 * UWAGA: drugi obiekt jest typu FileInfo.
	 * @param other
	 * @return
	 */
	public int compareTo(FileInfo other) {
		BigInteger a=BigInteger.valueOf(this.getDateModify());
		BigInteger b=BigInteger.valueOf(other.getDateModify());
		int result = a.compareTo(b);
		if(result==0){
			return path.compareTo(other.getPath());
		}
		return result;
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

	@Override
	public String toString() {
		String s=super.toString();
	    return s + "(" + name + ", " +
	    path + ", " +
        size + ", " +
        dateModify + ")";
	}

}
