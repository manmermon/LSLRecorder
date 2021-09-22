/**
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
