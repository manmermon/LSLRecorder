/**
 * 
 */
package lslrec.dataStream.family.stream.lslrec.streamgiver;

import lslrec.auxiliar.task.ITaskLog;

/**
 * @author Manuel Merino Monge
 *
 */
public class StringLogStream extends ByteStreamGiver implements ITaskLog
{	
	@Override
	protected final void setData() 
	{
		synchronized ( this )
		{
			super.notify();
		}
	}

	@Override
	public void log( String log ) 
	{
		super.timestamps.add( System.nanoTime() / 1e9D ); 
		super.strs.add( log );
		
		this.setData();
	}
}
