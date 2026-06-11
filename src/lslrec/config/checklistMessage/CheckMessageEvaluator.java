/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2011-2026 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.  https://github.com/manmermon/LSLRecorder
 *
 *   LSLRec is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   LSLRec is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with LSLRec.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */
package lslrec.config.checklistMessage;

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
public class CheckMessageEvaluator 
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
	public CheckMessageEvaluator( String idPar, String exp, List< ICheckMessagePart > pars ) 
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
