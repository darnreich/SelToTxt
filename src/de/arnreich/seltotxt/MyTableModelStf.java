package de.arnreich.seltotxt;

import java.util.ArrayList;
//import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
//import javax.swing.table.DefaultTableCellRenderer;
//import java.awt.Color;


class MyTableModelStf extends AbstractTableModel
{
	static final long serialVersionUID = 1L;
	
	private ArrayList<String> name = new ArrayList<String>();
	private ArrayList<String> klasse = new ArrayList<String>();
	private ArrayList<String> disziplin = new ArrayList<String>();
	private ArrayList<String> leistung = new ArrayList<String>();
	private ArrayList<String> platz = new ArrayList<String>();
	private ArrayList<Boolean> export = new ArrayList<Boolean>();
	private ArrayList<Dataset_stf> data = new ArrayList<Dataset_stf>();
	private int rows;
	
	MyTableModelStf()
	{
		rows=0;
	}
	
	public int getRowCount()
	{
		return rows;
	}
	
	public int getColumnCount()
	{
		return 7;
	}
	
	public String getColumnName(int column)
	{
		if(column==0)
			return "Name d. Staffel";
		else if(column==1)
			return "Klasse";
		else if(column==2)
			return "Disziplin";
		else if(column==3)
			return "Leistung";
		else if(column==4)
			return "Platz";
		else if(column==5)
			return "Exportieren?";
		else
			return "";
	}
	
	public Class<?> getColumnClass(int columnIndex)
	{
			if(columnIndex<5)
			{
				return String.class;
			}
			else if(columnIndex==5)
			{
				return Boolean.class;
			}
			else
				return Dataset_stf.class;
	}
	
	public Object getValueAt(int row, int col)
	{
		if(col==0)
			return name.get(row);
		else if(col==1)
			return klasse.get(row);
		else if(col==2)
			return disziplin.get(row);
		else if(col==3)
			return leistung.get(row);
		else if(col==4)
			return platz.get(row);
		else if(col==5)
			return export.get(row);
		else
			return data.get(row);
	}
	
	@Override
	public boolean isCellEditable(int row, int col)
	{
		if(col!=5)
			return false;
		else
			return true;
	}
	
	public void setValueAt( Object aValue, int row, int col )
	{
		export.set(row, (Boolean)aValue);
		
		fireTableCellUpdated( row, col );
	}
	
	public void clear()
	{
		name.clear();
		klasse.clear();
		disziplin.clear();
		leistung.clear();
		platz.clear();
		export.clear();
		data.clear();
		rows=0;
		fireTableStructureChanged();
	}
	
	public void addRow(Dataset_stf data, boolean exp)
	{
		name.add( data.name );
		klasse.add( data.klasse );
		disziplin.add( data.eventname );
		leistung.add( data.leistung );
		platz.add( data.rank1 );
		export.add(exp);
		this.data.add(data);
		rows++;
		fireTableStructureChanged();
	}
}