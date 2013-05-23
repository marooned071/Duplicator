import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;


public class FileInfoSet implements Serializable{
	
	private TreeSet<FileInfo> ts;
	private List<List<FileInfo>> listOfDuplicatsLists;
	private String startDirectory;
	
	/**
	 * Konstruktor pierwszy:
	 * Jest uruchamiany gdy folder dla ktorego tworzymy zestaw danych nie istnieje. 
	 * @param startDirectory -katalog w ktorym szukamy duplikatow
	 */
	public FileInfoSet(File startDirectory){
		ts=getTreeSetOfFileInfo(startDirectory); //Tworzy R.B.Tree zlozona z obiektor file info
		System.out.println("PIERWSZY KONSTRUKTOR");
		this.startDirectory=startDirectory.getAbsolutePath();
		
		listOfDuplicatsLists = getListOfDuplicatsLists();//lista list- lista list duplikatow
	}
		
	/**
	 * Konstruktor drugi: 
	 * Jest uruchamiany gdy folder dla ktorego tworzymy zestaw danych istnieje. 
	 * @param startDirectory -katalog w ktorym szukamy duplikatow
	 * @param oldSet -stary zestaw danych z XML (moze byc nieaktualny)
	 */
	public FileInfoSet(File startDirectory, FileInfoSet oldSet){
		System.out.println("DRUGI KONSTRUKTOR");
		TreeSet<FileInfo> xmlSet = oldSet.getTs(); // zbior FileInfo wczoytany z pliku XML
		
		class DateComparator implements Comparator<FileInfo>{
			@Override
			public int compare(FileInfo arg0, FileInfo arg1) {
				BigInteger a=BigInteger.valueOf(arg0.getDateModify());
				BigInteger b=BigInteger.valueOf(arg1.getDateModify());
				int result = a.compareTo(b);
				if(result==0){
					return arg0.getPath().compareTo(arg1.getPath());
				}
				return result;
			}
		}
		
		TreeSet<FileInfo> xmlDataSortedSet = new TreeSet<FileInfo>(new DateComparator()); //drzewo sortujace wg daty modyfikacji
		xmlDataSortedSet.addAll(xmlSet);  //zbior fileInfo (z XML) posortowany wg daty modifikacji
		
//		System.out.println("\nLiczba plikow w XML");
//		printSet(xmlDataSortedSet);
		
		
		TreeSet<FileInfoLight> directoryLight =getDirectoryDataSortedSet(startDirectory); //drzewo z plikow (te ktore sa ktore sa w katalogu) posortowwanych po datach	
		TreeSet<FileInfoLight> xmlLight= new TreeSet<FileInfoLight>(); //drzewo z plikow krtre sa w XML w wesji light
		Iterator<FileInfo> iteratorXmlDataSortedSet = xmlDataSortedSet.iterator();	
		while(iteratorXmlDataSortedSet.hasNext()){ //konwertuje cale xmlDataSorted (z pliku) na wersje light
			xmlLight.add(iteratorXmlDataSortedSet.next().converToLight());
		}
		
//		System.out.println("\nW XML:");
//		printSet(xmlLight);
//		
//		System.out.println("\nW katalogu:");
//		printSet(directoryLight);
		
		TreeSet<FileInfoLight> directoryMinusXml=new TreeSet<FileInfoLight>(directoryLight); 
		TreeSet<FileInfoLight> xmlMinusDirectory=new TreeSet<FileInfoLight>(xmlLight);
		
		
		directoryMinusXml.removeAll(xmlLight); //z tego co jest w folderze usuwamy te, ktore sa w xml - zostaja tylko te ktorych nie ma w xml - czyli te ktore uzytkownik sam dodal
//		System.out.println("\n Zostaly dodane:");
//		printSet(directoryMinusXml);
		
		
		xmlMinusDirectory.removeAll(directoryLight);//z tego co jest w xml usuwamy to co jest w folderze- czyli zostaja tylko te ktore uzytkownik sam usunal
//		System.out.println("\n Zostaly usuniete:");
//		printSet(xmlMinusDirectory);
		
		FileInfoLight fileInfoLight;
		FileInfo fileInfo;
		
		System.out.println("\nPliki ktore zostaly usuniete przez uzytkownika od ostatniego uruchomienia programu: "+xmlMinusDirectory.size());
		Iterator<FileInfoLight> iteratorRemove = xmlMinusDirectory.iterator();
		while(iteratorRemove.hasNext()){ // z zestawu danych usuwamy pliki ktorych juz nie ma w folderze
			fileInfoLight = iteratorRemove.next();
			fileInfo = (FileInfo) fileInfoLight;
			boolean b=xmlSet.remove(fileInfo);
			System.out.println(b+" usunieto: "+fileInfo.toString());
		}
		
		System.out.println("\nPliki ktore zostaly dodane przez uzytkownika od ostatniego uruchomienia programu: "+directoryMinusXml.size());
		Iterator<FileInfoLight> iteratorAdd = directoryMinusXml.iterator();
		while(iteratorAdd.hasNext()){ //do zestawu danych dodajemy pliki ktorych nie bylo w starym zestawie danych, a sa w katalogu
			fileInfoLight = iteratorAdd.next();
			fileInfo = new FileInfo(fileInfoLight.getName(),fileInfoLight.getPath(),fileInfoLight.getSize(),fileInfoLight.getDateModify());
			boolean b=xmlSet.add(fileInfo);
			System.out.println(b+" dodano: "+fileInfo.toString());
		}
		
		//printSet(xmlSet);
		
		ts=xmlSet;
		this.startDirectory=startDirectory.getAbsolutePath();
		listOfDuplicatsLists = getListOfDuplicatsLists();//lista list- lista list duplikatow
		
	}
	
