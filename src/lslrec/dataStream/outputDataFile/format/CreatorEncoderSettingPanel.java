package lslrec.dataStream.outputDataFile.format;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import lslrec.config.ConfigApp;
import lslrec.config.SettingOptions;
import lslrec.config.language.Language;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionDictionary;
import lslrec.exceptions.handler.ExceptionMessage;

public class CreatorEncoderSettingPanel 
{
	public static JScrollPane getSettingPanel( List< SettingOptions > opts )
	{
		JScrollPane scrollPane = null;
			
		if( opts != null && !opts.isEmpty() )
		{
			int cols = 2;
			
			scrollPane = new JScrollPane();
			JPanel panel = new JPanel();			
			
			scrollPane.setViewportView( panel );
			
			panel.setLayout(new GridLayout(1, 2, 0, 0) );
			
			JPanel leftPanel = new JPanel();
			leftPanel.setLayout( new BoxLayout( leftPanel, BoxLayout.Y_AXIS ));			
			
			JPanel rigthPanel = new JPanel();
			rigthPanel.setLayout( new BoxLayout( rigthPanel, BoxLayout.Y_AXIS ));
			
			JPanel adjustLeftPanel = new JPanel( new BorderLayout() );
			adjustLeftPanel.add( leftPanel, BorderLayout.NORTH );
			JPanel adjustRightPanel = new JPanel( new BorderLayout() );
			adjustRightPanel.add( rigthPanel, BorderLayout.NORTH );
			
			panel.add( adjustLeftPanel );
			panel.add( adjustRightPanel );
			
	
			int c = 0;
			for( SettingOptions opt : opts )
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx =  c / cols;
				gbc.gridy = ( c % cols );
				gbc.anchor = GridBagConstraints.WEST;
				
				Component comp = getSettingPanel( opt );
				
				if( comp != null )
				{
					JPanel selPanel = leftPanel;
					
					if( gbc.gridy == 1 )
					{
						selPanel = rigthPanel;
					}
					
					JPanel p = new JPanel( new BorderLayout() );
					p.setBorder( BorderFactory.createTitledBorder( opt.getID() ) );					
					p.add( comp, BorderLayout.CENTER );
					
					selPanel.add( p );					
				}
				
				c++;
			}
		}
		
		return scrollPane;
	}

	private static Component getSettingPanel( SettingOptions opt )
	{
		Component c = null;

		if( opt != null )
		{

			String[] options = opt.getOptions();
			boolean isList = opt.isList();
			String refPar = opt.getIDReferenceParameter();	
			int sel = opt.getSelectedValue();

			if( options != null && options.length > 0 )
			{
				if( !isList )
				{
					JTextField txt = new JTextField( );

					String text = options[ 0 ];

					txt.setText( text );

					txt.getDocument().addDocumentListener( new DocumentListener()
					{									
						@Override
						public void removeUpdate(DocumentEvent e)
						{
							updateVal( e );
						}

						@Override
						public void insertUpdate(DocumentEvent e)
						{
							updateVal( e );
						}

						@Override
						public void changedUpdate(DocumentEvent e)
						{
							updateVal( e );
						}

						private void updateVal( DocumentEvent e )
						{	
							try 
							{
								String desc = e.getDocument().getText( 0, e.getDocument().getLength() );

								saveValue( refPar, desc );
							} 
							catch ( Exception e1) 
							{
								ExceptionMessage m = new ExceptionMessage( e1, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionDictionary.ERROR_MESSAGE );

								ExceptionDialog.showMessageDialog( m, true, true );
							}
						}
					});								

					c = txt;
				}
				else
				{
					JComboBox< String > combox = new JComboBox<String>();

					for( Object val : options )
					{
						combox.addItem( val.toString() );
					}

					combox.addActionListener( new ActionListener()
					{									
						@Override
						public void actionPerformed(ActionEvent arg0)
						{
							JComboBox< String > cb = (JComboBox< String >)arg0.getSource();

							String select = cb.getSelectedItem().toString();
							
							try 
							{
								saveValue( refPar, select );
							} 
							catch (Exception e1) 
							{
								ExceptionMessage m = new ExceptionMessage( e1, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionDictionary.ERROR_MESSAGE );

								ExceptionDialog.showMessageDialog( m, true, true );
							}
						}
					});
					
					if( sel > 0 && sel < options.length )
					{
						combox.setSelectedIndex( sel );
					}

					c = combox;						
				}
			}

		}

		return c;
	}
	
	private static void saveValue( String parRefID, String value ) throws Exception
	{
		Object type = ConfigApp.getProperty( parRefID );
		if( type != null && value != null )
		{
			if( type instanceof String )
			{
				ConfigApp.setProperty( parRefID, value );
			}
			else if( type instanceof Boolean )
			{
				ConfigApp.setProperty( parRefID, Boolean.parseBoolean( value ) );
			}
			else if( type instanceof Number )				
			{
				Number d = Double.parseDouble( value );
				
				if( type instanceof Float )
				{
					d = d.floatValue();
				}
				else if( type instanceof Long )
				{
					d = d.longValue();
				}
				else if( type instanceof Integer )
				{
					d = d.intValue();
				}
				else if( type instanceof Short )
				{
					d = d.shortValue();
				}
				else if( type instanceof Byte )
				{
					d = d.byteValue();
				}
				
				ConfigApp.setProperty( parRefID, d );
			}
		}
	}
}
