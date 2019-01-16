package Controls.Messages;

import java.util.Comparator;

import Config.Language.Language;

public class SpecialInputMessage implements Comparable< SpecialInputMessage >, Comparator< SpecialInputMessage > 
{
	private int markValue;
	private String markText;
	
	private String markLegend = "";
	
	private boolean special = false;
	
	public SpecialInputMessage( int mark, String text )
	{
		this.markText = text;
		this.markValue = mark;
	}
	
	public void setSpecial(boolean special) 
	{
		this.special = special;
	}
	
	public boolean isSpecial() 
	{
		return special;
	}
	
	public int getMarkValue() 
	{
		return markValue;
	}
	
	public void setMarkValue( int markValue ) 
	{
		this.markValue = markValue;
	}
	
	public String getMarkText() 
	{
		return markText;
	}
	
	public void setMarkText( String markText ) 
	{
		this.markText = markText;
	}
	
	public void setMarkLegendToken( String langToken )
	{
		this.markLegend = langToken;
	}
	
	public String getMarkLegend() 
	{
		return Language.getLocalCaption( this.markLegend );
	}
	
	@Override
	public boolean equals( Object obj ) 
	{
		boolean eq = (obj instanceof SpecialInputMessage );
		
		if( !eq )
		{
			eq = this.markValue == ((SpecialInputMessage)obj).getMarkValue()
					&& this.markText.equals( ((SpecialInputMessage)obj).getMarkText() );
		}
		
		return eq;
	}

	@Override
	public int compareTo(SpecialInputMessage o) 
	{
		int pos = 0;
		
		if( !this.equals( o ) )
		{
			pos = this.markValue - o.getMarkValue();
			if( pos == 0 )
			{
				pos = this.markText.compareTo( o.getMarkText() );
			}
		}
		
		return 0;
	}
	
	@Override
	public int hashCode() 
	{
		return ( this.markText + this.markValue ).hashCode();
	}

	@Override
	public int compare(SpecialInputMessage o1, SpecialInputMessage o2) 
	{
		return o1.compareTo( o2 );
	}
}