	public static void printSet(TreeSet<?> treeSet){
		Iterator<?> it =treeSet.iterator();
		System.out.println("Size: "+treeSet.size());
		while(it.hasNext()){
				System.out.println(it.next().toString());
		}
		System.out.println();
	}
	
	public FileInfoSet(){
		
	}
	
	/**
	 * Metoda tworzaca z zbioru TreeSet (ts) liste list z duplikatami.
	 * Tzn: Metoda zwroci nam liste, ktora zawiera listy obiektow typu FileInfo ktore sa duplikatami.
	 * Jeden rodziaj duplikatu to jedna lista.  
	 * @return
	 */
	public List<List<FileInfo>> getListOfDuplicatsLists(){
		listOfDuplicatsLists = new ArrayList<List<FileInfo>>();
		Iterator<FileInfo> curentIterator=ts.iterator(); //iterator bierzacy
		Iterator<FileInfo> nextIterator=ts.iterator(); //iterator nastepny - zawsze jeden dalej niz bierzacy
		nextIterator.next();
		
		FileInfo curentFileInfo; //obiekt na ktory wskazuje iterator bierzacy
		FileInfo nextFileInfo; //obiekt na ktory wskazuje iterator nastepny
		
		boolean inRow=false; //czy sa takie same pod rzad
		ArrayList<FileInfo> duplicateList=null;
		while(nextIterator.hasNext()){
			curentFileInfo=curentIterator.next(); //element bierzacy (zaczyna od 0)
			nextFileInfo=nextIterator.next(); //element nastepny (zaczyna od 1)
			if(nextFileInfo.equalsContent(curentFileInfo)){ //jesli nastepny i bierzacy sa sobie rowne
				if(!inRow){ //jesli nie ma kontynuacji dodawanie do listy duplikatow (czyli ten element nie jest pod rzad)
					inRow=true; //nastepny bedzie pod rzad
					duplicateList = new ArrayList<FileInfo>(); //tworzy liste duplikatow
					duplicateList.add(curentFileInfo); //dodaje do niej bierzacy
					duplicateList.add(nextFileInfo); //dodaje nastepny
				}
				else{ //jesli jest juz lista dla tego pliku stworzona (czyli ten jest pod rzad)
					duplicateList.add(nextFileInfo); //dodaje plik do listy duplikatow
				}
				if(!nextIterator.hasNext()){ //jesli po tym dodanym nie ma juz nic, 
					listOfDuplicatsLists.add(duplicateList);//dodaje liste duplkatow do listy list
				}
				
	
			}
			else{ //jesli bierzacy i nastepny nie sa rozne
				if(inRow){ //jesli wczesniej byla seria duplikatow (element next juz do niej nienalezy)
					listOfDuplicatsLists.add(duplicateList); //dodaj liste duplikatow do listy list
				}
				inRow=false; // juz nie ma nic pod rzad
				
			}
		}
		return listOfDuplicatsLists;
	}
	
