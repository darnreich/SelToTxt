package de.arnreich.seltotxt;

import java.io.File;
import java.sql.*;
import java.util.regex.Pattern;
import java.text.DecimalFormat;

public abstract class DbFkts 
{
	static File file = null;
	
	static boolean checkForUpdate()
	{
		if(file==null)
			return false;
		try
		{
			Class.forName(info.DRIVER);
		}
		catch(ClassNotFoundException e)
		{
			System.err.println( "Keine Treiber-Klasse!" );
		}
		boolean newData=false;
		Connection con = null;
		try
		{
			con = DriverManager.getConnection(info.PROTOCOL+file.getAbsoluteFile()); //, info.USER, info.PASS
			Statement stmt = con.createStatement();
			ResultSet res = stmt.executeQuery( "SELECT COUNT(*) AS rowcount FROM rounds WHERE intStatus=6 AND (strName='Finale' OR strName='Zeitläufe')" );
			res.next();
			if(res.getInt("rowcount")<1)
				newData=false;
			else
				newData=true;
			
			//stmt.executeUpdate( "CREATE TABLE test2 (wert char(25) not null)" );
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (con!=null)
				try{ con.close();} catch(SQLException e){e.printStackTrace();}	
		}
		return newData;
	}
	
	static void getData(Main m)
	{
		m.data_einz.clear();
		m.data_stf.clear();
		
		String disziplin, ort="", datum="", datum_lang="", disz_lang;
		int einz=-1, anz_stf=-1; //Laufnummern
		
		//Treiber laden
		try
		{
			Class.forName(info.DRIVER);
		}
		catch(ClassNotFoundException e)
		{
			System.err.println( "Keine Treiber-Klasse!" );
			return;
		}
		
		Connection con = null;
		
		try
		{
			con = DriverManager.getConnection(info.PROTOCOL+file.getAbsoluteFile()); //info.USER, info.PASS
			
				//Anzahl neuer Datensätze ermitteln
				Statement stmt = con.createStatement();
				String kritStatus;
				if(m.getAll)
				{
					kritStatus=">";
				}
				else
				{
					kritStatus="";
				}
				ResultSet res0 = stmt.executeQuery("SELECT COUNT(*) AS c FROM rounds WHERE intStatus"+kritStatus+"=6 AND (strName='Finale' OR strName='Zeitläufe')"); 
				res0.next();
				m.newEvents=res0.getInt("c");
				res0.close();
				
				//Wettkampf-Basis-Infos
				ResultSet res = stmt.executeQuery("SELECT * FROM competition");
				res.next();
				ort = res.getString("strPlace"); //Ort
				
				//Datum (in 2 Formen)
				Pattern p = Pattern.compile("[- ]");
				String[] temp = p.split( res.getString("datStart") );
				datum = temp[2] + "."+temp[1]+"."+temp[0];  //Datum 1
				String monat;
				if(temp[1].equals("01"))
				{
					monat="Januar";
				}
				else if(temp[1].equals("02"))
				{
					monat="Februar";
				}
				else if(temp[1].equals("03"))
				{
					monat="MÃ¤rz";
				}
				else if(temp[1].equals("04"))
				{
					monat="April";
				}
				else if(temp[1].equals("05"))
				{
					monat="Mai";
				}
				else if(temp[1].equals("06"))
				{
					monat="Juni";
				}
				else if(temp[1].equals("07"))
				{
					monat="Juli";
				}
				else if(temp[1].equals("08"))
				{
					monat="August";
				}
				else if(temp[1].equals("09"))
				{
					monat="September";
				}
				else if(temp[1].equals("10"))
				{
					monat="Oktober";
				}
				else if(temp[1].equals("11"))
				{
					monat="November";
				}
				else
				{
					monat="Dezember";
				}
				datum_lang = temp[2] + ". "+monat+" "+temp[0];  //Datum 2
				res.close();
			//Ende Basis-Infos
				
			//Urkundendaten suchen
				
				ResultSet res2 = stmt.executeQuery( "SELECT r.*, e.intType, e.bolRelay, e.strName AS eventname FROM rounds r, events e WHERE r.idEvent=e.id AND intStatus"+kritStatus+"=6 AND (r.strName='Finale' OR r.strName='Zeitläufe')" );
				while(res2.next())
				{
					int eventType=res2.getInt("intType");
					
					boolean stf;
					if(res2.getInt("bolRelay")==0) //Staffel: ja oder nein?
						stf=false;
					else
					{
						stf=true;
					}
						
					disziplin=res2.getString("eventname");
						
					if(eventType==1 && !stf) //EventName2
					{
							disz_lang = disziplin+"-Lauf";
					}
					else
					{
						disz_lang = disziplin;
					}
					
					stmt = con.createStatement();
					ResultSet res4 = stmt.executeQuery( "SELECT * FROM startlist WHERE idRound=" + res2.getString("id") + " AND lngRank>=1 AND lngRank<=" + m.model_dis.getNumber().intValue() );
					while(res4.next())
					{
						stmt = con.createStatement();
						ResultSet res5;
						int typeGet=m.cb_seek.getSelectedIndex(); ////Einzel=0, Staffel=1, Beides=2
						if(!stf && (typeGet==0 || typeGet==2))
						{
							res5 = stmt.executeQuery( "SELECT m.*, c.strName AS vereinsname, kl.strName AS klasse FROM members m, clubs c, classes kl WHERE m.idClub=c.id AND m.id=" + res4.getString("idCompetitor") + " AND kl.id=" + res4.getString("idClass") );
						}
						else if(stf && (typeGet==1 || typeGet==2))
						{
							res5 = stmt.executeQuery( "SELECT r.*, c.strName AS vereinsname, kl.strName AS klasse FROM relays r, clubs c, classes kl WHERE r.idClub=c.id AND r.id=" + res4.getString("idCompetitor") + " AND kl.id=" + res4.getString("idClass") );
						}
						else
						{
							res5=stmt.executeQuery( "SELECT * FROM members WHERE strName='XYXYX XYXYX'");
						}
						
						while(res5.next())
						{
							if(!stf && (typeGet==0 || typeGet==2))
							{	
								m.data_einz.add( new Dataset_einz() );
								einz++;
								m.data_einz.get(einz).name = res5.getString("strName") ;  //Name
								m.data_einz.get(einz).vname = res5.getString("strForename") ; //Vorname
								
								String temp2 = res5.getString("intYOB");
								char[] yob = temp2.toCharArray();
								m.data_einz.get(einz).jg = String.valueOf(yob[2]) + String.valueOf(yob[3]);
							}
							else if(stf && (typeGet==1 || typeGet==2))
							{
								m.data_stf.add( new Dataset_stf() );
								anz_stf++;
								m.data_stf.get(anz_stf).name = res5.getString("strName");
								int i=0;
								stmt = con.createStatement();
								ResultSet res6 = stmt.executeQuery( "SELECT * FROM members2relays WHERE idStartlist=" + res4.getString("id") );
								while(res6.next())
								{
									stmt = con.createStatement();
									ResultSet res7 = stmt.executeQuery( "SELECT * FROM members WHERE id="+res6.getString("idMember") );
									while(res7.next())
									{
										m.data_stf.get(anz_stf).names[i] = res7.getString("strName");
										m.data_stf.get(anz_stf).vnames[i] = res7.getString("strForename");
									}
									res7.close();
									i++;
								}
								res6.close();
							}
											
							if(!stf && (typeGet==0 || typeGet==2))  //Verein auslesen
							{
								m.data_einz.get(einz).verein = res5.getString("vereinsname");
							}
							else if(stf && (typeGet==1 || typeGet==2))
							{
								m.data_stf.get(anz_stf).verein = res5.getString("vereinsname");
							}
							
							if(!stf && (typeGet==0 || typeGet==2))  //Klasse auslesen
							{
								m.data_einz.get(einz).klasse = res5.getString("klasse");
							}
							else if(stf && (typeGet==1 || typeGet==2))
							{
								m.data_stf.get(anz_stf).klasse = res5.getString("klasse");
							}
							
							if(eventType==1)
							{
								double temp2 = res4.getDouble("dblValue");
								temp2 = temp2/1000;
								
								if(temp2>60)
								{
									double min = Math.floor( temp2/60 );
									double sec = temp2 - (60*min);
									DecimalFormat df = new DecimalFormat(",#00.00");
									if(!stf && (typeGet==0 || typeGet==2))
										m.data_einz.get(einz).leistung = ""+(int)min+":"+df.format(sec)+" min";
									else if(stf && (typeGet==1 || typeGet==2))
										m.data_stf.get(anz_stf).leistung = ""+(int)min+":"+df.format(sec)+" min";
								}
								else
								{
									DecimalFormat df = new DecimalFormat(",##0.00");
									if(!stf && (typeGet==0 || typeGet==2))
										m.data_einz.get(einz).leistung = df.format(temp2)+" sec";
									else if(stf && (typeGet==1 || typeGet==2))
										m.data_stf.get(anz_stf).leistung = df.format(temp2)+" sec";
								}
							}
							else if((typeGet==0 || typeGet==2))
							{
								double temp2 = res4.getDouble("dblValue");
								DecimalFormat df = new DecimalFormat(",##0.00");
								m.data_einz.get(einz).leistung = df.format(temp2)+" m";
							}
							
							int temp2 = res4.getInt("lngRank") ;
							
							if(!stf && (typeGet==0 || typeGet==2))
								m.data_einz.get(einz).rank1 = ""+temp2;
							else if(stf && (typeGet==1 || typeGet==2))
								m.data_stf.get(anz_stf).rank1 =  ""+temp2;
							
							if(res5.getInt("intSex")==1 && !stf && (typeGet==0 || typeGet==2))  //Also männlich
							{
								switch (temp2)
								{
									case 1: m.data_einz.get(einz).rank2 = "Erster"; break;
									case 2: m.data_einz.get(einz).rank2 = "Zweiter"; break;
									case 3: m.data_einz.get(einz).rank2 = "Dritter"; break;
									case 4: m.data_einz.get(einz).rank2 = "Vierter"; break;
									case 5: m.data_einz.get(einz).rank2 = "Fünfter"; break;
									case 6: m.data_einz.get(einz).rank2 = "Sechster"; break;
									case 7: m.data_einz.get(einz).rank2 = "Siebter"; break;
									case 8: m.data_einz.get(einz).rank2 = "Achter"; break;
								}
							}
							else if(stf && (typeGet==1 || typeGet==2))
							{
								switch (temp2)
								{
									case 1: m.data_stf.get(anz_stf).rank2 = "Erster"; break;
									case 2: m.data_stf.get(anz_stf).rank2 = "Zweiter"; break;
									case 3: m.data_stf.get(anz_stf).rank2 = "Dritter"; break;
									case 4: m.data_stf.get(anz_stf).rank2 = "Vierter"; break;
									case 5: m.data_stf.get(anz_stf).rank2 = "Fünfter"; break;
									case 6: m.data_stf.get(anz_stf).rank2 = "Sechster"; break;
									case 7: m.data_stf.get(anz_stf).rank2 = "Siebter"; break;
									case 8: m.data_stf.get(anz_stf).rank2 = "Achter"; break;
								}
							}
							else if(typeGet==0 || typeGet==2)
							{
								switch (temp2)
								{
									case 1: m.data_einz.get(einz).rank2 = "Erste"; break;
									case 2: m.data_einz.get(einz).rank2 = "Zweite"; break;
									case 3: m.data_einz.get(einz).rank2 = "Dritte"; break;
									case 4: m.data_einz.get(einz).rank2 = "Vierte"; break;
									case 5: m.data_einz.get(einz).rank2 = "Fünfte"; break;
									case 6: m.data_einz.get(einz).rank2 = "Sechste"; break;
									case 7: m.data_einz.get(einz).rank2 = "Siebte"; break;
									case 8: m.data_einz.get(einz).rank2 = "Achte"; break;
								}
							}
	
	
							if(!stf && (typeGet==0 || typeGet==2))
							{
								m.data_einz.get(einz).eventname1 = disziplin;
								m.data_einz.get(einz).ort = ort;
								m.data_einz.get(einz).dat1 = datum;
								m.data_einz.get(einz).dat2 = datum_lang;
								m.data_einz.get(einz).eventname2 = disz_lang;
							}
							else if(stf && (typeGet==1 || typeGet==2))
							{
								m.data_stf.get(anz_stf).eventname = disziplin;
								m.data_stf.get(anz_stf).ort = ort;
								m.data_stf.get(anz_stf).dat1 = datum;
								m.data_stf.get(anz_stf).dat2 = datum_lang;
							}
						}
						res5.close();
					}
					res4.close();
				}
				res2.close();
				stmt.close();
			
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			if (con!=null)
				try{ con.close();} catch(SQLException e){e.printStackTrace();}	
		}
		display(m);
		//m.model.addRow(ort, datum, datum_lang, "verein_in", "leistung_in", "platz_in", false);
	}
	
