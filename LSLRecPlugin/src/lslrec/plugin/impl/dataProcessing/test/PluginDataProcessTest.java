/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.test;

import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import lslrec.auxiliar.WarningMessage;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.dataStream.setting.DataStreamSetting;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

/**
 * @author Manuel Merino Monge
 *
 */
public class PluginDataProcessTest implements ILSLRecPluginDataProcessing 
{
	private ParameterList pars = null;

	/**
	 * 
	 */
	public PluginDataProcessTest() 
	{
		this.pars = new ParameterList();
		
		for( int i = 0; i < 10; i++ )
		{
			this.pars.addParameter( new Parameter< String >( "ParTest" + i , "prueba " + i ) );
		}
	}
	
	@Override
	public JPanel getSettingPanel() 
	{
		JPanel p = new JPanel(  );
		p.setLayout( new BoxLayout( p,BoxLayout.Y_AXIS ));
		
		for( String id : this.pars.getParameterIDs() )
		{
			JTextField t = new JTextField( pars.getParameter( id ).getValue().toString() );
			
			t.getDocument().addDocumentListener( new DocumentListener() 
			{				
				@Override
				public void removeUpdate(DocumentEvent arg0) {
					// TODO Auto-generated method stub
					update( arg0 );
				}
				
				@Override
				public void insertUpdate(DocumentEvent arg0) {
					// TODO Auto-generated method stub
					update( arg0 );
				}
				
				@Override
				public void changedUpdate(DocumentEvent arg0) {
					// TODO Auto-generated method stub
					update( arg0 );
				}
				
				private void update( DocumentEvent e )
				{
					Document d = e.getDocument();
					
					try 
					{
						String tx = d.getText( 0, d.getLength() );
						pars.getParameter( id ).setValue( tx );
					}
					catch (BadLocationException e1) 
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
					
			p.add( t );
		}
		
		return p;
	}

	@Override
	public List<Parameter<String>> getSettings() 
	{
		return null;
	}

	@Override
	public void loadSettings(List<Parameter<String>> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PluginType getType() 
	{
		return PluginType.DATA_PROCESSING;
	}

	@Override
	public String getID() 
	{
		return "Testing";
	}

	@Override
	public LSLRecPluginDataProcessing getProcessing( DataStreamSetting setting, LSLRecPluginDataProcessing prevProcess )
	{
		return new DataProcessingTest( setting, prevProcess);
	}

	@Override
	public int compareTo( ILSLRecPlugin o ) 
	{
		return o.getID().compareTo( this.getID() );
	}

	@Override
	public WarningMessage checkSettings() 
	{
		return new WarningMessage();
	}
}
