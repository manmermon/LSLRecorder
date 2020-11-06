/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.firFilter;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.xy.DefaultXYDataset;

import lslrec.auxiliar.WarningMessage;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.dataStream.setting.DataStreamSetting;
import lslrec.plugin.impl.dataProcessing.firFilter.FIRFilter.FilterType;
import lslrec.plugin.impl.dataProcessing.firFilter.FilterWindow.WindowType;
import lslrec.plugin.impl.gui.basicPainter2D;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

/**
 * @author Manuel Merino Monge
 *
 */
public class FIRFilterPlugin implements ILSLRecPluginDataProcessing 
{
	private ParameterList pars = null;
	
	private double prevFm = 128;
	/**
	 * 
	 */
	public FIRFilterPlugin( ) 
	{
		this.pars = new ParameterList();
		
		Parameter par = new Parameter< Double >( FIRFilter.CUT_FREQ1, 48D );
		this.pars.addParameter( par );
		
		par = new Parameter< Double >( FIRFilter.CUT_FREQ2, 52D );
		this.pars.addParameter( par );
		
		par = new Parameter< Integer >( FIRFilter.FILTER_LENGTH, 100 );
		this.pars.addParameter( par );
		
		par = new Parameter< WindowType >( FIRFilter.WINDOW_TYPE, WindowType.HAMMING );
		this.pars.addParameter( par );
		
		par = new Parameter< FilterType >( FIRFilter.FILTER_TYPE, FilterType.LOWPASS_FILTER );
		this.pars.addParameter( par );
	}
	
