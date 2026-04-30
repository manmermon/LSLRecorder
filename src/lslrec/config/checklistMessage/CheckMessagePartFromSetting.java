package lslrec.config.checklistMessage;

import lslrec.config.ConfigApp;

public class CheckMessagePartFromSetting implements ICheckMessagePart
{
	private String idConfigParamenter = "";
	
	public CheckMessagePartFromSetting( String idCfgPar ) 
	{
		this.idConfigParamenter = idCfgPar;
	}

	@Override
	public void setIDLangCaption(String id) 
	{	
	}

	@Override
	public boolean isPartEditable() 
	{
		return false;
	}

	@Override
	public String getMessagePart() 
	{
		Object par = ConfigApp.getProperty( this.idConfigParamenter );
		
		String val = ( par != null ) ? par.toString() : "";
		
		return val;
	}

	@Override
	public boolean setMessagePart( String noTx ) 
	{		
		return true;
	}

	@Override
	public String toString() 
	{
		String str = "<" 
				+ TypeMessagePart.Setting.name() + toStrSeparator
				+ this.isPartEditable() + this.toStrSeparator
				+ "" + this.toStrSeparator
				+ "" + this.toStrSeparator;
		
		String unicode = String.format("\\u%04X", (int) this.toStrSeparator);
		str += this.idConfigParamenter.replace( String.valueOf( this.toStrSeparator ), unicode);
		
		str += ">";
		
		return str;
	}
}
