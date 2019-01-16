package Auxiliar.Extra;

public class NumberRange implements Comparable< NumberRange >
{
    private final double min;
    private final double max;

    /**
     * 
     * @param min - Minimum value 
     * @param max - Maximum value
     * 
     * @throws IllegalArgumentException if min > max
     */
    public NumberRange( Number min, Number max ) 
    {
    	if( min.doubleValue() > max.doubleValue() )
    	{
    		throw new IllegalArgumentException( "Minimum value must be greater than maximum value.");
    	}
    	
        this.min = min.doubleValue();
        this.max = max.doubleValue();
    }

    /**
     * Check if input value is into the interval
     * @param value - Number to check
     * @return true if value is into the interval. Otherwise, false
     */
    public boolean within( Number value ) 
    {
    	double v = value.doubleValue();
    	
        return this.min <= v &&  v <= max ;
    }
    
    /**
     * 
     * @return Interval's minimum value 
     */
    public double getMin() 
    {
		return this.min;
	}
    
    /**
     * 
     * @return Interval's maximum value 
     */
    public double getMax() 
    {
		return this.max;
	}
    
    /**
     * 
     * @return the length of the interval 
     */
    public double getRangeLength()
    {
    	return this.max - this.min;
    }
     
    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() 
    {
    	return (this.min + "" + this.max).hashCode();  
    }
    
    /**
     * Check if the input interval is contained.
     * 
     * @param rng - Input to check
     * @return true if rng is contained. Otherwise, false
     */
    public boolean contain( NumberRange rng )
    {
    	boolean cont = false;
    	
    	if( rng != null )
    	{
    		cont = this.within( rng.min ) && this.within( rng.max );
    	}
    	
    	return cont;
    }
    
    /**
     * Check if intervals are overlapped.
     * 
     * @param rng - Input ot check
     * @return true if rng is overlapped. Otherwise, false.
     */
    public boolean overlap( NumberRange rng )
    {
    	boolean cont = false;
    	
    	if( rng != null )
    	{
    		cont = ( this.within( rng.min ) || this.within( rng.max ) ) || rng.contain( this );
    	}
    	
    	return cont;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) 
    {
    	boolean eq = ( obj instanceof NumberRange );
    			
    	if( eq )
    	{
    		NumberRange r = (NumberRange)obj;
    		
    		eq = r.min == this.min && this.max == r.max;
    	}
    	
    	return eq;
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
	@Override
	public int compareTo(NumberRange rng ) 
	{
		int v = 0;
		
		if( rng != null && !rng.equals( this ) )
		{
			v = 1;
			double diff = this.min - rng.min;			
			if( diff < 0 )
			{
				v = -1;
			}
			else if( diff == 0 )
			{
				diff = this.max - rng.max;
				if( diff < 0 )
				{
					v = -1;
				}
			}
		}
		
		return v;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() 
	{
		return "[" + this.min + "," + this.max + "]";
	}
}