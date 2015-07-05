package de.arnreich.seltotxt;

import javax.swing.*;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

public class Main extends JFrame 
{
	static final long serialVersionUID = 1L;
	public TrayIcon icon = null;
	public SystemTray tray;
	private javax.swing.Timer t;
	public MyTableModel model;
	public MyTableModelStf model_stf;
	public JTable table, table_stf;
	private TableRowSorter<MyTableModel> sorter;
	private TableRowSorter<MyTableModelStf> sorter_stf;
	public boolean getAll=false; //Für "alle Urkunden suchen"
	public SpinnerNumberModel model_dis=null, sp_model_stf=null;
	public JComboBox<String> cb_seek=null;
	public ArrayList<Dataset_einz> data_einz = new ArrayList<Dataset_einz>();
	public ArrayList<Dataset_stf> data_stf = new ArrayList<Dataset_stf>();
	private JComboBox<String> cb_sort;
	private JCheckBox chkb_onefile, chkb_print;
	public int newEvents = 0; //Wie viele neue Datensätze liegen vor? (Zur Kontrolle bei Schreiben des Status)
	private boolean setStatusOnExport=true;
	
	public void sort()
	{
		ArrayList<RowSorter.SortKey> list = null;
		ArrayList<RowSorter.SortKey> list_stf = null;
		switch (cb_sort.getSelectedIndex())
		{
			case 0:  //Wettbewerb und Platz
			{
				list = new ArrayList<RowSorter.SortKey>(3);
				list.add(new javax.swing.RowSorter.SortKey(1,javax.swing.SortOrder.ASCENDING) );
				list.add(new javax.swing.RowSorter.SortKey(2,javax.swing.SortOrder.ASCENDING) );
				list.add(new javax.swing.RowSorter.SortKey(5,javax.swing.SortOrder.ASCENDING) );
				list_stf = new ArrayList<RowSorter.SortKey>(3);
				list_stf.add(new javax.swing.RowSorter.SortKey(1,javax.swing.SortOrder.ASCENDING) );
				list_stf.add(new javax.swing.RowSorter.SortKey(2,javax.swing.SortOrder.ASCENDING) );
				list_stf.add(new javax.swing.RowSorter.SortKey(4,javax.swing.SortOrder.ASCENDING) );
				break;
			}
			case 1: //Name
			{
				list = new ArrayList<RowSorter.SortKey>(1);
				list.add(new javax.swing.RowSorter.SortKey(0,javax.swing.SortOrder.ASCENDING) );
				list_stf = new ArrayList<RowSorter.SortKey>(1);
				list_stf.add(new javax.swing.RowSorter.SortKey(0,javax.swing.SortOrder.ASCENDING) );
				break;
			}
			case 2: //Verein und Klasse
			{
				list = new ArrayList<RowSorter.SortKey>(2);
				list.add(new javax.swing.RowSorter.SortKey(3,javax.swing.SortOrder.ASCENDING) );
				list.add(new javax.swing.RowSorter.SortKey(1,javax.swing.SortOrder.ASCENDING) );
				list_stf = new ArrayList<RowSorter.SortKey>(2);
				list_stf.add(new javax.swing.RowSorter.SortKey(0,javax.swing.SortOrder.ASCENDING) );
				list_stf.add(new javax.swing.RowSorter.SortKey(1,javax.swing.SortOrder.ASCENDING) );
				break;
			}
			case 3: //Platz
			{
				list = new ArrayList<RowSorter.SortKey>(1);
				list.add(new javax.swing.RowSorter.SortKey(5,javax.swing.SortOrder.ASCENDING) );
				list_stf = new ArrayList<RowSorter.SortKey>(1);
				list_stf.add(new javax.swing.RowSorter.SortKey(4,javax.swing.SortOrder.ASCENDING) );
				break;
			}
		}
		
		sorter.setSortKeys(list);
		sorter_stf.setSortKeys(list_stf);
		sorter.sort();
		sorter_stf.sort();

	}
	
