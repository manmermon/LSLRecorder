package lslrec.plugin.impl.dataProcessing.levelChecker;

import java.util.List;

import lslrec.auxiliar.extra.NumberRange;
import lslrec.auxiliar.thread.timer.Timer;
import lslrec.config.Parameter;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

public class LevelCheckerProcessing extends LSLRecPluginDataProcessing
{
	public static final String WARNING_RANGE = "WARNING RANGE";
	public static final String ALERT_RANGE = "ALERT RANGE";
	public static final String CHECK_TIMER = "CHECK TIMER";	
	
	private NumberRange warnLevel = new NumberRange( 16, 30 );
	private NumberRange alertLevel = new NumberRange( 0, 15 );
	private int check_timer = 10; // 10s
	
	private Timer timer = null;
	private Object sync = new Object();
	
	public LevelCheckerProcessing(IStreamSetting setting, LSLRecPluginDataProcessing prevProc) 
	{
		super( setting, prevProc );		
	}	
	
	@Override
	public String getID() 
	{
		return this.getClass().getSimpleName();
	}

	@Override
	protected void finishProcess()
	{	
		if( this.timer != null )
		{
			this.timer.destroyTimer();
			this.timer = null;
		}
	}

	@Override
	public int getBufferLength() 
	{
		return 1;
	}

	@Override
	public int getOverlapOffset() 
	{
		return 1;
	}

	@Override
	public void loadProcessingSettings(List<Parameter<String>> arg0) 
	{
		synchronized( this.sync )
		{
			for( Parameter< String > par : arg0 )
			{
				switch ( par.getID() )
				{
					case ALERT_RANGE:
					{
						String range = par.getValue().replaceAll("[", "");
						range = range.replace( "]", "");
						String[] values = range.split( "," );
						
						double min = Double.parseDouble( values[ 0 ] );
						double max = Double.parseDouble( values[ 0 ] );
						
						this.alertLevel = new NumberRange( min, max );
						
						break;
					}
					case WARNING_RANGE:
					{
						String range = par.getValue().replaceAll("[", "");
						range = range.replace( "]", "");
						String[] values = range.split( "," );
						
						double min = Double.parseDouble( values[ 0 ] );
						double max = Double.parseDouble( values[ 1 ] );
						
						this.warnLevel = new NumberRange( min, max );
						
						break;
					}
					case CHECK_TIMER:
					{
						String time = par.getValue();
						this.check_timer = Integer.parseInt( time );
						
						this.setCheckTimer();
					}
					default:
						break;
				}
			}
		}
	}

	@Override
	protected Number[] processData(Number[] arg0) 
	{		
		synchronized( this.sync )
		{
			if( arg0 != null && (this.timer == null || !this.timer.isRunning() ) )
			{		
				int level = 0;
				
				Number value = null;
				for( Number n : arg0 )
				{
					if( this.warnLevel.within( n ) && level < 1)
					{
						level = 1;
						value = n;
					}
					
					if( this.alertLevel.within( n ) )
					{
						level = 2;
						value = n;
						
						break;
					}
				}
				
				if( level == 1 )
				{
					ExceptionMessage msg = new ExceptionMessage( new Exception( "Warning: level is " + value  ), "WARNING LEVEL", ExceptionMessage.WARNING_MESSAGE );
					ExceptionDialog.showMessageDialog(msg, true, false);
				}
				else if( level == 2 )
				{
					ExceptionMessage msg = new ExceptionMessage( new Exception( "Alert: level is " + value ), "ALERT LEVEL", ExceptionMessage.WARNING_MESSAGE );
					ExceptionDialog.showMessageDialog(msg, true, false);
				}
			}			
			
			return arg0;
		}
	}
	
	private void setCheckTimer()
	{
		if( this.timer == null || !this.timer.isRunning() )
		{
			this.timer = new Timer( this.check_timer * 1000, false, null );
		}
	}

}
