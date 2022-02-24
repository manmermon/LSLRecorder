/**
 * 
 */
package lslrec.testing.LSLSender;

import lslrec.dataStream.tools.StreamUtils.StreamDataType;

/**
 * @author Manuel Merino Monge
 *
 */
class testLongLSLStreamOutlet {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		LSLSimulationParameters pars = new LSLSimulationParameters();
		pars.setInDataType( StreamDataType.int64 );
		pars.setOutDataType( StreamDataType.int64 );
		pars.setNumberOutputBlocks( 1000 );
		pars.setSamplingRate( 10 );
		pars.setOutputFunctionType( pars.LINEAR );
		
		try 
		{
			LSLSimulationStreaming lsl = new LSLSimulationStreaming( pars );
			lsl.startThread();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	}

}