	Main(String titel)
	{
		super(titel);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(650,500);
		this.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		final Main f = this;
		//Tray Icon
//		if(SystemTray.isSupported())
//		{
//			tray = SystemTray.getSystemTray();
//			Image img = new ImageIcon( ClassLoader.getSystemResource("img/new.gif") ).getImage();
//			icon = new TrayIcon(img, "Sel2Txt");
//			icon.setImageAutoSize(true);
//		}
		//Ende TrayIcon
		
		//Toolbar
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		
		JButton b_showAll = new JButton("Alle Urkundendaten anzeigen");
		b_showAll.addActionListener( new ActionListener()
			{
				public void actionPerformed(ActionEvent e) 
				{
					if(DbFkts.file!=null)
					{
						getAll=true;
						f.setStatusOnExport=false;
						DbFkts.getData(f);
						getAll=false;
					}
					else
					{
						JOptionPane.showMessageDialog(null, "Bitte zuerst eine Quelledatei auswählen!", "Keine Quelle ausgewählt!", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		);
		toolbar.add(b_showAll);
		
		JButton b_about = new JButton("Über...");
		b_about.addActionListener( new ActionListener()
			{
				public void actionPerformed(ActionEvent e) 
				{
					showAbout(f);
				}
			}
		);
		toolbar.add(b_about);
		
		JButton b_exit = new JButton("Ende");
		b_exit.addActionListener( new ActionListener()
			{
				public void actionPerformed(ActionEvent e) 
				{
					System.exit(0);
				}
			}
		);
		toolbar.add(b_exit);

		//Ende Toolbar
		
		//Panel: Aktionen (unten)
		JPanel p_actions = new JPanel();
		JButton b_search = new JButton("Suchen");
		b_search.addActionListener( new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0) {
					if(DbFkts.file!=null)
					{
						f.setStatusOnExport=true;
						DbFkts.getData(f);
					}
					else
					{
						JOptionPane.showMessageDialog(null, "Bitte zuerst eine Quelledatei auswählen!", "Keine Quelle ausgewählt!", JOptionPane.ERROR_MESSAGE);
					}					
				}
			}
		);
		
		/*JButton b_search_export = new JButton("Suchen und Exportieren");
		b_search_export.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0) {
				DbFkts.setStatus(f);
			}
		}
		);*/
		JButton b_export = new JButton("Exportieren");
		b_export.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0) {
					if(FileFkts.dest!=null)
					{
						boolean oneFile = chkb_onefile.isSelected();
						FileFkts.export(table, true, oneFile,0);
						FileFkts.export(table_stf, false, oneFile,f.sp_model_stf.getNumber().intValue());
						
						if(f.setStatusOnExport)
						{	
							DbFkts.setStatus(f);
						}
						if(f.chkb_print.isSelected())
							DbFkts.setStatus(f);
						JOptionPane.showMessageDialog(null, "Die Urkundendaten wurden erfolgreich exportiert!", "Erfolg", JOptionPane.INFORMATION_MESSAGE);
						
						DbFkts.getData(f);
						
						//Icon aktualiesieren
						for(ActionListener a: t.getActionListeners())
							a.actionPerformed(null);
					}
					else
					{
						JOptionPane.showMessageDialog(null, "Bitte zuerst eine Zieldatei auswählen!", "Keine Quelle ausgewählt!", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		);
		p_actions.add(b_search);
		//p_actions.add(b_search_export);
		p_actions.add(b_export);
		//Ende Panel: Aktionen
		
		//Tabelle - Einzel
		model = new MyTableModel();
		table = new JTable(model);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sorter = new TableRowSorter<MyTableModel>( model );
		table.getColumnModel().getColumn(7).setMaxWidth(0);
		table.getColumnModel().getColumn(7).setMinWidth(0);
		table.getColumnModel().getColumn(7).setPreferredWidth(0);
		table.setRowSorter(sorter);
		JScrollPane sp = new JScrollPane(table);
		JPanel p_table = new JPanel();
		p_table.setLayout(new BorderLayout(2,2));
		p_table.add(new JLabel(" AusgewÃ¤hlte Daten - Einzel:"), BorderLayout.PAGE_START);
		p_table.add(sp);
		//Ende Tabelle - Einzel
		
		//Tabelle - Stf
		model_stf = new MyTableModelStf();
		table_stf = new JTable(model_stf);
		table_stf.setRowSelectionAllowed(true);
		table_stf.setColumnSelectionAllowed(true);
		table_stf.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sorter_stf = new TableRowSorter<MyTableModelStf>( model_stf );
		table_stf.getColumnModel().getColumn(6).setMaxWidth(0);
		table_stf.getColumnModel().getColumn(6).setMinWidth(0);
		table_stf.getColumnModel().getColumn(6).setPreferredWidth(0);
		table_stf.setRowSorter(sorter_stf);
		ArrayList<RowSorter.SortKey> list_stf = new ArrayList<RowSorter.SortKey>(1);
		list_stf.add(new javax.swing.RowSorter.SortKey(0,javax.swing.SortOrder.ASCENDING) );
		sorter_stf.setSortKeys(list_stf);
		sorter_stf.sort();
		JScrollPane sp_stf = new JScrollPane(table_stf);
		JPanel p_table_stf = new JPanel();
		p_table_stf.setLayout(new BorderLayout(2,2));
		p_table_stf.add(new JLabel(" Ausgewählte Daten - Staffel:"), BorderLayout.PAGE_START);
		p_table_stf.add(sp_stf);
		//Ende Tabelle - Stf
		
		JSplitPane sp_tbl =  new JSplitPane(JSplitPane.VERTICAL_SPLIT, p_table, p_table_stf);
		sp_tbl.setOneTouchExpandable( true );
		sp_tbl.setDividerLocation( 235 );
		
		//Panel: Settings
		JPanel p_settings = new JPanel();
		GridBagLayout gbl_settings = new GridBagLayout();
		p_settings.setLayout(gbl_settings);
		
		addComponent( p_settings, gbl_settings, new JLabel(" Quelldatei:"), 0, 0, 1, 1, 0.2, 0.2 );
		final JLabel lbl_source = new JLabel(" Noch keine Quelle ausgewählt!");
		addComponent( p_settings, gbl_settings, lbl_source, 0, 1, 1, 1, 0.2, 0.2 );
		JButton b_source = new JButton(" Quelle Ändern");
		b_source.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0) {
					File source = FileFkts.getSource();	
					if(source!=null)
					{
						DbFkts.file = source;
						lbl_source.setText(" " + source.getName());
						for(ActionListener a: t.getActionListeners())
							a.actionPerformed(null);
					}
				}
			}
		);
		addComponent( p_settings, gbl_settings, b_source, 1, 0, 1, 2, 0.2, 0.2 );
		
		addComponent( p_settings, gbl_settings, new JLabel(" Zieldatei:"), 0, 2, 1, 1, 0.2, 0.2 );
		final JLabel lbl_dest = new JLabel(" Noch kein Ziel ausgewählt!");
		addComponent( p_settings, gbl_settings, lbl_dest, 0, 3, 1, 1, 0.2, 0.2 );
		JButton b_dest = new JButton(" Ziel ändern");
		b_dest.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0) {
					File dest = FileFkts.getDest();	
					if(dest!=null)
					{
						FileFkts.dest = dest;
						lbl_dest.setText(" " + dest.getName());
					}
				}
			}
		);
		addComponent( p_settings, gbl_settings, b_dest, 1, 2, 1, 2, 0.2, 0.2 );
		
		addComponent( p_settings, gbl_settings, new JLabel(" Suchen nach:"), 0, 4, 1, 1, 0, 0.2 );
		String[] opts_seek = {"Einzel", "Staffel", "Beides"};
		cb_seek = new JComboBox<String>(opts_seek);
		cb_seek.setSelectedIndex(2);
		addComponent( p_settings, gbl_settings, cb_seek, 1, 4, 1, 1, 0.2, 0.2 );
		
		addComponent( p_settings, gbl_settings, new JPanel(), 2, 0, 1, 5, 0.1, 0.2 );  //Platzhalter zw. Spalten
		
		addComponent( p_settings, gbl_settings, new JLabel("Urkunden pro Disziplin:"), 3, 0, 1, 1, 0.2, 0.2 );
		model_dis = new SpinnerNumberModel(8, 1, 99, 1);
		JSpinner spin_dis = new JSpinner(model_dis);
		addComponent( p_settings, gbl_settings, spin_dis, 4, 0, 1, 1, 0.2, 0.2 );
		
		addComponent( p_settings, gbl_settings, new JLabel("Urkunden pro Staffel/Mannschaft:"), 3, 1, 1, 1, 0.2, 0.2 );
		sp_model_stf = new SpinnerNumberModel(4, 1, 8, 1);
		JSpinner spin_stf = new JSpinner(sp_model_stf);
		addComponent( p_settings, gbl_settings, spin_stf, 4, 1, 1, 1, 0.2, 0.2 );
		
		addComponent( p_settings, gbl_settings, new JLabel("Sortieren nach:"), 3, 2, 1, 1, 0.2, 0.2 );
		String[] opts_sort = {"Wettbewerb (u. Platz)", "Name", "Verein (u. Klasse)", "Platz"};
		cb_sort = new JComboBox<String>(opts_sort);
		cb_sort.addActionListener( new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0) {
					f.sort();
				}
			}
		);
		addComponent( p_settings, gbl_settings, cb_sort, 4, 2, 1, 1, 0.2, 0.2 );
		
		chkb_print = new JCheckBox("Status nach Export auf \"Gedruckt\" setzten");
		chkb_print.setSelected(true);
		addComponent( p_settings, gbl_settings, chkb_print, 3, 3, 1, 1, 0.5, 0.2 );
		
		chkb_onefile = new JCheckBox("Einzel und Staffel in die selbe Datei exportieren");
		chkb_onefile.setSelected(true);
		addComponent( p_settings, gbl_settings, chkb_onefile, 3, 4, 1, 1, 0.5, 0.2 );
		
		addComponent( p_settings, gbl_settings, new JPanel(), 5, 0, 1, 6, 0.3, 0.2 ); //Platzhalter zum rechten Rand
		addComponent( p_settings, gbl_settings, new JPanel(), 0, 6, 6, 1, 0.1, 0.2 ); //Platzhalter zur Tabelle
		//Ende Panel: Setting
		
		//Layout fÃ¼r Frame
		Container c = this.getContentPane();
		GridBagLayout gbl = new GridBagLayout();
		c.setLayout(gbl);
		addComponent( c, gbl, toolbar, 0, 0, 1, 1, 0  , 0.02 ); //Toolbar
		addComponent( c, gbl, p_settings, 0, 1, 1, 1, 1.0, 0.14 ); 
		addComponent( c, gbl, sp_tbl, 0, 2, 1, 5, 0 , 0.82 );
		addComponent( c, gbl, new JPanel(), 0, 7, 1, 1, 0  , 0.01 ); //Platzhalter
	    addComponent( c, gbl, p_actions, 0, 8, 1, 1, 0.6  , 0.03 ); 
	    //Ende Layout fÃ¼r Frame
	    this.setVisible(true);
	}
	
	//Zum setzen der Elemente per GridBagLayout
	static void addComponent( Container cont, 
            GridBagLayout gbl, 
            Component c, 
            int x, int y, 
            int width, int height, 
            double weightx, double weighty ) 
	{ 
		GridBagConstraints gbc = new GridBagConstraints(); 
		gbc.fill = GridBagConstraints.BOTH; 
		gbc.gridx = x; gbc.gridy = y; 
		gbc.gridwidth = width; gbc.gridheight = height; 
		gbc.weightx = weightx; gbc.weighty = weighty; 
		gbl.setConstraints( c, gbc ); 
		cont.add( c ); 
	} 
	
	public static void main(String[] args) {
		final Main main = new Main("SeltectToText - Urkundenexport (by Daniel Arnreich)");
	
		main.t = new javax.swing.Timer( 10000, new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{			
				if(DbFkts.checkForUpdate())
				{
					if(main.tray != null) {
						try
						{
							main.tray.add(main.icon);
						}
						catch (AWTException ex)
						{
							System.err.println(ex);
						}
						catch (IllegalArgumentException ex)	{}
					}
				}
				else
				{
					if(main.tray != null) {
						main.tray.remove(main.icon);
					}
					//Image img = new ImageIcon( ClassLoader.getSystemResource("img/ok.gif") ).getImage();
					//main.icon.setImage(img);
				}
			}
		});
		main.t.start();
		DbFkts.file=null;
	}
	
	static public void showAbout(JFrame owner)
	{
		final JDialog d_about = new JDialog(owner, "Anleitung", true);
		d_about.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		d_about.setLayout( new BorderLayout(5,5) );
		JLabel text = new JLabel("<html>Diese Programm erzeugt aus der geladenen Datenbank eine Textdatei, mit der man per Seriendruck Urkunden erstellen kann.<br>(by Daniel Arnreich)</html>");
		d_about.add( text );
		JButton back = new JButton("Zurück");
		back.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						d_about.setVisible( false );
					}
				}	
		);
		d_about.add( back, BorderLayout.PAGE_END );
		d_about.setSize(250,150);
		d_about.setLocation(250,250);
		d_about.setVisible( true );
	}

}
