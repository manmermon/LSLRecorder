/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.zTransform;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.complex.Complex;

import lslrec.auxiliar.extra.Tuple;
import lslrec.gui.miscellany.VerticalFlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * @author Manuel Merino Monge
 *
 */
public class ZPlanePluginWindow extends JFrame 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 48354833945827279L;

	private JPanel contentPane;

	private ZPlanePane zpp = null;
	private JPanel panel;
	private JPanel panel_1;
	private ButtonGroup buttonGroup;
	private JRadioButton radZeros;
	private JRadioButton radPoles;
	private JPanel panel_2;
	private JPanel panel_3;
	private JPanel panel_4;
	private JScrollPane scrollPane;
	private JScrollPane scrollPane_1;
	private JPanel panel_zeros;
	private JPanel panel_poles;
	private JPanel panel_5;
	
	private FrequencyPanel panelFreq;
	
	private Filter filter = null;
	private JPanel panel_6;
	private JButton btnClear;
	private JLabel lblSamplingRate;
	private JSeparator separator;
	private JSpinner spinnerSampling;
	private JPanel panel_7;
	private JPanel panel_8;
	private JLabel lbGain;
	private JSpinner spinnerGain;
	private JLabel lbGainFrq;
	private JSpinner spinnerGainFrq;
	
	/**
	 * Create the frame.
	 */
	public ZPlanePluginWindow( Filter f ) 
	{
		this.filter = f;
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
				
		contentPane.add(getPanel(), BorderLayout.WEST);
		contentPane.add(getPanel_5(), BorderLayout.CENTER);
	}

	private ZPlanePane getZPlanePane()
	{
		if( this.zpp == null )
		{
			this.zpp = new ZPlanePane();
			if( getRadZeros().isSelected() )
			{
				this.zpp.setMarkerType( Marker.Type.ZERO );
			}
			else 
			{
				this.zpp.setMarkerType( Marker.Type.POLE );
			}
			
			this.zpp.addZeroPoleEventListener( new ZeroPoleEventListener() 
			{				
				@Override
				public void ZeroPoleEvent(ZeroPoleEvent ev) 
				{
					List< Marker > org = zpp.getZerosPoles();
					List< Marker > markers = new ArrayList< Marker >( org );
										
					JPanel zeros = getPanelZeros();
					JPanel poles = getPanelPoles();
					
					zeros.setVisible( false );
					poles.setVisible( false );
					
					zeros.removeAll();
					poles.removeAll();
					
					List< Marker > zs = new ArrayList<Marker>();
					List< Marker > ps = new ArrayList<Marker>();
					
					for( Marker m : markers )
					{
						if( m.getType() == Marker.Type.ZERO )
						{
							zs.add( m );
						}
						else
						{
							ps.add( m );
						}
					}
					
					if( filter != null )
					{
						filter.setZeroPoles( org );
					}
					
					setMarkerList( zs, zeros );
					setMarkerList( ps, poles );
					
					zeros.setVisible( true );
					poles.setVisible( true );
										
					drawFrequencyBehavior( );
				};
			} );
		}
		
		return this.zpp;
	}
	
	private void setMarkerList( List< Marker > markerList, final JPanel dst )
	{
		if( markerList != null && dst != null )
		{
			while( !markerList.isEmpty() )
			{
				Marker z1 = markerList.get( 0 );
				markerList.remove( 0 );
				
				Tuple< Double, Double > v = z1.getValue();
				
				Marker z2 = null;
				
				for( int j = 0; j < markerList.size(); j++ )
				{
					Marker aux = markerList.get( j );
					Tuple< Double, Double > auxV = aux.getValue();
					
					boolean sameX = auxV.t1.doubleValue() == v.t1.doubleValue();
					boolean conjY = auxV.t2.doubleValue() == -v.t2.doubleValue(); 
					if( sameX && conjY)
					{
						z2 = aux;
						markerList.remove( j );
						
						break;
					}
				}
				
				JPanel p = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
				p.setBorder( BorderFactory.createEtchedBorder());
				
				JLabel remove = new JLabel( "X" );
				remove.setForeground( Color.RED );
				Font f = remove.getFont();
				remove.setFont( new Font( f.getName(), Font.BOLD, f.getSize() ));
				
				final List< Marker > listZ = new ArrayList<Marker>();
				listZ.add( z1 );
				if( z2 != null )
				{
					listZ.add( z2 );
				}
				remove.addMouseListener( new MouseAdapter() 
				{
					public void mouseReleased(java.awt.event.MouseEvent e) 
					{						
						getZPlanePane().removeZeroPole( listZ );
						
						dst.setVisible( false );
						dst.remove( p );
						dst.setVisible( true );
					};
				});				
				p.add( remove );
										
				String ztx = "("+String.format( "%.3f",v.t1) + ", " + String.format( "%.3f",v.t2) + ")";
				JLabel z1lb = new JLabel(  );
				p.add( z1lb );
				z1lb.setFont( new Font( f.getName(), Font.BOLD, 10 ) );
				
				if( z2 != null )
				{
					v = z2.getValue();												
					
					ztx += ", ("+String.format( "%.3f",v.t1) + ", " + String.format( "%.3f",v.t2) + ")";					
				}
				

				z1lb.setText( "<html><p>" + ztx + "</p></html>" );
				
				dst.add( p );
			}
		}
	}
	
	private void drawFrequencyBehavior( )
	{
		FrequencyPanel fp = this.getFreqPanel();
		
		fp.setVisible( false );
		
		List< Complex > ws = new ArrayList< Complex >();
		
		Tuple< double[], double[] > coefs = filter.getFilterCoef();
		double[] a = coefs.t1;
		double[] b = coefs.t2;
		
		int lenA = a.length;
		if( lenA < 2 )
		{
			lenA++;
		}
		
		int lenB = b.length;
		if( lenB < 2 )
		{
			lenB++;
		}
		
		double[] _a =  Arrays.copyOf( a, lenA );
		ArrayUtils.reverse( _a );
		
		double[] _b = Arrays.copyOf( b, lenB );				
		ArrayUtils.reverse( _b );
		
		
		double step = Math.PI / fp.getWidth(); 
		for( double w = 0; w < 2 * Math.PI; w += step )
		{
			double r = Math.cos( -w );
			double i = Math.sin( -w );
			
			Complex z = new Complex( r, i );
			
			Complex polyNum = Utils.polyval( _b, z );
			
			Complex polyDen = Utils.polyval( _a, z );
						
			ws.add( polyNum.divide( polyDen ) );
		}
		
		
		fp.drawData( ws.toArray( new Complex[0] ) );
		
		fp.setVisible( true );
	}
	
	public List< Marker > getZerosPoles()
	{
		return this.getZPlanePane().getZerosPoles();
	}
	
	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setBorder(new TitledBorder(null, "Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panel.setLayout(new BorderLayout(0, 0));
			panel.add(getPanel_7(), BorderLayout.NORTH);
			
			panel.add(getPanel_2(), BorderLayout.CENTER);
			panel.add(getPanel_6(), BorderLayout.SOUTH);
		}
		return panel;
	}
	private JPanel getPanel_1() 
	{
		if (panel_1 == null) 
		{
			panel_1 = new JPanel();
			panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.LINE_AXIS));
			panel_1.add(getRadZeros());
			panel_1.add(getRadPoles());
			
			getButtonGroup().add( getRadZeros() );
			getButtonGroup().add( getRadPoles() );			
			panel_1.add(getSeparator());
			panel_1.add(getLblSamplingRate());
			panel_1.add(getSpinnerSampling());
		}
		return panel_1;
	}
	/**
	 * @wbp.nonvisual location=22,349
	 */
	
	private ButtonGroup getButtonGroup() 
	{
		if (buttonGroup == null) 
		{
			buttonGroup = new ButtonGroup();
		}
		return buttonGroup;
	}
	private JRadioButton getRadZeros() {
		if (radZeros == null) {
			radZeros = new JRadioButton("zero");
			radZeros.setSelected(true);
			
			radZeros.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					getZPlanePane().setMarkerType( Marker.Type.ZERO);
				}
			});
		}
		return radZeros;
	}
	private JRadioButton getRadPoles() {
		if (radPoles == null) {
			radPoles = new JRadioButton("pole");
			
			radPoles.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					getZPlanePane().setMarkerType( Marker.Type.POLE);
				}
			});
		}
		return radPoles;
	}
	private JPanel getPanel_2() {
		if (panel_2 == null) {
			panel_2 = new JPanel();
			panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));
			panel_2.add(getPanel_3());
			panel_2.add(getPanel_4());
		}
		return panel_2;
	}
	private JPanel getPanel_3() {
		if (panel_3 == null) {
			panel_3 = new JPanel();
			panel_3.setBorder(new TitledBorder(null, "Zeros", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panel_3.setLayout(new BorderLayout(0, 0));
			panel_3.add(getScrollPane(), BorderLayout.CENTER);
		}
		return panel_3;
	}
	private JPanel getPanel_4() {
		if (panel_4 == null) {
			panel_4 = new JPanel();
			panel_4.setBorder(new TitledBorder(null, "Poles", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panel_4.setLayout(new BorderLayout(0, 0));
			panel_4.add(getScrollPane_1(), BorderLayout.CENTER);
		}
		return panel_4;
	}
	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(getPanelZeros());
			scrollPane.setPreferredSize( new Dimension( 120, 0 ));
		}
		return scrollPane;
	}
	private JScrollPane getScrollPane_1() {
		if (scrollPane_1 == null) {
			scrollPane_1 = new JScrollPane();
			scrollPane_1.setViewportView(getPanelPoles());
			scrollPane_1.setPreferredSize( new Dimension( 120, 0 ));
		}
		return scrollPane_1;
	}
	private JPanel getPanelZeros() {
		if (panel_zeros == null) {
			panel_zeros = new JPanel();
			panel_zeros.setLayout( new VerticalFlowLayout( VerticalFlowLayout.TOP ) );
			//panel_zeros.setPreferredSize( new Dimension( 0, 100 ) );
		}
		return panel_zeros;
	}
	private JPanel getPanelPoles() {
		if (panel_poles == null) {
			panel_poles = new JPanel();
			panel_poles.setLayout( new VerticalFlowLayout( VerticalFlowLayout.TOP ) );
			//panel_poles.setPreferredSize( new Dimension( 0, 100 ) );
		}
		return panel_poles;
	}
	private JPanel getPanel_5() {
		if (panel_5 == null) {
			panel_5 = new JPanel();
			panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.X_AXIS));
			
			panel_5.add( this.getZPlanePane() );
			panel_5.add(getFreqPanel());
		}
		return panel_5;
	}
	private FrequencyPanel getFreqPanel() {
		if (panelFreq == null) {
			panelFreq = new FrequencyPanel();
		}
		return panelFreq;
	}
	private JPanel getPanel_6() {
		if (panel_6 == null) {
			panel_6 = new JPanel();
			panel_6.add(getBtnClear());
		}
		return panel_6;
	}
	private JButton getBtnClear() {
		if (btnClear == null) {
			btnClear = new JButton("clear");
			btnClear.addActionListener(new ActionListener() 
			{
				public void actionPerformed(ActionEvent e) 
				{
					getZPlanePane().clearZeroPole();
				}
			});
		}
		return btnClear;
	}
	private JLabel getLblSamplingRate() {
		if (lblSamplingRate == null) {
			lblSamplingRate = new JLabel("Sampling rate:");
		}
		return lblSamplingRate;
	}
	private JSeparator getSeparator() {
		if (separator == null) {
			separator = new JSeparator();
			separator.setOrientation(SwingConstants.VERTICAL);
		}
		return separator;
	}
	private JSpinner getSpinnerSampling() {
		if (spinnerSampling == null) {
			spinnerSampling = new JSpinner();
			spinnerSampling.setModel(new SpinnerNumberModel(new Double(1), new Double(1e-5D), null, new Double(1)));
			spinnerSampling.setPreferredSize( new Dimension( 50, 0 ) );
			
			spinnerSampling.addMouseWheelListener( new MouseWheelListener()
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
						sp.setValue( ((SpinnerNumberModel) sp.getModel()).getMinimum() );
					}
					else
					{
						sp.setValue( v );
					}
				}
			});
			
			spinnerSampling.addChangeListener( new ChangeListener() 
			{	
				@Override
				public void stateChanged(ChangeEvent e) 
				{
					JSpinner sp = (JSpinner)e.getSource();
					double Fm = (double)sp.getValue();
					
					getZPlanePane().setSamplingRate( Fm );
				}
			});
		}
		return spinnerSampling;
	}
	private JPanel getPanel_7() {
		if (panel_7 == null) {
			panel_7 = new JPanel();
			panel_7.setLayout(new BoxLayout(panel_7, BoxLayout.Y_AXIS));
			panel_7.add(getPanel_1() );
			panel_7.add(getPanel_8());
		}
		return panel_7;
	}
	private JPanel getPanel_8() {
		if (panel_8 == null) {
			panel_8 = new JPanel();
			panel_8.setLayout(new BoxLayout(panel_8, BoxLayout.X_AXIS));
			panel_8.add(getLbGain());
			panel_8.add(getSpinnerGain());
			panel_8.add(getLbGainFrq());
			panel_8.add(getSpinnerGainFrq());
		}
		return panel_8;
	}
	private JLabel getLbGain() {
		if (lbGain == null) {
			lbGain = new JLabel(" Gain ");
		}
		return lbGain;
	}
	private JSpinner getSpinnerGain() {
		if (spinnerGain == null) {
			spinnerGain = new JSpinner();
			spinnerGain.setModel(new SpinnerNumberModel(new Double(1), null, null, new Double(1)));
			
			spinnerGain.addMouseWheelListener( new MouseWheelListener()
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
						sp.setValue( ((SpinnerNumberModel) sp.getModel()).getMinimum() );
					}
					else
					{
						sp.setValue( v );
					}
				}
			});
			
			spinnerGain.addChangeListener( new ChangeListener() 
			{	
				@Override
				public void stateChanged(ChangeEvent e) 
				{
					JSpinner sp = (JSpinner)e.getSource();
					double G = (double)sp.getValue();
					double fn = (double)getSpinnerGainFrq().getValue();
					
					if( filter != null )
					{
						filter.setGain( G, fn );
					}

					drawFrequencyBehavior();

					getZPlanePane().repaint();
				}
			});
		}
		return spinnerGain;
	}
	private JLabel getLbGainFrq() {
		if (lbGainFrq == null) {
			lbGainFrq = new JLabel(" Norm. Freq. ");
		}
		return lbGainFrq;
	}
	private JSpinner getSpinnerGainFrq() {
		if (spinnerGainFrq == null) {
			spinnerGainFrq = new JSpinner();
			spinnerGainFrq.setModel(new SpinnerNumberModel( 0.0, 0.0, 1.0, 0.01 ));
			
			spinnerGainFrq.addMouseWheelListener( new MouseWheelListener()
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
						sp.setValue( ((SpinnerNumberModel) sp.getModel()).getMinimum() );
					}
					else
					{
						sp.setValue( v );
					}
				}
			});
			
			spinnerGainFrq.addChangeListener( new ChangeListener() 
			{	
				@Override
				public void stateChanged(ChangeEvent e) 
				{
					JSpinner sp = (JSpinner)e.getSource();
					double fn = (double)sp.getValue();
					double G = (double)getSpinnerGain().getValue();
					
					if( filter != null )
					{
						filter.setGain( G, fn );
					}
					
					drawFrequencyBehavior();
				}
			});
		}
		return spinnerGainFrq;
	}
}