	@Override
	public WarningMessage checkSettings() 
	{
		WarningMessage wm = new WarningMessage();
		
		double fq = 0;
		
		for( String id : this.pars.getParameterIDs() )
		{
			Parameter par = this.pars.getParameter( id );
			switch ( id ) 
			{
				case FIRFilter.FILTER_LENGTH:
				{					
					if( (Integer)par.getValue() <= 0 )
					{
						wm.addMessage( "Filter length must be greater than 0.", WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
				case FIRFilter.CUT_FREQ1:
				{			
					Double fr = (Double)par.getValue();
					if( fr <= 0 )
					{
						wm.addMessage( "Cut frequency 1 must be greater than 0.", WarningMessage.ERROR_MESSAGE );
					}
					
					fq -= fr;
					
					break;
				}
				case FIRFilter.CUT_FREQ2:
				{				
					Double fr = (Double)par.getValue();
					if( fr <= 0 )
					{
						wm.addMessage( "Cut frequency 2 must be greater than 0.", WarningMessage.ERROR_MESSAGE );
					}
					
					fq += fr;
					
					break;
				}
				default:
				{
					break;
				}
			}
		}
		
		FilterType ft = (FilterType)this.pars.getParameter( FIRFilter.FILTER_TYPE ).getValue();
		
		if( fq < 0 && ( ft == FilterType.BANDPASS_FILTER || ft == FilterType.NOTCH_FILTER  ) )
		{
			wm.addMessage( "Cut frequency 2 must be greater than cut frequency 1.", WarningMessage.ERROR_MESSAGE );
		}
		
		return wm;
	}

	@Override
	public JPanel getSettingPanel() 
	{
		JPanel container = new JPanel( new BorderLayout() );
		
		JPanel panel = new JPanel( new GridBagLayout() );
		
		JPanel preview = new JPanel( new BorderLayout() );
		preview.setBorder( BorderFactory.createEtchedBorder() );
				
		JSpinner fmSp = new JSpinner( new SpinnerNumberModel( prevFm, 0.000001D, null, 1D ) );
		fmSp.setPreferredSize( new Dimension( 75, 20 ) );
		fmSp.addMouseWheelListener( new MouseWheelListener()
		{
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) 
			{
				JSpinner sp = (JSpinner)e.getSource();
				
				double update = 0;
				if( e.getWheelRotation() > 0 )
				{
					update = -1;
				}
				else if( e.getWheelRotation() < 0 )
				{
					update = +1;
				}
				
				double v = (Double)sp.getValue() + update;
				if( v <= 0 )
				{
					sp.setValue( 0.000001 );
				}
				else
				{
					sp.setValue( v );
				}
			}
		});
		
		fmSp.addChangeListener( new ChangeListener() 
		{	
			@Override
			public void stateChanged(ChangeEvent e) 
			{
				JSpinner sp = (JSpinner)e.getSource();
				double Fm = (double)sp.getValue();
				
				FilterType ft = (FilterType)pars.getParameter( FIRFilter.FILTER_TYPE ).getValue();
				updateFilterPreview( preview, Fm, ft );
				
				prevFm = Fm;
			}
		});
		
		preview.addComponentListener( new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e) 
			{
				FilterType ft = (FilterType)pars.getParameter( FIRFilter.FILTER_TYPE ).getValue();
				
				double Fm = (double)fmSp.getValue();
				updateFilterPreview( preview, Fm, ft );
			}
		});
		
		JPanel setPreview = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		setPreview.add( new JLabel( "Preview: Sampling rate (Hz): " ) );
		setPreview.add( fmSp );
		
		
		container.add( panel, BorderLayout.NORTH );
		
		JPanel prePanel = new JPanel( new BorderLayout() );
		container.add( prePanel, BorderLayout.CENTER );
		
		prePanel.add( setPreview, BorderLayout.NORTH );
		prePanel.add( preview, BorderLayout.CENTER );
		
		int cols = 2;
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 0.5;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets( 0, 0, 0, 0 );
		gbc.gridwidth = 1;				
		
		int c = -1;
		List< String > idPars = new ArrayList<String>( this.pars.getParameterIDs() );
		Collections.sort( idPars );
		for( String idPar : idPars )
		{	
			JPanel parPanel = new JPanel( new BorderLayout() );
			parPanel.setBorder( BorderFactory.createTitledBorder( idPar ) );
			
			Component cmp = null;
			switch ( idPar )
			{
				case FIRFilter.FILTER_LENGTH:
				{
					final int step = 1;
					
					JSpinner sp = new JSpinner( new SpinnerNumberModel( (Integer)this.pars.getParameter( idPar ).getValue() , 1, null, step ) );	
					sp.addMouseWheelListener( new MouseWheelListener()
					{
						@Override
						public void mouseWheelMoved(MouseWheelEvent e) 
						{
							JSpinner sp = (JSpinner)e.getSource();
							
							int update = 0;
							if( e.getWheelRotation() > 0 )
							{
								update = -step;
							}
							else if( e.getWheelRotation() < 0 )
							{
								update = step;
							}
							
							Integer v = (Integer)sp.getValue();
							if( v > 1 )
							{
								sp.setValue( v + update );
							}
							else
							{
								sp.setValue( 1 );
							}
						}
					});
					
					sp.addChangeListener( new ChangeListener() 
					{	
						@Override
						public void stateChanged(ChangeEvent e) 
						{
							JSpinner sp = (JSpinner)e.getSource();
							Integer v = (Integer)sp.getValue();
							
							pars.getParameter( idPar ).setValue( v );
							
							double Fm = (double)fmSp.getValue();
							FilterType ft = (FilterType)pars.getParameter( FIRFilter.FILTER_TYPE ).getValue();
							updateFilterPreview( preview, Fm, ft);
						}
					});
					
					cmp = sp;
					
					break;
				}
				case FIRFilter.WINDOW_TYPE:
				{
					JComboBox< WindowType > cb = new JComboBox< WindowType >( WindowType.values() );
					
					cb.addItemListener( new ItemListener() 
					{	
						@Override
						public void itemStateChanged(ItemEvent e) 
						{
							if( e.getStateChange() == ItemEvent.SELECTED )
							{
								WindowType d = (WindowType)e.getItem();
								
								pars.getParameter( idPar ).setValue( d );
								
								double Fm = (double)fmSp.getValue();
								FilterType ft = (FilterType)pars.getParameter( FIRFilter.FILTER_TYPE ).getValue();
								updateFilterPreview( preview, Fm, ft );
							}
						}
					});
					
					cb.addMouseWheelListener( new MouseWheelListener()
					{
						@Override
						public void mouseWheelMoved(MouseWheelEvent e) 
						{
							JComboBox< Integer > sp = (JComboBox< Integer >)e.getSource();
							
							int index = sp.getSelectedIndex();
							
							if( e.getWheelRotation() > 0 )
							{
								index--;
							}
							else
							{
								index++;
							}
							
							if( index < 0 )
							{
								index = 0;
							}
							
							if( index >= sp.getItemCount() )
							{
								index = sp.getItemCount() -1;
							}
							
							sp.setSelectedIndex( index );
						}
					});
					
					cb.setSelectedItem( this.pars.getParameter( idPar ).getValue() );
					
					cmp = cb;
					
					break;
				}
				case FIRFilter.FILTER_TYPE:
				{
					JComboBox< FilterType > cb = new JComboBox< FilterType >( FilterType.values() );
					
					cb.addItemListener( new ItemListener() 
					{	
						@Override
						public void itemStateChanged(ItemEvent e) 
						{
							if( e.getStateChange() == ItemEvent.SELECTED )
							{
								FilterType d = (FilterType)e.getItem();
								
								pars.getParameter( idPar ).setValue( d );
								
								double Fm = (double)fmSp.getValue();
								FilterType ft = (FilterType)pars.getParameter( FIRFilter.FILTER_TYPE ).getValue();
								updateFilterPreview( preview, Fm, ft );
							}
						}
					});
					
					cb.addMouseWheelListener( new MouseWheelListener()
					{
						@Override
						public void mouseWheelMoved(MouseWheelEvent e) 
						{
							JComboBox< Integer > sp = (JComboBox< Integer >)e.getSource();
							
							int index = sp.getSelectedIndex();
							
							if( e.getWheelRotation() > 0 )
							{
								index--;
							}
							else
							{
								index++;
							}
							
							if( index < 0 )
							{
								index = 0;
							}
							
							if( index >= sp.getItemCount() )
							{
								index = sp.getItemCount() -1;
							}
							
							sp.setSelectedIndex( index );
						}
					});
					
					cb.setSelectedItem( this.pars.getParameter( idPar ).getValue() );
					
					cmp = cb;
					
					break;
				}
				case FIRFilter.CUT_FREQ1:
				case FIRFilter.CUT_FREQ2:
				{
					final double step = 1D;
					
					Number v = (Number)pars.getParameter( idPar ).getValue();
					JSpinner sp = new JSpinner( new SpinnerNumberModel( v.doubleValue(), 0D, null, step ) );	
					sp.addMouseWheelListener( new MouseWheelListener()
					{
						@Override
						public void mouseWheelMoved(MouseWheelEvent e) 
						{
							JSpinner sp = (JSpinner)e.getSource();
							
							double update = 0;
							if( e.getWheelRotation() > 0 )
							{
								update = -step;
							}
							else if( e.getWheelRotation() < 0 )
							{
								update = step;
							}
							
							double v = (Double)sp.getValue() + update;
							if( v >= 0 )
							{
								sp.setValue( v );
							}
							else
							{
								sp.setValue( 0 );
							}
						}
					});
					
					sp.addChangeListener( new ChangeListener() 
					{	
						@Override
						public void stateChanged(ChangeEvent e) 
						{
							JSpinner sp = (JSpinner)e.getSource();
							Double v = (Double)sp.getValue();
							
							pars.getParameter( idPar ).setValue( v );
							
							double Fm = (double)fmSp.getValue();
							FilterType ft = (FilterType)pars.getParameter( FIRFilter.FILTER_TYPE ).getValue();
							
							updateFilterPreview( preview, Fm, ft);
						}
					});
					
					cmp = sp;
					
					break;
				}
				default:
				{
					break;
				}
			}
			
			if( cmp != null )
			{
				parPanel.add( cmp, BorderLayout.CENTER );
				
				c++;
				gbc.gridx = ( c % cols );
				gbc.gridy =  c / cols;
				
				panel.add( parPanel, gbc );
			}
		}
		
		FilterType ft = (FilterType)this.pars.getParameter( FIRFilter.FILTER_TYPE ).getValue();
		
		this.updateFilterPreview( preview, (double)fmSp.getValue(), ft );
		
		return container;
	}

