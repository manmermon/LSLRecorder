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
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

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
	public static final String SAMPLING_RATE = "sampling rate (Hz)";

	public double freq = 1;
	public double time = 1;
	
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
		return super.getClass().getSimpleName();
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
		return 1;
	}

	@Override
	public int getOverlapOffset() 
	{
		return 1;
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
						this.time = (int)(Double.parseDouble( val ));
						
						break;
					}
					case SAMPLING_RATE:
					{
						this.freq = (int)(Double.parseDouble( val ));
						
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
		
		while( this.inputs.size() > this.time * this.freq )
		{
			this.inputs.remove( 0 );
		}
		
		if( this.inputs.size() == this.time * this.freq )
		{
			Complex[] dft = Utils.dft( ConvertTo.Casting.NumberArray2DoubleArray( this.inputs.toArray( new Double[0] )  ) );
		
			this.inputs.clear();
			
			this.freqPanel.drawData( dft );			
		}
		
		return arg0;
	}

}
