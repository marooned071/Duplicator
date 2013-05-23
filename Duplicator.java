import java.awt.EventQueue;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class Duplicator implements Observer, Runnable{
	
	private static final String OS = System.getProperty("os.name").toLowerCase();
	private static final String DEPO_NAME = "save";
	
	private GUI gui;
	private FileInfoSet fis; //- zbior informacji o plikach 
	private List<List<FileInfo>> listOfDuplicatsLists; //-lista list duplikatow
	
	private BlockingQueue<Transporter> queueObvserver; //kolejka obserwatorow
	
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable(){
			public void run(){
				GUI gui = new GUI();
				Duplicator d = new Duplicator(gui);
				Thread t = new Thread(d);
				t.start();
			}
		});

	}
	
	public Duplicator(GUI gui){
		this.gui=gui;
		gui.addObserver(this);
		queueObvserver=new ArrayBlockingQueue<Transporter>(100);
	}
	/**
	 * Klasa sluzaca "transportowi" danych z Obserwowanych do metody run, ktora zarzadza tym co sie dzieje
	 * @author Piotrek
	 *
	 */
	private class Transporter{
		private Observable arg0; 
		private Object arg1;
		Transporter(Observable arg0, Object arg1){
			this.arg0=arg0;
			this.arg1=arg1;
		}
		public Observable getArg0(){
			return arg0;
		}
		public Object getArg1(){
			return arg1;
		}
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		Transporter t= new Transporter(arg0,arg1);
		try {
			queueObvserver.put(t);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	@Override
	public void run() {
		Transporter t;
		Observable arg0;
		Object arg1;
		while(true){
			try {
				t=queueObvserver.take();
				arg0= t.getArg0();
				arg1=t.getArg1();
				gui.setButtonsEnable(false); //przy kazdym wyszukiwaniu, dodawania, zamykaniu itp (akcji na ktora czeka uzytkownik) blokuj przyciski
				if(arg0.equals(gui.getButtonChooseFileActionListener())){ //jesli powiadomienie pochodzi od listenera Przycisku wybierania plikow
					String startDirectory = (String) arg1;
					start(startDirectory); //rozpocznij nowe wyszukiwanie plikow
				}
				else if(arg0.equals(gui.getButtonDeleteFileActionListener())){//jesli powiadomienie pochodzi od listenera Przycisku usuwania plikow
					FileInfo deleteFile = (FileInfo) arg1;
					delete(deleteFile);
				}
				else if(arg0.equals(gui.getWindowAdapter())){//jesli powiadomienie pochodzi od listenera przycisku wyjscia (frame zostalo zamkniete)
					System.out.println("EXIT");
					exit();
				}
				else{
					System.out.println("Obserwatpr:\nCos innego!");
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			}
			
		}
		
	}	

	private void start(String startDirectory){
		
		if(fis!=null){//wczesniej bylo wyszukiwanie - zapisz do pliku
			gui.setLabelInfoText("Zapis do pliku..");
			writeFISToXML(getDepoFile(fis.getStartDirectory())); //w fis.getStartDirectory() jest adres katalogu startowego z poprzedniego wyszukiwania
		}
		
		File depoFile = getDepoFile(startDirectory);
		fis=readFromXML(depoFile);
		
		File startDirectoryFile = new File(startDirectory);
		System.out.println("DL FOLDERU : "+startDirectoryFile.length());
		if(startDirectoryFile.list().length<=0){ //sprawdza czy plik nie jest pusty
			System.out.println("Folder jest pusty");
			gui.setList_duplicateMainList(listOfDuplicatsLists = new ArrayList<List<FileInfo>>()); //jesli jest pusty ustawia jako liste listy pusta liste list
			return;
		}
		if(fis==null){
			gui.setLabelInfoText("Generowanie nowej listy...");
			
			System.out.println("Plik xml wybranego folderu nie istnieje:");
			fis = new FileInfoSet(startDirectoryFile); //tworzenie nowego zbioru plikow rozpoczynajacego sie w miejscu startDirectory.
			

		}
		else{
			gui.setLabelInfoText("Odczyt z pliku..");
			System.out.println("Plik xml wybranego folderu istnieje:");
			fis= new FileInfoSet(startDirectoryFile,fis);
		}
		
		listOfDuplicatsLists=fis.getListOfLists(); //lista list duplikatow
		
		gui.setList_duplicateMainList(listOfDuplicatsLists); //dodanie lisy list duplikatow do GUI
		

		
	}
	
	private void delete(FileInfo fileToDelete){
		
		fis.delete(fileToDelete); //usuwam plik ze zbioru plikow
		
		listOfDuplicatsLists=fis.getListOfLists(); //pobiera nowa lista duplikatow
		
		gui.setList_duplicateMainList(listOfDuplicatsLists); //przekazuje nowa liste duplikatow do GUI
	}
	
	private void exit(){
		String startDirectory;
		try{ //sprawdza czy plik jest pusty
			startDirectory=fis.getStartDirectory();
		}
		catch(NullPointerException e){return;};
		File file = getDepoFile(startDirectory);
		writeFISToXML(file);
		
		System.exit(0);
		
		
	}
	/**
	 * Zapisuje FileInfoSet do pliku XML
	 * @param file - plik w ktorym zostanie zapisany stan FileinfoSet
	 */
	private void writeFISToXML(File file){
		XMLEncoder e;
		
		try {
			e = new XMLEncoder(new BufferedOutputStream(
					new FileOutputStream(file)));
			e.writeObject(fis);
			e.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.out.println("Eksport do pliku: "+file.getAbsolutePath()+" zostal zakonczony pomyslnie:");
	}
	
	/**
	 * Wczytuje z pliku XML fileToread obiekt FileInfoSet i zwraca go.
	 * @param fileToRead
	 * @return fileInfoSet lub null jesli plik nie istnieje
	 */
	private FileInfoSet readFromXML(File fileToRead){
		FileInfoSet oldFis=null;
		
		if(fileToRead.exists()){
			System.out.println("zapisany wczesniej plik istenie!");
			XMLDecoder d;
			try {
				d = new XMLDecoder(
				                 new BufferedInputStream(
				                   new FileInputStream(fileToRead)));
				oldFis = (FileInfoSet) d.readObject();
			    d.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		}else{
			System.out.println("zapisany wczesniej NIE istnieje istenie!");
			return null;
		}
		return oldFis;

	}
	
	
	
	/**
	 * Funkcja zamienieniajaca sciezke do pliku na nazwe pliku. 
	 * W systemach Windows zamienia "\" na $ oraz ":" na "^"
	 * W systemach Unix zamienia "/" na $ oraz ":" na "^"
	 * @param path - sciezka do pliku
	 * @param directory - katalog w do/z ktorego jest dokonywany zapis/odczyt
	 * @return - nazwa pliku zawierajaca sciezke
	 */
	private File getDepoFile(String path){
		
		File saveDirectory = new File(DEPO_NAME);
		
		if(!saveDirectory.exists()){ //sprawdza czy plik do ktorego jest dokonywany zapis/odczyt istnieje
			File saves = new File(DEPO_NAME);
			saves.mkdir();
		}
		String savePath = saveDirectory.getAbsolutePath();
		
		String fileName;
		
		if(isWindows()){ //zamienia znaki ktorych nie moze zawierac nazwa pliku 
			fileName=path.replace("\\", "$").replace(":", "^");
			fileName=savePath+"\\"+fileName+ ".xml";
		}
		else if(isUnix()){
			fileName=path.replace("/", "$");
			fileName=savePath+"/"+fileName+ ".xml";
		}
		else{
			System.out.println("Twoj system nie jest wspierany! Nie mozna zapisac do pliku!");
			return null;
		}
		System.out.println("pathToname: "+fileName);
		return new File(fileName);
	}
	
	private static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}
	private static boolean isUnix() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
	}
}