	static void display(Main m)  //ausgelesene Datensï¿½tze den Tabellen übergeben
	{
		m.model.clear();
		for(int i=0; i<m.data_einz.size(); i++)
		{
			if(!m.getAll)  //Also nur fertige, nicht gedruckt Wettkämpfe anzeigen
			{
				m.model.addRow(m.data_einz.get(i), true);
			}
			else
			{
				m.model.addRow(m.data_einz.get(i), false);
			}
		}
		m.model_stf.clear();
		for(int i=0; i<m.data_stf.size(); i++)
		{
			if(!m.getAll)  //Also nur fertige, nicht gedruckt Wettkämpfe anzeigen
			{
				m.model_stf.addRow(m.data_stf.get(i), true);
			}
			else
			{
				m.model_stf.addRow(m.data_stf.get(i), false);
			}
		}
		
		//Zeilen mit den Datasets verstecken
		m.table.getColumnModel().getColumn(7).setMaxWidth(0);
		m.table.getColumnModel().getColumn(7).setMinWidth(0);
		m.table.getColumnModel().getColumn(7).setPreferredWidth(0);
		m.table_stf.getColumnModel().getColumn(6).setMaxWidth(0);
		m.table_stf.getColumnModel().getColumn(6).setMinWidth(0);
		m.table_stf.getColumnModel().getColumn(6).setPreferredWidth(0);
		
		m.sort();
	}
	
