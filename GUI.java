import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class GUI extends JFrame {
	private JButton button_chooseFile; //przycisk uruchamiajacy wybor folderow
	private JButton button_deleteFile;
	private JLabel label_destination; //label z lokalizacja pliku
	private JLabel label_info; //label z Informacja
	private JFileChooser chooser; //komponent wybierajacy plik
	private GUI gui;
	
	private JList<String> list_duplicateMainList; //lista plikow ktore posiadaja duplikaty
	private JList<String> list_duplicateDetailsList; //lista plikow ktora sa duplikatami
	
	private ButtonChooseFileActionListener listener_buttonChooseFile;
	private ButtonDeleteFileActionListener listener_buttonDeleteFile;
	private ListDuplicateMainListSelectionListener listener_listMainList;
	private WindowAdapter windowAdapter;
	
	private List<List<FileInfo>> listOfDuplicatsLists;
	
	private String directoryPath;
	
	private int lastSelectedMainListIndex=-1; //index ostatniego elementu z gornej listy z ktorego zostalo usuniete
	
	
	GUI(){
		this.setSize(400, 500);		
		this.setLayout(null);

		gui=this;
		
		//chooser = new JFileChooser("D:\\Programowanie\\zzz_proby");
		chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		button_chooseFile=new JButton("Wybierz folder");
		button_chooseFile.setBounds(25,5,150,25);	
		listener_buttonChooseFile=new ButtonChooseFileActionListener();
		button_chooseFile.addActionListener(listener_buttonChooseFile);
		
		button_deleteFile=new JButton("Usun plik");
		button_deleteFile.setBounds(25, 435, 150, 25);
		listener_buttonDeleteFile=new ButtonDeleteFileActionListener();
		button_deleteFile.addActionListener(listener_buttonDeleteFile);
		
		label_destination=new JLabel("Wybrana lokacja:");
		label_destination.setBounds(25,30,375,20);
		
		label_info=new JLabel("",JLabel.RIGHT);
		label_info.setBounds(200, 435, 175, 25);
		
		list_duplicateMainList = new JList<String>();
		JScrollPane scrollPane_MainList = new JScrollPane(list_duplicateMainList);
		scrollPane_MainList.setBounds(25, 50, 350, 180);
		
		listener_listMainList=new ListDuplicateMainListSelectionListener();
		list_duplicateMainList.addListSelectionListener(listener_listMainList);
		list_duplicateMainList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		list_duplicateDetailsList = new JList<String>();
		JScrollPane scrollPane_DetailsList = new JScrollPane(list_duplicateDetailsList);
		scrollPane_DetailsList.setBounds(25, 250, 350, 180);
		
		
		this.add(scrollPane_MainList);
		this.add(scrollPane_DetailsList);
		this.add(button_chooseFile);
		this.add(label_destination);
		this.add(button_deleteFile);
		this.add(label_info);
		

		this.setVisible(true);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setVisible(true);  
		
		windowAdapter=new WindowAdapter();
		this.addWindowListener(windowAdapter);
		
		
		
	}
	/**
	 * Dodaje liste duplikatow do gornej listy.
	 * @param listOfDuplicatsLists - lista list z duplikatami
	 */
	public void setList_duplicateMainList(List<List<FileInfo>> listOfDuplicatsLists){
		setButtonsEnable(true); //odblokuj przyciski
		this.listOfDuplicatsLists=listOfDuplicatsLists;
		String[] stringList = new String[listOfDuplicatsLists.size()];
		List<FileInfo> detailsList;
		for(int i=0;i<listOfDuplicatsLists.size();i++){
			detailsList=listOfDuplicatsLists.get(i);
			stringList[i]=detailsList.get(0).getNameAndPath(); //reprezentowac liste duplikatow danego pliku bedzie plik ktory ma pierwsze miejsca na swojej liscie z duplikatami.
			
		}
		list_duplicateMainList.setListData(stringList);
		label_info.setText("Znaleziono "+listOfDuplicatsLists.size()+" plikow.");
		setDetailsList(lastSelectedMainListIndex); //dodaje elementy do listy dolnej lastSelectedMainListIndex- to ostatnio zaznaczna pozycja w gornej liscie
		
	}
	
	/**
	 * Metoda dodaje obserwatorow.
	 * Program kozysta ze wzorca projektowego Obserwator.
	 * @param o
	 */
	public void addObserver(Observer o){
		listener_buttonChooseFile.addObserver(o); //komponent wybierania folderu
		listener_buttonDeleteFile.addObserver(o); //przycisk usuwania
		windowAdapter.addObserver(o); //nadsluchiwacz przycisku wyjscia (x)
	}
	
	public ButtonChooseFileActionListener getButtonChooseFileActionListener(){
		return listener_buttonChooseFile;
	}
	
	public ButtonDeleteFileActionListener getButtonDeleteFileActionListener(){
		return listener_buttonDeleteFile;
	}
	
	public WindowAdapter getWindowAdapter(){
		return windowAdapter;
	}
	
	public void setButtonsEnable(boolean b){
		button_chooseFile.setEnabled(b);
		button_deleteFile.setEnabled(b);
	}
	
	public void setLabelInfoText(String s){
		label_info.setText(s);
	}
	
	
	/**
	 * Wstawia elementy do dolnej listy 
	 * @param lastMainListSelectedIndex - indeks zaznaczonego elementu (kliknieciem) lub indeks ostatniego zaznaczonego elementu gornej listy
	 */
	private void setDetailsList(int lastMainListSelectedIndex){ 
		if(lastMainListSelectedIndex<0){//gdy lastMainListSelectedIndex  == -1 oznacza to, ze nie bylo nic zaznaczone, 
			String[] listData = new String[0]; //albo nie powinno byc poniewaz ostatni duplikat zostal usuniety
			list_duplicateDetailsList.setListData(listData); //dolna lista jest wtedy pusta 
			return;
		}

		list_duplicateMainList.setSelectedIndex(lastMainListSelectedIndex); //w gornej liscie zaznaczamy ostatni element ktory byl zaznaczony 
		System.out.println("setDetailsListIndex: "+lastMainListSelectedIndex);
		int size=listOfDuplicatsLists.get(lastMainListSelectedIndex).size();
		List<FileInfo> detailsList=listOfDuplicatsLists.get(lastMainListSelectedIndex);		//pobieramy liste duplikatow pliku zaznaczonego w gornej liscie
		String[] listData= new String[size];
		
		for(int i=0;i<size;i++){
			listData[i]=detailsList.get(i).getNameAndPath();
		}
		
		list_duplicateDetailsList.setListData(listData);
	}
	
	private class ButtonChooseFileActionListener extends Observable implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("File Choose");
			chooser.showOpenDialog(gui);
			try{
				directoryPath=chooser.getSelectedFile().getPath();
			}
			catch(NullPointerException ex){
				System.out.println("Nie wybrano folderu.");
				return;
			}
			label_destination.setText(directoryPath); //ustawienie labela z lokacja folderu
			label_info.setText(directoryPath);
			System.out.println("Wybrany folder:"+directoryPath);
			setChanged();
			this.notifyObservers(directoryPath);
		

			
		}
	}
	
	private class ButtonDeleteFileActionListener extends Observable implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("Przycisk usuwania!");
			if(!list_duplicateDetailsList.isSelectionEmpty()){ //jesli nie jest nic zaznaczone, nie mozna nic usunac
				
				int selection = JOptionPane.showConfirmDialog(gui, "Na pewno chcesz usunac ten plik?\nOperacji nie mozna cofnac.", "Usuwanie pliku.", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if(selection==JOptionPane.NO_OPTION){
					System.out.println("rezygnacja z usuwania");
					return;
				}
				
				int index =list_duplicateDetailsList.getSelectedIndex();
				int mainListIndex=list_duplicateMainList.getSelectedIndex();
				FileInfo toDelete=listOfDuplicatsLists.get(mainListIndex).get(index);
				System.out.println("Usuwam:"+toDelete);
				if(listOfDuplicatsLists.get(mainListIndex).size()>2){ //jesli w dolnej liscie sa wiecej niz 2 elementy to zapamietaj gorne zaznaczenie. (jesli sa <=2 el to po usunieciu obiekt z gorenej listy znika)
					lastSelectedMainListIndex=mainListIndex; 
				}
				else //jesli na dolnej liscie sa dwa elementy - po usunieciu zostanie jeden czyli nie ma duplikatow
					lastSelectedMainListIndex=-1; 
				
				System.out.println("lastSelectedMainListIndex= "+lastSelectedMainListIndex);
				setChanged();
				this.notifyObservers(toDelete);
			}
			
		}
	}

	
	private class ListDuplicateMainListSelectionListener implements ListSelectionListener{

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			if(!arg0.getValueIsAdjusting()){
				int index=list_duplicateMainList.getSelectedIndex();
				setDetailsList(index);
			}

		}
		
	}

	private class WindowAdapter extends Observable implements WindowListener{

		@Override
		public void windowClosing(WindowEvent e) {
			// TODO Auto-generated method stub
			if(directoryPath==null){
				System.exit(0);
				return;
			}
			System.out.println("Okno zamykane.");
			label_info.setText("Zapis do pliku...");
			setButtonsEnable(false);
			setChanged();
			this.notifyObservers();
		}

		
		@Override
		public void windowActivated(WindowEvent e) {}
		@Override
		public void windowClosed(WindowEvent e) {}
		@Override
		public void windowDeactivated(WindowEvent e) {}
		@Override
		public void windowDeiconified(WindowEvent e) {}
		@Override
		public void windowIconified(WindowEvent e) {}
		@Override
		public void windowOpened(WindowEvent e) {}

	}
	
}
