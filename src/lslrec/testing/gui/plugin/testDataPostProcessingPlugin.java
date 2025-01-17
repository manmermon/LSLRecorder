package lslrec.testing.gui.plugin;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import lslrec.gui.panel.plugin.item.DataProcessingPluginSelectorPanel;
import lslrec.plugin.lslrecPlugin.ILSLRecConfigurablePlugin;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin.PluginType;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;

public class testDataPostProcessingPlugin extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					testDataPostProcessingPlugin frame = new testDataPostProcessingPlugin();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public testDataPostProcessingPlugin() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 650, 500);
		this.contentPane = new JPanel( new BorderLayout());
		this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(this.contentPane);
		
		JButton bt = new JButton( "Refresh" );
		bt.addActionListener( new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				// TODO Auto-generated method stub
				contentPane.removeAll();
				contentPane.add( bt, BorderLayout.NORTH );
				
				contentPane.setVisible( false );
				setPluginPanel();
				contentPane.setVisible( true );
			}
		});
		
		contentPane.add( bt, BorderLayout.NORTH );
		
		setPluginPanel();
		
	}
	
	private void setPluginPanel()
	{
		lslrec.plugin.loader.PluginLoader loader;
		try 
		{
			loader = lslrec.plugin.loader.PluginLoader.getInstance();
			
			int t = 1;
			if( t == 0 )
			{
				List< ILSLRecPlugin > list = loader.getPluginsByType( PluginType.DATA_PROCESSING );
				if( list != null )
				{
					ILSLRecConfigurablePlugin[] plgs = list.toArray( new ILSLRecConfigurablePlugin[ 0 ] );	
					Set< ILSLRecPluginDataProcessing > idPlugins = new HashSet< ILSLRecPluginDataProcessing >();
	
					for( ILSLRecPlugin pl : plgs )
					{
						idPlugins.add( (ILSLRecPluginDataProcessing) pl );
					}
	
					DataProcessingPluginSelectorPanel psp = new DataProcessingPluginSelectorPanel( idPlugins );
	
					contentPane.add( psp, BorderLayout.CENTER );
				}
			}
			else
			{
				List< ILSLRecPlugin > list = loader.getPluginsByType( PluginType.DATA_PROCESSING );
				if( list != null )
				{
					ILSLRecConfigurablePlugin[] plgs = list.toArray( new ILSLRecConfigurablePlugin[ 0 ] );	
					Set< ILSLRecPluginDataProcessing > idPlugins = new HashSet< ILSLRecPluginDataProcessing >();

					for( ILSLRecPlugin pl : plgs )
					{
						idPlugins.add( (ILSLRecPluginDataProcessing) pl );
					}

					List< ILSLRecPluginDataProcessing > plugins = new ArrayList< ILSLRecPluginDataProcessing >( idPlugins );
					Collections.sort( plugins, new Comparator<ILSLRecPlugin>() 
					{
						@Override
						public int compare(ILSLRecPlugin o1, ILSLRecPlugin o2) 
						{
							return o1.getID().compareTo( o2.getID() );
						}
					} );
					DataProcessingPluginSelectorPanel psp = new DataProcessingPluginSelectorPanel( plugins );

					contentPane.add( psp, BorderLayout.CENTER );
				}
			}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
