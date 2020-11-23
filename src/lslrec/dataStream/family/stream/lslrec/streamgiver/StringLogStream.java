/**
 * 
 */
package lslrec.dataStream.family.stream.lslrec.streamgiver;

/**
 * @author Manuel Merino Monge
 *
 */
public class StringLogStream extends ByteStreamGiver 
{
	@Override
	protected final void setData() 
	{
		synchronized ( this )
		{
			super.notify();
		}
	}
	
	public final void push_log( String log )
	{
		super.timestamps.add( System.nanoTime() / 1e9D ); 
		super.strs.add( log );
		
		this.setData();
	}
}