	public void delete(FileInfo toDelete){
		
		boolean b=ts.remove(toDelete);
		
		if(b){
			File fileToDelete = new File(toDelete.getPath());
			b=fileToDelete.delete();
			if(b){
				System.out.println("Usunieto z dysku: "+toDelete);
			}
		}
		listOfDuplicatsLists=getListOfDuplicatsLists();

	}
	
	public int getSize(){
		return ts.size();
	}
	
	
	public void printListOfDuplicatsLists(){
		for(int i=0;i<listOfDuplicatsLists.size();i++){
			System.out.println(i);
			for(int j=0;j<listOfDuplicatsLists.get(i).size();j++){
				System.out.println(listOfDuplicatsLists.get(i).get(j).toString());
			}
			
		}
	}
	
	
	/**
	 * Funkcja tworzaca posortowany zbior danych typu TreeSet z obiektami typu FileInfo.
	 * Drzewo sklada sie z informacji o plikach z katalogu startDirectory i wszystkich jego podkatalogach.
	 * @param startDirectory - katalog z ktorego zostana dodane wszystkie pliki.
	 * @return zbior TreeSet
	 */
	public TreeSet<FileInfo> getTreeSetOfFileInfo(File startDirectory){
		TreeSet<FileInfo> ts = new TreeSet<FileInfo>();
		makeTreeSetOfFileInfo(ts,startDirectory);
		return ts;
		
	}
	
	/**
	 * Metoda pomocnicza tworzenia zbioru danych. Jest wylowywana rekurencyjnie dla kazdego katalgu. Pliki dodawane sa do drzewa
	 * @param ts
	 * @param startDirectory
	 */
	private void makeTreeSetOfFileInfo(TreeSet<FileInfo> ts, File startDirectory){
		File[] listOfFiles =startDirectory.listFiles();
		for(File file : listOfFiles){
			if(file.isDirectory()){ //jesli file jest katalogiem wywolaj rekurencyjnie dla niego zczytywanie plikow
				makeTreeSetOfFileInfo(ts,file);
			}
			else{
				FileInfo fileInfo=new FileInfo(file.getName(),file.getAbsolutePath(),file.length(),file.lastModified());
				ts.add(fileInfo);
			}
		}
	}
	

	/**
	 * Funkcja tworzaca posortowany zbior danych typu TreeSet z obiektami typu FileInfoLight.
	 * Drzewo sklada sie z informacji o plikach z katalogu startDirectory i wszystkich jego podkatalogach.
	 * @param startDirectory - katalog z ktorego zostana dodane wszystkie pliki.
	 * @return zbior TreeSet
	 */
	public TreeSet<FileInfoLight> getDirectoryDataSortedSet(File startDirectory){
		TreeSet<FileInfoLight> tsl = new TreeSet<FileInfoLight>();
		makeDirectoryDataSortedSet(tsl,startDirectory);
		return tsl;
		
	}
	/**
	 * Metoda pomocnicza tworzenia zbioru danych. Jest wylowywana rekurencyjnie dla kazdego katalgu. Pliki dodawane sa do drzewa
	 * @param ts
	 * @param startDirectory
	 */
	private void makeDirectoryDataSortedSet(TreeSet<FileInfoLight> tsl, File startDirectory){
		File[] listOfFiles =startDirectory.listFiles();
		for(File file : listOfFiles){
			if(file.isDirectory()){
				makeDirectoryDataSortedSet(tsl,file);
			}
			else{
				FileInfoLight fileInfoLight=new FileInfoLight(file.getName(),file.getAbsolutePath(),file.length(),file.lastModified());
				tsl.add(fileInfoLight);
			}
				
		}
	}
	

	public TreeSet<FileInfo> getTs() {
		return ts;
	}

	public void setTs(TreeSet<FileInfo> ts) {
		this.ts = ts;
	}
	
	public void setListOfLists(List<List<FileInfo>> listOfDuplicatsLists) {
		this.listOfDuplicatsLists = listOfDuplicatsLists;
	}
	public List<List<FileInfo>> getListOfLists() {
		return listOfDuplicatsLists;
	}

	public String getStartDirectory() {
		return startDirectory;
	}

	public void setStartDirectory(String startDirectory) {
		this.startDirectory = startDirectory;
	}

	
	public String toString(){
		String s = super.toString();
		return s+ts.toString();
	}

}
