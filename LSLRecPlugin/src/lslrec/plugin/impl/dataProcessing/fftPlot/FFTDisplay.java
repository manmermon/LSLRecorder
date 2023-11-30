/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.fftPlot;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.math3.complex.Complex;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.config.Parameter;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.plugin.impl.dataProcessing.zTransform.FrequencyPanel;
import lslrec.plugin.impl.dataProcessing.zTransform.Utils;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

/**
 * @author Manuel Merino Monge
 *
 */
public class FFTDisplay extends LSLRecPluginDataProcessing
{
	public static final String TIME_WIN = "time windows (s)";
	public static final String OVERLAP_WIN = "overlap (%)";
	//public static final String SAMPLING_RATE = "sampling rate (Hz)";

	private double freq = 1;
	private double time = 1;
	private double overlapOffset = 0;
	
	private JFrame window = null;	
	private FrequencyPanel freqPanel = null;
	
	private Object lock = new Object();
	
	private List< Double > inputs = new ArrayList< Double >();
	
	/**
	 * @param setting
	 * @param prevProc
	 */
	public FFTDisplay(IStreamSetting setting, LSLRecPluginDataProcessing prevProc)
	{	
		super( setting, prevProc );
		
		this.window = new JFrame();
		this.window.setVisible( false );
				
		this.window.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		this.window.setContentPane( new JPanel( new BorderLayout() ) );
		
		this.window.setFocusableWindowState( false );
		
		this.freq = setting.sampling_rate();
		if( this.freq == IStreamSetting.IRREGULAR_RATE )
		{
			this.freq = 1;
		}
		
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		d.width /= 2;
		d.height /= 2;
		this.window.setSize( d );
		
		this.window.setTitle( this.getID() );
		
		this.freqPanel = new FrequencyPanel();
		this.window.getContentPane().add( this.freqPanel, BorderLayout.CENTER );
		
		this.window.addWindowListener( new WindowAdapter() 
		{
			@Override
			public void windowClosing(WindowEvent e) 
			{
				synchronized( lock )
				{
					window = null;
					freqPanel = null;
				}
			}
		});
		
		this.window.setVisible( true );
	}

	
	@Override
	public String getID() 
	{
		return "DFT display";
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

	@Override
	public int getBufferLength() 
	{
		return (int)( this.freq * this.time );
	}

	@Override
	public int getOverlapOffset() 
	{
		int offset = 1;
		
		double p = 1 - this.overlapOffset / 100D;
		p = ( p > 1 ) ? 1 : p;
		
		offset = ( p <= 0 ) ? offset : (int)( this.getBufferLength() * p );
		
		return offset;
	}

	@Override
	public void loadProcessingSettings( List<Parameter<String>> arg0 ) 
	{
		if( arg0 != null )
		{
			for( Parameter< String > par : arg0 )
			{
				String val = par.getValue();
				
				switch ( par.getID() )
				{
					case TIME_WIN:
					{
						this.time = Double.parseDouble( val );
						
						break;
					}
					/*
					case SAMPLING_RATE:
					{
						this.freq = Double.parseDouble( val );
						
						if( this.freq <= 0 )
						{
							this.freq = 1;
						}
						
						break;
					}
					//*/
					case OVERLAP_WIN:
					{
						this.overlapOffset = Double.parseDouble( val );
						break;
					}
					default:
					{
						break;
					}
				}
			}
		}
	}

	@Override
	protected Number[] processData(Number[] arg0) 
	{	
		for( Number a : arg0 )
		{
			this.inputs.add( a.doubleValue() );
		}
		
		while( this.inputs.size() > this.getBufferLength() )
		{
			this.inputs.remove( 0 );
		}
		
		if( this.inputs.size() == this.getBufferLength() )
		{
			Complex[] dft = Utils.dft( ConvertTo.Casting.NumberArray2DoubleArray( this.inputs.toArray( new Double[0] )  ) );
		
			this.inputs.clear();
			
			synchronized ( this.lock )
			{
				if( this.freqPanel != null )
				{
					this.freqPanel.drawData( dft );
				}
			}
		}
		
		return arg0;
	}

}
