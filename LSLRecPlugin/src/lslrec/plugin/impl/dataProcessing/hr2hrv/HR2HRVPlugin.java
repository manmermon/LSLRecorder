package lslrec.plugin.impl.dataProcessing.hr2hrv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lslrec.auxiliar.WarningMessage;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.LSLRecConfigurablePluginAbstract;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

public class HR2HRVPlugin  extends LSLRecConfigurablePluginAbstract implements ILSLRecPluginDataProcessing
{
	private ParameterList pars = null;
	
	public HR2HRVPlugin() 
	{
		this.pars = new ParameterList();
		
		Parameter par = new Parameter< Integer >( HR2HRVProcessing.BUFFER_LEN, 20 );
		this.pars.addParameter( par );
	}
	
	@Override
	public WarningMessage checkSettings() 
	{
		WarningMessage warn = new WarningMessage();
		
		for( String id : this.pars.getParameterIDs() )
		{
			Parameter par = this.pars.getParameter( id );
			switch ( id ) 
			{
				case HR2HRVProcessing.BUFFER_LEN:
				{					
					if( (Integer)par.getValue() <= 0 )
					{
						warn.addMessage( "Buffer length must be greater than 0.", WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
			}
		}
		
		return warn;
	}

	@Override
	public JPanel getSettingPanel() 
	{
		JPanel container = new JPanel( new BorderLayout() );
				
		JPanel panel = new JPanel( new GridBagLayout() );
		container.add( panel, BorderLayout.NORTH );
		
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
				case HR2HRVProcessing.BUFFER_LEN:
				{
					final int step = 1;
					
					final int minLenValue = 1, maxLenValue = 200;
					JSpinner sp = new JSpinner( new SpinnerNumberModel( ((Integer)this.pars.getParameter( idPar ).getValue()).intValue() , minLenValue, maxLenValue, step ) );
					
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
							
							Integer v = (Integer)sp.getValue() + update;
							if( v < minLenValue )
							{
								sp.setValue( minLenValue );
							}
							else if( v > maxLenValue )
							{
								sp.setValue( maxLenValue );						
							}
							else
							{
								sp.setValue( v );
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
				
		return container;
	}
	
	@Override
	public PluginType getType() 
	{
		return PluginType.DATA_PROCESSING;
	}

	@Override
	public String getID()
	{
		return "HR2HRV";
	}

	@Override
	public int compareTo(ILSLRecPlugin arg0) 
	{
		// TODO Auto-generated method stub
		return arg0.getID().compareTo( this.getID() );
	}

	@Override
	public LSLRecPluginDataProcessing getProcessing(IStreamSetting arg0, ParameterList pars, LSLRecPluginDataProcessing arg1) 
	{
		HR2HRVProcessing hrvprocess = new HR2HRVProcessing( arg0, arg1 );
		hrvprocess.loadProcessingSettings( this.getSettings() );
		
		return hrvprocess;
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
				case HR2HRVProcessing.BUFFER_LEN:
				{					
					this.pars.getParameter( id ).setValue( Integer.parseInt( val ) );
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
	protected void postLoadSettings() 
	{		
	}

	@Override
	protected void setSettingPanel(JPanel arg0) 
	{	
	}

	@Override
	public ProcessingLocation getProcessingLocation() 
	{
		return ProcessingLocation.BOTH;
	}

}