	private void updateFilterPreview( JPanel previewPanel, double Fm, FilterType ft )
	{
		previewPanel.setVisible( false );
		previewPanel.removeAll();
		
		if( Fm > 0 )
		{
			int L = (int)this.pars.getParameter( FIRFilter.FILTER_LENGTH ).getValue();
			WindowType t = (WindowType)this.pars.getParameter( FIRFilter.WINDOW_TYPE ).getValue();
					
			double Fc1 = (double)this.pars.getParameter( FIRFilter.CUT_FREQ1 ).getValue();
			double Fc2 = (double)this.pars.getParameter( FIRFilter.CUT_FREQ2 ).getValue();
		
			double[] h;
			switch ( ft ) 
			{
				case HIGHPASS_FILTER:
				{
					h =  FIRFilterCoefficients.FIRHighpassFilterCoefficients( L, Fc1 / Fm,  t );
				
					break;
				}
				case BANDPASS_FILTER:
				{
					h =  FIRFilterCoefficients.FIRBandpassFilterCoefficients( L, Fc1 / Fm, Fc2 / Fm, t );
					
					break;
				}
				case NOTCH_FILTER:
				{
					h =  FIRFilterCoefficients.FIRNotchFilterCoefficients( L, Fc1 / Fm, Fc2 / Fm, t );
					
					break;
				}
				default:
				{
					h =  FIRFilterCoefficients.FIRLowpassFilterCoefficients( L, Fc1 / Fm, t );
					
					break;
				}
			}
						
			if( h.length > 0 )
			{			
				int p2 =  (int)Math.pow( 2, (int)( Math.ceil( Math.log( L ) / Math.log( 2 ) ) ) );
								
				if( p2 > h.length )
				{
					int npad = p2 - h.length;
					double[] hpad = new double[ h.length + npad ];
					System.arraycopy( h, 0, hpad, 0, h.length );
					
					h = hpad;
				}
				
				FastFourierTransformer fft = new FastFourierTransformer( DftNormalization.STANDARD );
				Complex[] dft = fft.transform( h, TransformType.FORWARD );
				
				double[][] mods = new double[ 2 ][ Math.round( h.length / 2 ) ];
				double[][] G = new double[ 2 ][ Math.round( h.length / 2 ) ];
				
				double min = 0, max = 1;
				double minG = Double.NEGATIVE_INFINITY, maxG = Double.NEGATIVE_INFINITY;
				for( int i = 0; i < mods[ 0 ].length; i++ )
				{
					Complex c = dft[ i ];
					
					mods [ 0 ][ i ] =  Fm * i / h.length;
					G[ 0 ][ i ] = mods[ 0 ][ i ];
					
					mods [ 1 ][ i ] = c.abs();
					G[ 1 ][ i ] = 20*Math.log10( mods[ 1 ][ i ] );
					
					if( G[ 1 ][ i ] == Double.NEGATIVE_INFINITY )
					{
						G[ 1 ][ i ] = -Double.MIN_VALUE;
					}
					
					if( min > c.abs() )
					{
						min = c.abs();
					}
					
					if( c.abs() > max )
					{
						max = c.abs();
					}
					
					if( minG > G[ 1 ][ i ] )
					{
						minG = G[ 1 ][ i ] ;
					}
					
					if( G[ 1 ][ i ] > maxG )
					{
						maxG = G[ 1 ][ i ];
					}
				}
				
				DefaultXYDataset xyValues = new DefaultXYDataset();
				xyValues.addSeries( this.getID(), mods );
				JFreeChart chart = ChartFactory.createXYLineChart( null, null, "Module", xyValues  );
				chart.setAntiAlias( true );				
				chart.setBackgroundPaint( Color.WHITE );
				chart.getXYPlot().setBackgroundPaint( Color.WHITE );
				chart.getXYPlot().setRangeGridlinePaint( Color.BLACK );
				chart.getXYPlot().setDomainGridlinePaint( Color.BLACK );
				chart.getXYPlot().getRenderer().setSeriesStroke( 0, new BasicStroke( 3.0F ) );
				ValueAxis yaxis = chart.getXYPlot().getRangeAxis();
				yaxis.setRange( min - 0.25, max + 0.25 );
				
				chart.getXYPlot().setRangeAxis( yaxis );				
				chart.clearSubtitles();
				
				
				
				DefaultXYDataset xyValues2 = new DefaultXYDataset();
				xyValues2.addSeries( this.getID(), G );
				JFreeChart chart2 = ChartFactory.createXYLineChart( null, "Sampling rate (Hz)", "Gain (dB)", xyValues2 );
				chart2.setAntiAlias( true );				
				chart2.setBackgroundPaint( Color.WHITE );
				chart2.getXYPlot().setBackgroundPaint( Color.WHITE );
				chart2.getXYPlot().setRangeGridlinePaint( Color.BLACK );
				chart2.getXYPlot().setDomainGridlinePaint( Color.BLACK );
				chart2.getXYPlot().getRenderer().setSeriesStroke( 0, new BasicStroke( 3.0F ) );
				ValueAxis yaxis2 = chart2.getXYPlot().getRangeAxis();
				
				if( minG == Double.NEGATIVE_INFINITY || minG == Double.NaN )
				{
					minG = Double.MIN_VALUE;
				}
				
				if( maxG == Double.POSITIVE_INFINITY|| maxG == Double.NaN )
				{
					maxG = Double.MAX_VALUE;
				}
				
				//yaxis2.setRange( minG, maxG );
				
				chart2.getXYPlot().setRangeAxis( yaxis2 );				
				chart2.clearSubtitles();
				
				
				
				int width = previewPanel.getWidth();
				int height = previewPanel.getHeight();
				
				if ((width > 0) && (height > 0 ) )
				{
					Image img = basicPainter2D.createEmptyCanva( width, height / 2, Color.WHITE );
					
					chart.draw( (Graphics2D)img.getGraphics(), 
								new Rectangle2D.Double( 0, 0, img.getWidth( null ), img.getHeight( null ) ) );
					
					Image img2 = basicPainter2D.createEmptyCanva( width, height / 2, Color.WHITE );
					
					chart2.draw( (Graphics2D)img2.getGraphics(), 
								new Rectangle2D.Double( 0, 0, img2.getWidth( null ), img2.getHeight( null ) ) );
					
					JLabel lb = new JLabel( new ImageIcon( img ) );
					JLabel lb2 = new JLabel( new ImageIcon( img2 ) );
					
					previewPanel.setLayout( new BoxLayout( previewPanel, BoxLayout.Y_AXIS ) );
					previewPanel.add( lb );
					previewPanel.add( lb2 );
				}
			}
		}
		
		previewPanel.setVisible( true );
	}
	
