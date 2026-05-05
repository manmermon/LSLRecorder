package lslrec.gui.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import lslrec.config.ConfigApp;
import lslrec.config.language.Language;
import lslrec.dataStream.sync.SyncMethod;
import lslrec.gui.GuiManager;
import lslrec.gui.miscellany.VerticalFlowLayout;

public class Dialog_SelectionSyncMethod extends JDialog
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		
	public Dialog_SelectionSyncMethod( JButton syncBtn,  Window owner ) 
	{
		super( owner );
		
		init( syncBtn );
	}
	
	private void init( JButton syncBtn )
	{
		
		super.setUndecorated( true );
		
		JPanel p = new JPanel( new VerticalFlowLayout( VerticalFlowLayout.TOP ) );
		super.setContentPane( new JScrollPane( p ) );
		
		p.setBorder( BorderFactory.createLineBorder( Color.BLACK ) );
		
		Set< String > mets = (Set< String >)ConfigApp.getProperty( ConfigApp.SELECTED_SYNC_METHOD );
		
		if( mets.isEmpty() )
		{
			mets.add( SyncMethod.SYNC_NONE );
		}
		
		final List< JCheckBox > synMets = new ArrayList< JCheckBox >();
		for( String met : SyncMethod.getSyncMethodID() )
		{
			JCheckBox ch = new JCheckBox( met );
			
			ch.setSelected( mets.contains( met ) );
			
			synMets.add( ch );
									
			p.add( ch );
			
			if( SyncMethod.isNoneSyncMethod( met ) || SyncMethod.isAllSyncMethod( met ) )
			{
				JSeparator sp = new JSeparator( JSeparator.HORIZONTAL );							
				sp.setPreferredSize( new Dimension( syncBtn.getSize().width, 2 ) );
				
				p.add( sp );
			}
		}
		
		boolean selAll = ( mets.size() >= SyncMethod.getSyncMethodID().length - 2 );
								
		for( JCheckBox ch : synMets )
		{
			ch.addItemListener( new ItemListener() 
			{	
				@Override
				public void itemStateChanged(ItemEvent e) 
				{	
					Set< String > mets = (Set< String >)ConfigApp.getProperty( ConfigApp.SELECTED_SYNC_METHOD );
					
					JCheckBox ch = (JCheckBox)e.getSource();
					
					String sync = ch.getText();
													
					if( e.getStateChange() == ItemEvent.SELECTED )
					{
						if( SyncMethod.isNoneSyncMethod( sync ) )
						{	
							for( JCheckBox ch2 : synMets )
							{
								if( !ch2.equals( ch ) )
								{
									ch2.setSelected( false );
								}
							}
							
							syncBtn.setText( sync );
							syncBtn.setToolTipText( sync );
						}
						else
						{
							if( SyncMethod.isAllSyncMethod( sync ) )
							{
								for( JCheckBox ch2 : synMets )
								{
									if(  !ch2.equals( ch ) && !SyncMethod.isNoneSyncMethod( ch2.getText() ) )
									{
										ch2.setSelected( true );
									}
								}
								
								ch.setSelected( true );
							}
							else
							{
								mets.add( sync );
							}
																	
							for( JCheckBox ch2 : synMets )
							{											
								if( SyncMethod.isNoneSyncMethod( ch2.getText() ) )
								{
									ch2.setSelected( false );
									
									break;
								}
							}
						}
					}
					else
					{
						mets.remove( sync );
						
						if( sync.equals( SyncMethod.SYNC_STREAM ) )
						{
							try 
							{
								GuiManager.getInstance().unselectSyncDevices();
							}
							catch (Exception e1) 
							{
								e1.printStackTrace();
							}
						}									
					}
					
					boolean selAll = ( mets.size() >= SyncMethod.getSyncMethodID().length - 2 );
													
					for( JCheckBox ch2 : synMets )
					{											
						if( SyncMethod.isAllSyncMethod( ch2.getText() ) )
						{
							ch2.setSelected( selAll );
							
							break;
						}
					}
					
					if( mets.isEmpty() )
					{
						for( JCheckBox c : synMets )
						{
							if( SyncMethod.isNoneSyncMethod( c.getText() ) )
							{
								c.setSelected( true );
								
								break;
							}
						}
					}
					else
					{
						String syncText = "";
						
						for( String m :  mets )
						{
							if( syncText.isEmpty() )
							{
								syncText = m;
							}
							else
							{
								syncText = mets.size() + " " + Language.getLocalCaption( Language.SETTING_SYNC_METHOD );
								
								break;
							}
						}
						
						syncBtn.setText( syncText );									
						syncBtn.setToolTipText( mets.toString() );
					}
				}
			});
		
			if( SyncMethod.isAllSyncMethod( ch.getText() ) )
			{
				ch.setSelected( selAll );
			}
		}
	}

}
