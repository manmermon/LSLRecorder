package lslrec.control.message.checklist;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

//*
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
//*/

import lslrec.config.ConfigApp;
public class MessageEvaluator 
{
	private String idParameter = "";
	private String exp2Eval = "";
	private List< ICheckMessagePart > args = null;
	
	private JexlEngine jexl = new JexlBuilder().strict(true).create();
	
	/**
	 * String plantilla = "Resultado: {0} < {1}";
	 * String resultado = MessageFormat.format(plantilla, 3, 25);
	 * 
	 * @param idPar
	 * @param exp
	 * @param pars
	 */
	public MessageEvaluator( String idPar, String exp, List< ICheckMessagePart > pars ) 
	{
		this.idParameter = ( idPar != null ) ? idPar : ""; ;
		this.exp2Eval = ( exp != null ) ? exp : "";
		
		this.args = ( pars == null ) ? new ArrayList< ICheckMessagePart >() : pars;
		
		try
		{
			this.jexl = new JexlBuilder().strict(true).create();
		}
		catch (Exception | Error e)
		{
			e.printStackTrace();
		}
	}
		
	public boolean evalue( )
	{
		boolean res = true;
		Object par = ConfigApp.getProperty( this.idParameter );
		
		if( !this.exp2Eval.isEmpty() && par != null)
		{
			List< String > pars = new ArrayList<String>();
			
			pars.add( "par" );
			for( int i = 0; i < this.args.size(); i++ )
			{
				ICheckMessagePart part = this.args.get( i );
				pars.add( part.getMessagePart() );
			}
			
			String exprString = MessageFormat.format(this.exp2Eval, pars.toArray() );
			//String exprString = this.prefix + "par" + this.exp2Eval;
						
			JexlExpression expr = this.jexl.createExpression( exprString );
			
			JexlContext ctx = new MapContext();
	      
	        if( this.exp2Eval.contains("{0}") )
	        {
	        	 ctx.set("par", par);	 	        
	        }

	       Object out = expr.evaluate( ctx );
	       res = (Boolean)out ;
		}
		
		return res;
	}
}
