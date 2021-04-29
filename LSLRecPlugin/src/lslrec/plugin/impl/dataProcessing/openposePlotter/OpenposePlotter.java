/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.openposePlotter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.auxiliar.extra.Tuple;
import lslrec.config.Parameter;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

/**
 * @author Manuel Merino Monge
 *
 */
public class OpenposePlotter extends LSLRecPluginDataProcessing 
{
	public static final String BODY_POINTS = "num_body_points";
	/*
	public static final String OPP_X_RESOLUTION = "openpose_x-resolution";
	public static final String OPP_Y_RESOLUTION = "openpose_y-resolution";
	*/
	
	private int bodyPoints = 25;
	//private int xRes, yRes;
	
	private JFrame window = null;	
	private OpenposePlotterPanel drawPanel = null;
		
	private Object lock = new Object();
	
	private List< Number > chunkBuffer = new LinkedList< Number >();
	
	/**
	 * @param setting
	 * @param prevProc
	 */
	public OpenposePlotter( IStreamSetting setting, LSLRecPluginDataProcessing prevProc )
	{
		super( setting, prevProc );
		
		this.window = new JFrame();
		this.window.setVisible( false );
				
		this.window.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		this.window.setContentPane( new JPanel( new BorderLayout() ) );
		
		this.window.setFocusableWindowState( false );
		
		//this.window.setResizable( false );
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		d.width /= 2;
		d.height /= 2;
		this.window.setSize( d );
		
		this.window.setTitle( this.getID() );
		
		
		this.window.addWindowListener( new WindowAdapter() 
		{
			@Override
			public void windowClosing(WindowEvent e) 
			{
				synchronized( lock )
				{
					window = null;
					drawPanel = null;
				}
			}
		});
	}

	@Override
	public String getID() 
	{
		return "Openpose Plotter";
	}

	@Override
	public int getBufferLength() 
	{
		return this.bodyPoints * 4;
	}

	@Override
	public int getOverlapOffset() 
	{
		return 0;
	}

	
	@Override
	public void loadProcessingSettings(List<Parameter<String>> arg0) 
	{
		if( arg0 != null )
		{
			for( Parameter< String > par : arg0 )
			{
				String val = par.getValue();
				
				switch ( par.getID() )
				{
					case BODY_POINTS:
					{
						this.bodyPoints = (int)Double.parseDouble( val );
						
						break;
					}
					/*
					case OPP_X_RESOLUTION:
					{
						this.xRes = (int)Double.parseDouble( val );
						
						break;
					}
					case OPP_Y_RESOLUTION:
					{
						this.yRes = (int)Double.parseDouble( val );
						
						break;
					}
					*/
					default:
					{
						break;
					}
				}
			}
		}
		
		synchronized( this.lock )
		{
			this.drawPanel = new OpenposePlotterPanel(); //( new Dimension( this.xRes, this.yRes ) );
			if( this.window != null )
			{
				this.window.getContentPane().add( this.drawPanel, BorderLayout.CENTER );
			}
		}
	}

	@Override
	protected Number[] processData(Number[] arg0) 
	{
		synchronized( this.lock )
		{	
			if( arg0 != null && this.drawPanel != null && this.window != null )
			{
				for( Number n : arg0 )
				{
					this.chunkBuffer.add( n );
				}
				
				if( this.chunkBuffer.size() >= this.bodyPoints * super.streamSetting.channel_count() )
				{
					this.window.setVisible( true );
					
					
					Number[] values = this.chunkBuffer.subList( 0, this.bodyPoints * super.streamSetting.channel_count() ).toArray( new Number[0] );					
					values = ConvertTo.Transform.Interleaved( values, this.bodyPoints, super.streamSetting.channel_count() );
										
					Tuple< Number[][], Number[] > data = ConvertTo.Transform.Array2Matrix( values, (long)this.bodyPoints );
					
					this.drawPanel.drawData( ConvertTo.Casting.NumberMatrix2doubleMatrix( data.t1 ) );
					
					this.chunkBuffer.subList( 0, this.bodyPoints * super.streamSetting.channel_count() ).clear();
				}
			}
		}
		
		return arg0;
	}

	@Override
	protected void finishProcess() 
	{
		JFrame w = null;
		
		synchronized( this.lock )
		{
			w = this.window;
		}
		
		if( w != null )
		{
			w.dispose();
		}
	}

}
