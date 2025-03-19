/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.painter;

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
public class DataDisplay extends LSLRecPluginDataProcessing 
{
	public static final String DATA_LENGTH = "x-axis_span";
	public static final String VIEW_CHUNK_SIZE = "chunk_size";
	public static final String VIEW_MIN_Y = "y-axis_min";
	public static final String VIEW_MAX_Y = "y-axis_max";
	public static final String WIN_TITLE = "window title";
	
	private int xAxisSpan = 1_000;
	private int chunk_size = 1;
	private double minY, maxY;
	
	private JFrame window = null;	
	private DataPanel drawPanel = null;
		
	private Object lock = new Object();
	
	private List< Number > chunkBuffer = new LinkedList< Number >();
	
	/**
	 * @param setting
	 * @param prevProc
	 */
	public DataDisplay( IStreamSetting setting, LSLRecPluginDataProcessing prevProc )
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
		return "Data display";
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
		if( arg0 != null )
		{
			for( Parameter< String > par : arg0 )
			{
				String val = par.getValue();
				
				switch ( par.getID() )
				{
					case DATA_LENGTH:
					{
						this.xAxisSpan = (int)(Double.parseDouble( val ));
						
						break;
					}
					case VIEW_CHUNK_SIZE:
					{
						this.chunk_size = (int)(Double.parseDouble( val ));
						
						break;
					}
					case VIEW_MIN_Y:
					{
						this.minY = Double.parseDouble( val );
						
						break;
					}
					case VIEW_MAX_Y:
					{
						this.maxY = Double.parseDouble( val );
						
						break;
					}
					case WIN_TITLE:
					{
						this.window.setTitle( val );
						break;
					}
					default:
					{
						break;
					}
				}
			}
		}
		
		synchronized( this.lock )
		{
			this.drawPanel = new DataPanel( this.xAxisSpan, minY, maxY );
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
				
				if( this.chunkBuffer.size() >= this.chunk_size * super.streamSetting.channel_count() )
				{
					this.window.setVisible( true );
										
					Number[] values = this.chunkBuffer.subList( 0, this.chunk_size * super.streamSetting.channel_count() ).toArray( new Number[0] );					
					values = ConvertTo.Transform.Interleaved( values, this.chunk_size, super.streamSetting.channel_count() );
										
					Tuple< Number[][], Number[] > data = ConvertTo.Transform.Array2Matrix( values, (long)this.chunk_size );
					this.drawPanel.drawData( ConvertTo.Casting.NumberMatrix2doubleMatrix( data.t1 ) );
					
					this.chunkBuffer.subList( 0, this.chunk_size * super.streamSetting.channel_count() ).clear();
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
