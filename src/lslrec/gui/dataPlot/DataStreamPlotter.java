package lslrec.gui.dataPlot;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import lslrec.config.language.Language;
import lslrec.dataStream.binary.input.plotter.DataPlotter;
import lslrec.dataStream.binary.input.plotter.StringPlotter;
import lslrec.dataStream.family.DataStreamFactory;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.tools.StreamUtils.StreamDataType;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.stoppableThread.IStoppableThread;

public class DataStreamPlotter 
{
	private static DataStreamPlotter plotter = null;
	
	private DataPlotter ctrLSLDataPlot = null;
	private StringPlotter ctrLSLDataStringPlot = null;
	
	private DataStreamPlotter()
	{
		
	}
	
	public static DataStreamPlotter getInstance()
	{
		if( plotter == null )
		{
			plotter = new DataStreamPlotter();
		}
		
		return plotter;
	}
	
	/**
	 * Delete plot thread.
	 */
	private void disposeLSLDataPlot()
	{
		if (this.ctrLSLDataPlot != null)
		{
			try
			{
				this.ctrLSLDataPlot.stopThread( IStoppableThread.FORCE_STOP );
			}
			catch (Exception e) 
			{
				if( !this.ctrLSLDataPlot.getState().equals( Thread.State.TERMINATED ) )
				{
					this.ctrLSLDataPlot.stopThread( IStoppableThread.FORCE_STOP );
				}
			}
			catch ( Error e)
			{
				if( !this.ctrLSLDataPlot.getState().equals( Thread.State.TERMINATED ) )
				{
					this.ctrLSLDataPlot.stopThread( IStoppableThread.FORCE_STOP );
				}
			}
			
			this.ctrLSLDataPlot = null;
		}
	}
	
	/**
	 * Delete string plot thread.
	 */
	private void disposeLSLDataStringPlot()
	{
		if (this.ctrLSLDataStringPlot != null)
		{
			try
			{
				this.ctrLSLDataStringPlot.stopThread( IStoppableThread.FORCE_STOP );
			}
			catch (Exception e) 
			{
				if( !this.ctrLSLDataStringPlot.getState().equals( Thread.State.TERMINATED ) )
				{
					this.ctrLSLDataStringPlot.stopThread( IStoppableThread.FORCE_STOP );
				}
			}
			catch ( Error e)
			{
				if( !this.ctrLSLDataStringPlot.getState().equals( Thread.State.TERMINATED ) )
				{
					this.ctrLSLDataStringPlot.stopThread( IStoppableThread.FORCE_STOP );
				}
			}
			
			this.ctrLSLDataStringPlot = null;
		}
	}
	
	public void disposeDataPlots()
	{
		this.disposeLSLDataPlot();
		this.disposeLSLDataStringPlot();
	}

	/**
	 * Create a plot.
	 *  
	 * @param PlotPanel 	-> plot panel.
	 * @param streamSetting	-> LSL setting to plot data.
	 */
	public void createLSLDataPlot( JPanel PlotPanel, IStreamSetting streamSetting )
	{
		try
		{
			// Delete plots.
			this.disposeDataPlots();
			
			PlotPanel.setVisible( false );
			PlotPanel.removeAll();
						 
			IStreamSetting[] results = DataStreamFactory.getStreamSettings( );

			IStreamSetting inletInfo = null;

			// Look for the LSL streaming 
			for (int i = 0; i < results.length && inletInfo == null; i++)
			{
				IStreamSetting info = results[i];
				if ( info.uid().equals( streamSetting.uid() ) )
				{
					inletInfo = streamSetting;
				}
			}

			if ( inletInfo != null)
			{
				// Set sampling rate
				double frq = inletInfo.sampling_rate();

				// Data plot queue length								
				int queueLength = ((int)(5.0D * frq)) * inletInfo.getChunkSize();
				if (queueLength < 10)
				{
					queueLength = 100;
				}

				if( inletInfo.data_type() != StreamDataType.string )
				{	
					CanvasStreamDataPlot LSLCanvaPlot = new CanvasStreamDataPlot( queueLength ); 
					
					LSLCanvaPlot.clearData();
					LSLCanvaPlot.clearFilters();
	
					PlotPanel.add( LSLCanvaPlot, BorderLayout.CENTER );
					
					// Plot data
					this.ctrLSLDataPlot = new DataPlotter( LSLCanvaPlot, inletInfo );
					this.ctrLSLDataPlot.startThread();
				}
				else
				{
					JTextPane log = new JTextPane();
					
					PlotPanel.add( new JScrollPane( log ), BorderLayout.CENTER );
					
					// String data plot
					this.ctrLSLDataStringPlot = new StringPlotter( log, inletInfo );
					this.ctrLSLDataStringPlot.startThread();
				}
				
				PlotPanel.setVisible( true );
			}
			else
			{Exception ex = new Exception( Language.getLocalCaption( Language.MSG_LSL_PLOT_ERROR ) );
												
				ExceptionMessage msg = new ExceptionMessage( ex, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionMessage.ERROR_MESSAGE ); 
				ExceptionDialog.showMessageDialog( msg, true, false );
			}
		}
		catch (Exception localException) 
		{
			localException.printStackTrace();
			
			ExceptionMessage msg = new ExceptionMessage( localException, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionMessage.ERROR_MESSAGE ); 
			ExceptionDialog.showMessageDialog( msg, true, true );
		}
		catch (Error localError) 
		{
			localError.printStackTrace();
			
			ExceptionMessage msg = new ExceptionMessage( localError, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionMessage.ERROR_MESSAGE ); 
			ExceptionDialog.showMessageDialog( msg, true, true );
		}
	}
	
	public boolean isPlotingStream( IStreamSetting stream )
	{
		boolean check = false;
		
		if( stream != null )
		{
			if( this.ctrLSLDataPlot != null )
			{
				check = this.ctrLSLDataPlot.getStreamUID().equals( stream.uid() );
			}
			
			if( this.ctrLSLDataStringPlot != null )
			{
				check = check || this.ctrLSLDataPlot.getStreamUID().equals( stream.uid() );
			}
		}
		
		return check;
	}
}