	static void setStatus(Main m)
	{
		try
		{
			Class.forName(info.DRIVER);
		}
		catch(ClassNotFoundException e)
		{
			System.err.println( "Keine Treiber-Klasse!" );
			return;
		}
		
		Connection con = null;
		
		try
		{
			con = DriverManager.getConnection(info.PROTOCOL+file.getAbsoluteFile()); //, info.USER, info.PASS
			
			Statement stmt = con.createStatement();
			int typeGet=m.cb_seek.getSelectedIndex();
			
			//ï¿½nderungen ausfï¿½hren
			ResultSet res11 = stmt.executeQuery("SELECT * FROM rounds WHERE intStatus=6 AND (strName='Finale' OR strName='Zeitläufe')");
			while(res11.next())
			{
				Statement stmt12 = con.createStatement();
				ResultSet res12 = stmt12.executeQuery("SELECT bolRelay FROM events WHERE id="+res11.getString("idEvent"));
				while(res12.next())
				{
					boolean stf = res12.getBoolean("bolRelay");
					if((stf && (typeGet==1 || typeGet==2)) || (!stf && (typeGet==0 || typeGet==2)) )
					{
						stmt = con.createStatement();
						stmt.executeUpdate("UPDATE rounds SET intStatus=7 WHERE id="+res11.getString("id"));
					}

				}
				res12.close();
			}
			res11.close();
	
			stmt.close();
			con.close();
		}
		catch(SQLException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			if (con!=null)
				try{ con.close();} catch(SQLException e){e.printStackTrace();}	
		}
	}
}
