/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.  https://github.com/manmermon/LSLRecorder
 *
 *   LSLRec is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   LSLRec is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with LSLRec.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */
package lslrec.gui.panel.plugin.item;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.auxiliar.extra.NumberRange;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.config.SettingOptions;
import lslrec.config.SettingOptions.Type;
import lslrec.config.language.Language;
import lslrec.dataStream.tools.StreamUtils;
import lslrec.dataStream.tools.StreamUtils.StreamDataType;
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
				gbc.fill = GridBagConstraints.BOTH;
				gbc.insets = new Insets( 0, 0, 0, 0 );
				
				Parameter par = parameters.getParameter( opt.getIDReferenceParameter() );
				
				Component comp = getSettingPanel( opt, par );
				
				if( comp != null )
				{					
					JPanel p = new JPanel( );
					GridLayout bl = new GridLayout( 0, 1);
					p.setLayout( bl );
					//p.setAlignmentX( Component.CENTER_ALIGNMENT );
					
					String title = par.getDescriptorText();
					
					if( !( comp instanceof JToggleButton ) )
					{
						p.add( new JLabel( " " + title ) );
					}
					
					p.add( comp );
					p.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) 
										, BorderFactory.createLineBorder( Color.GRAY ) ) );

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
			NumberRange nr = opt.getCorrectValueInterval();

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
							
							JSpinner sp = new JSpinner( new SpinnerNumberModel( v, null, null , 1D ) );
							
							if( nr != null )
							{
								SpinnerNumberModel spm = (SpinnerNumberModel)sp.getModel();
								spm.setMinimum( new Comparable<Number>() 
								{
									@Override
									public int compareTo(Number o) 
									{
										return (int)(nr.getMin() - o.doubleValue());
									}
								});
								
								spm.setMaximum( new Comparable<Number>() 
								{
									@Override
									public int compareTo(Number o) 
									{
										return (int)(nr.getMax() - o.doubleValue());
									}
								});
							}
														
							sp.addChangeListener( new ChangeListener()
							{								
								@Override
								public void stateChanged(ChangeEvent e)
								{
									JSpinner sp = (JSpinner)e.getSource();

									try
									{
										Object newValue = sp.getValue(); 
										Object val = convertValue( newValue, par.getValue() );
										
										if( val.equals( newValue ) )
										{										
											saveValue( par, val );
										}
										else
										{
											sp.setValue( val );
										}
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
							JCheckBox ch = new JCheckBox( par.getDescriptorText() );
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
								Object val = convertValue( select, par.getValue() );
								
								saveValue( par, val );
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

	private static Object convertValue( Object value, Object cast )
	{		
		Object res = value;
		
		if( cast != null && value != null)
		{
			StreamDataType dataType = StreamUtils.getDataTypeByClass( cast );
			
			if( dataType != StreamDataType.string && dataType != StreamDataType.undefined )
			{
				res = ConvertTo.Casting.NumberTo( (Number)value, dataType );
			}
		}
		
		return res;
	}
	
	private static void saveValue( Parameter par, Object value ) throws Exception
	{
		if( par != null )
		{
			Object v = value;
			if( par.getValue() instanceof String )
			{
				v = value.toString();
			}
			
			par.setValue( v );
		}
	}
}
