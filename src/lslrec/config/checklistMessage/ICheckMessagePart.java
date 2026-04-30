package lslrec.config.checklistMessage;

public interface ICheckMessagePart 
{
	public static final char toStrSeparator = ',';
	
	public enum TypeMessagePart{ Text, Setting }
		
	public void setIDLangCaption( String id );

	public boolean isPartEditable();
	
	public String getMessagePart() ;	
	
	public boolean setMessagePart( String newTx );
}
