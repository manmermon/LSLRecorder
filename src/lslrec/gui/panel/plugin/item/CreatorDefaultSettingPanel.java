package lslrec.gui.panel.plugin.item;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.config.SettingOptions;
import lslrec.config.SettingOptions.Type;
import lslrec.config.language.Language;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionDictionary;
import lslrec.exceptions.handler.ExceptionMessage;

public class CreatorDefaultSettingPanel 
{
	public static JPanel getSettingPanel( List< SettingOptions > opts, ParameterList parameters )
	{
		JPanel panel = null;
			
		if( opts != null && !opts.isEmpty() )
		{
			int cols = 2;
			panel = new JPanel();			
						
			panel.setLayout(new GridBagLayout() );

			int c = 0;
			for( SettingOptions opt : opts )
			{
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = ( c % cols );
				gbc.gridy =  c / cols;
				gbc.weightx = 0.5;
				gbc.anchor = GridBagConstraints.EAST;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.insets = new Insets( 0, 0, 0, 0 );
				
				Parameter par = parameters.getParameter( opt.getIDReferenceParameter() );
				
				Component comp = getSettingPanel( opt, par );
				
				if( comp != null )
				{
					
					JPanel p = new JPanel( new BorderLayout( 0, 0) );										
					p.add( comp, BorderLayout.CENTER );					
					
					p.setBorder( BorderFactory.createTitledBorder( par.getText() ) );

					panel.add( p, gbc );
				}
				
				c++;
			}
		}
		
		return panel;
	}

	private static Component getSettingPanel( SettingOptions opt, Parameter par )
	{
		Component c = null;

		if( opt != null )
		{

			String[] options = opt.getOptions();
			boolean isList = opt.isList();
			Type dataType = opt.getDataType();
			int sel = opt.getSelectedValue();

			if( options != null && options.length > 0 )
			{
				if( !isList )
				{
					String val = options[ 0 ];
					
					if( par != null && par.getValue() != null )
					{
						val = par.getValue().toString();
					}
					
					switch ( dataType )
					{	
						case NUMBER:
						{
							Double v = Double.parseDouble( val );
							JSpinner sp = new JSpinner( new SpinnerNumberModel( v, null, null, 1D ) );
							
							sp.addChangeListener( new ChangeListener()
							{								
								@Override
								public void stateChanged(ChangeEvent e)
								{
									JSpinner sp = (JSpinner)e.getSource();

									try
									{
										saveValue( par, sp.getValue() );
									} 
									catch ( Exception e1)
									{
										ExceptionMessage m = new ExceptionMessage( e1, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionDictionary.ERROR_MESSAGE );
										
										ExceptionDialog.showMessageDialog( m, true, true );
									}
								}
							});
							
							sp.addMouseWheelListener( new MouseWheelListener() 
							{				
								@Override
								public void mouseWheelMoved(MouseWheelEvent e) 
								{
									if( e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL )
									{
										try
										{	
											JSpinner sp = (JSpinner)e.getSource();
											
											int d = e.getWheelRotation();
											
											if( d > 0 )
											{
												sp.setValue( sp.getModel().getPreviousValue() );
											}
											else
											{
												sp.setValue( sp.getModel().getNextValue() );
											}	
										}
										catch( IllegalArgumentException ex )
										{												
										}
									}
								}
							});
							
							c = sp;
							
							break;
						}
						case BOOLEAN:
						{
							JCheckBox ch = new JCheckBox( par.getText() );
							ch.setSelected( Boolean.parseBoolean( val ) );
							
							ch.setHorizontalTextPosition( SwingConstants.LEFT );
							
							ch.addActionListener( new ActionListener() 
							{								
								@Override
								public void actionPerformed(ActionEvent arg0) 
								{
									JCheckBox ch = (JCheckBox)arg0.getSource();
									
									try 
									{
										saveValue( par, ch.isSelected() );
									}
									catch (Exception e1) 
									{
										ExceptionMessage m = new ExceptionMessage( e1, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionDictionary.ERROR_MESSAGE );
										
										ExceptionDialog.showMessageDialog( m, true, true );
									}
								}
							});
							
							c = ch;
							
							break;
						}
						default:
						{					
							JTextField txt = new JTextField( );
		
							txt.setText( val );
		
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
		
										saveValue( par, desc );
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
					}
				}
				else
				{
					JComboBox< String > combox = new JComboBox<String>();
					
					for( Object val : options )
					{
						combox.addItem( val.toString() );
					}
					
					String val = null;
					
					if( sel >= 0 && sel < options.length )
					{
						val = options[ sel ];
					}
					
					if( par.getValue() != null )
					{
						val = par.getValue().toString();
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
								saveValue( par, select );
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
						
					}
					
					combox.setSelectedItem( val );
					
					c = combox;						
				}
			}

		}

		return c;
	}
	
	private static void saveValue( Parameter par, Object value ) throws Exception
	{
		if( par != null )
		{
			par.setValue( value );
		}
	}
}
