/**
 * 
 */
package lslrec.plugin.lslrecPlugin.trial;

import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;
import lslrec.dataStream.family.stream.lslrec.LSLRecSimpleDataStream;
import lslrec.dataStream.family.stream.lslrec.streamgiver.StringLogStream;

/**
 * @author Manuel Merino Monge
 *
 */
public final class LSLRecPluginTrialLog extends LSLRecSimpleDataStream
{		
	private StringLogStream log = null;
	
	public LSLRecPluginTrialLog( String streamName, StringLogStream log )
	{
		super( streamName
				, StreamDataType.string
				, IStreamSetting.IRREGULAR_RATE
				, 1
				, 1
				, log );
		
		this.log = log;
	}
	
	public void push_log( String log )
	{
		this.log.push_log( log );
	}
}