	@Override
	public List< Parameter< String > > getSettings() 
	{
		List< Parameter< String > > parList = new ArrayList<Parameter<String>>();
		
		for( String idPar : this.pars.getParameterIDs() )
		{
			Parameter< String > p = new Parameter<String>( idPar, this.pars.getParameter( idPar ).getValue().toString() );
			
			parList.add( p );
		}
		
		return parList;
	}

	@Override
	public void loadSettings( List< Parameter< String > > arg0 ) 
	{
		for( Parameter< String > par : arg0 )
		{	
			String id = par.getID();
			String val = par.getValue();
			
			switch ( id ) 
			{
				case FIRFilter.FILTER_LENGTH:
				{					
					this.pars.getParameter( id ).setValue( Integer.parseInt( val ) );
					break;
				}
				case FIRFilter.CUT_FREQ1:
				case FIRFilter.CUT_FREQ2:
				{					
					this.pars.getParameter( id ).setValue( Double.parseDouble( val ) );
					break;
				} 
				case FIRFilter.WINDOW_TYPE:
				{
					this.pars.getParameter( id ).setValue( WindowType.valueOf( val ) );
					break;
				}
				case FIRFilter.FILTER_TYPE:
				{
					this.pars.getParameter( id ).setValue( FilterType.valueOf( val ) );
					break;
				}
				default:
				{
					break;
				}
			}
		}
	}

	@Override
	public PluginType getType() 
	{
		return PluginType.DATA_PROCESSING;
	}

	@Override
	public int compareTo( ILSLRecPlugin arg0 ) 
	{
		return this.getID().compareTo( arg0.getID() );
	}
	
	@Override
	public String getID() 
	{
		return "FIR Filter";
	}

	@Override
	public LSLRecPluginDataProcessing getProcessing( DataStreamSetting arg0, LSLRecPluginDataProcessing arg1 ) 
	{
		FIRFilter fir = new FIRFilter( arg0, arg1 );
		fir.loadProcessingSettings( this.getSettings() );
		
		return fir;
	}
}
