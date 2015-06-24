package de.arnreich.seltotxt;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.JTable;

public abstract class FileFkts {
	static File dest = null;
	
	static public File getSource()
	{
		JFileChooser fc = new JFileChooser();
		FileFilter filter = new FileFilter()
		{
			@Override
			public String getDescription()
			{
				return "Access-Datenbanken";
			}
			
			@Override
			public boolean accept( File f )
			{
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".mdb");
			}
		};
		fc.setFileFilter( filter );
		
		int i = fc.showOpenDialog(null);
		if(i == JFileChooser.APPROVE_OPTION)
		{
			return fc.getSelectedFile();
		}
		else
		{
			return null;
		}
	}
	
	static public File getDest()
	{
		JFileChooser fc = new JFileChooser();
		FileFilter filter = new FileFilter()
		{
			@Override
			public String getDescription()
			{
				return "Text-Dateien";
			}
			
			@Override
			public boolean accept( File f )
			{
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".doc") || f.getName().toLowerCase().endsWith(".dat") || f.getName().toLowerCase().endsWith(".txt");
			}
		};
		fc.setFileFilter( filter );
		int i = fc.showSaveDialog( null );
		if(i == JFileChooser.APPROVE_OPTION)
		{
			File temp = fc.getSelectedFile();
			if( !temp.getName().toLowerCase().endsWith(".doc") && !temp.getName().toLowerCase().endsWith(".dat") && !temp.getName().toLowerCase().endsWith(".txt"))
				temp = new File(temp.getAbsoluteFile()+".txt");
			return temp;
		}
		else
		{
			return null;
		}
	}
	                                    //true für Einzel, false für Staffel
	static public void export(JTable tbl, boolean einz, boolean oneFile, int urkProStf)  
	{
		try
		{
			Writer fw = null;
			if( !dest.exists() )
			{
		    	  fw = new FileWriter( dest );
		    	  fw.write( "KLASSE;DISZIPLIN;VORNAME;HAUSNAME;JG;VEREIN;PLATZ;PZ;LEISTUNG;ORT;DATUM;DATUM LANG;DISZ.M.LAUF;NAME1;NAME2;NAME3;NAME4;VORNAME1;VORNAME2;VORNAME3;VORNAME4\r\n" );
		    }
		    else
		    {
		    	  fw = new FileWriter( dest, true );
		    }
			
			Writer fw2 = null;
			if(!oneFile && !einz && tbl.getRowCount()>0)
			{
				String new_filename = "stf_"+FileFkts.dest.getName();
				File dest2 = new File(FileFkts.dest.getParent()+"/"+new_filename);
				if(!dest2.exists())
				{
			    	  fw2 = new FileWriter( dest2 );
			    	  fw2.write( "KLASSE;DISZIPLIN;VORNAME;HAUSNAME;JG;VEREIN;PLATZ;PZ;LEISTUNG;ORT;DATUM;DATUM LANG;DISZ.M.LAUF;NAME1;NAME2;NAME3;NAME4;VORNAME1;VORNAME2;VORNAME3;VORNAME4\r\n" );
			    }
			    else
			    {
			    	  fw2 = new FileWriter( dest2, true );
			    }
			}
			
			for(int i=0; i<tbl.getRowCount(); i++)
			{
				if(einz)  //Also Einzeldisziplinen
				{
					boolean exp= new Boolean(tbl.getValueAt(i, 6).toString());
					if( exp )
					{
						Dataset_einz temp = (Dataset_einz) tbl.getValueAt(i, 7);
						fw.write( temp.klasse+";" );
				    	fw.write( temp.eventname1+";" );
				    	fw.write( temp.vname+";" );
				    	fw.write( temp.name+";" );
				    	fw.write( temp.jg+";" );
				    	fw.write( temp.verein+";" );
				    	fw.write( temp.rank2+";" );
				    	fw.write( temp.rank1+";" );
				    	fw.write( temp.leistung+";" );
				    	fw.write( temp.ort+";" );
				    	fw.write( temp.dat1+";" );
				    	fw.write( temp.dat2+";" );
				    	fw.write( temp.eventname2+";;;;;;;;" );
					}
					if((i!=(tbl.getRowCount()-1) || oneFile) && exp )
					{
						fw.write("\r\n");
					}
				}
				else
				{
					boolean exp= new Boolean(tbl.getValueAt(i, 5).toString());
					if( exp )
					{
						Dataset_stf temp = (Dataset_stf) tbl.getValueAt(i, 6);
						Writer writer;
						if(oneFile)
							writer=fw;
						else
							writer=fw2;
						for( int k=0; k<urkProStf; k++)
						{
							writer.write( temp.klasse+";" );
							writer.write( temp.eventname+";;;;" );
							writer.write( temp.verein+";" );
							writer.write( temp.rank2+";" );
							writer.write( temp.rank1+";" );
							writer.write( temp.leistung+";" );
							writer.write( temp.ort+";" );
							writer.write( temp.dat1+";" );
							writer.write( temp.dat2+";" );
							writer.write( temp.eventname+";" );
							writer.write( temp.names[0]+";" );
							writer.write( temp.names[1]+";" );
							writer.write( temp.names[2]+";" );
							writer.write( temp.names[3]+";" );
							writer.write( temp.vnames[0]+";" );
							writer.write( temp.vnames[1]+";" );
							writer.write( temp.vnames[2]+";" );
							writer.write( temp.vnames[3] );
							
							if(i!=(tbl.getRowCount()-1) || k<(urkProStf-1))
							{
								if(oneFile)
									fw.write("\r\n");
								else
									fw2.write("\r\n");
							}
						}
					}
				}
			}
			fw.close();
			fw2.close();
		}
		catch (Exception e){}
		
	}
}
