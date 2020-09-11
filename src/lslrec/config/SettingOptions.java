package lslrec.config;

import java.util.ArrayList;
import java.util.List;

import lslrec.exceptions.UnsupportedTypeException;

public class SettingOptions
{	
	private String ID;
	private List< String > values = null;
	
	private boolean listType = false;
	private String refPar = null;
	
	private int selectedOpt =  -1;
	
	/**
	 * 
	 * @param id
	 * @param t
	 * @param isList
	 * @param idRefParameter
	 */
	public SettingOptions( String id, boolean isList, String idRefParameter  ) 
	{
		if( id == null || id.isEmpty() )
		{
			throw new IllegalArgumentException( "Input null or empty.");
		}
		
		if( idRefParameter == null || idRefParameter.isEmpty() )
		{
			throw new IllegalArgumentException( "ID of referenced parameter null or empty.");
		}
		
		this.ID = id;
		
		this.values = new ArrayList< String >( );
		
		this.listType = isList;
		
		this.refPar = idRefParameter;
	}
		
	public void addValue( String value )
	{
		if( this.listType )
		{
			this.values.add( value );
		}
		else
		{
			this.values.clear();
			this.values.add( value );
		}
	}
	
	public String getIDReferenceParameter()
	{
		return this.refPar;
	}
	
	public void removeValue( Object value )
	{
		this.values.remove( value );
	}
		
	public String getID() 
	{
		return ID;
	}
		
	public boolean isList()
	{
		return this.listType;
	}	
	
	/**
	 * 
	 * @return A copy of values
	 */
	public String[] getOptions() 
	{
		return this.values.toArray( new String[0] );
	}
	
	public void setSelectedValue( int index )
	{
		this.selectedOpt = index;
	}
	
	public int getSelectedValue()
	{
		return this.selectedOpt;
	}
}
