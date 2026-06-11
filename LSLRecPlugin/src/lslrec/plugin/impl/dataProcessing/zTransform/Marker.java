/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec. https://github.com/manmermon/LSLRecorder
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
package lslrec.plugin.impl.dataProcessing.zTransform;

import lslrec.auxiliar.extra.Tuple;

/**
 * @author Manuel Merino Monge
 *
 */
public class Marker 
{
	enum Type{ ZERO, POLE };
	
	private Type type = Type.ZERO;
	
	private Tuple< Double, Double > c;
	
	/**
	 * 
	 */
	public Marker( double real, double img, Type t ) 
	{
		this.c = new Tuple<Double, Double>( real, img );
		
		this.type = t;
	}
	
	public Marker( double real, double img ) 
	{
		this( real, img, Type.ZERO );
	}
	
	/**
	 * @return the c
	 */
	public Tuple<Double, Double> getValue() 
	{
		return this.c;
	}
	
	/**
	 * @return the type
	 */
	public Type getType() 
	{
		return this.type;
	}
	
	@Override
	public boolean equals( Object in ) 
	{
		boolean eq = ( in instanceof Marker );
		
		if( eq )
		{
			Marker o = (Marker)in;
			eq = o.getType() == this.type;
			
			if( eq )
			{
				Tuple< Double, Double > vo = o.getValue();
				
				eq = vo.t1.doubleValue() == this.c.t1.doubleValue();
				if( eq )
				{
					eq = vo.t2.doubleValue() == this.c.t2.doubleValue();
				}
			}
		}
		
		return eq;
	}
	
	@Override
	public String toString() 
	{
		return "<"+this.type.name() + "=(" + c.t1 + ", " + c.t2 + ")>";
	}
}
