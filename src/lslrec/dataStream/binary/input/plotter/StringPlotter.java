/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.
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
package lslrec.dataStream.binary.input.plotter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.config.language.Language;
import lslrec.dataStream.binary.input.LSLInStreamDataReceiverTemplate;
import lslrec.dataStream.family.lsl.LSLUtils;
import lslrec.dataStream.setting.DataStreamSetting;
import lslrec.exceptions.ReadInputDataException;
import lslrec.stoppableThread.IStoppableThread;

public class StringPlotter extends LSLInStreamDataReceiverTemplate 
{
	private JTextPane plot;
	private int minDataLengthToDraw = 1;
	private List< String > dataBuffer = null;
	private byte[] strLenBytes = null;
	
	private int maxNumLines = 100; 

	public StringPlotter( JTextPane Plot, DataStreamSetting lslCfg ) throws Exception 
	{
		super( lslCfg );
		
		if( super.streamSetting.getDataType() != LSLUtils.string )
		{
			throw new ReadInputDataException( "Data stream must be a string type" );
		}
		
		if (Plot == null)
		{
			throw new IllegalArgumentException( "Plot is null." );
		}
		
		this.dataBuffer = new ArrayList<String>();
		
		this.strLenBytes = new byte[ super.streamSetting.getStreamInfo().channel_count() * LSLUtils.getDataTypeBytes( super.streamSetting.getStringLegthType() ) ];
		
		this.plot = Plot;
		
		super.setName( this.getClass().getSimpleName() + "-" + super.streamSetting.getStreamName() );
		
		this.plot.setVisible(true);
		
		this.minDataLengthToDraw = (int)( lslCfg.getSamplingRate() * lslCfg.getChunkSize() * super.streamSetting.getStreamInfo().channel_count() * 0.400D ); // 400 ms
		
		if( this.minDataLengthToDraw <= 0 )
		{
			this.minDataLengthToDraw = 1;
		}
		
		this.maxNumLines = ((int)( 5.0D * lslCfg.getSamplingRate() ) ) * lslCfg.getChunkSize();
		if ( this.maxNumLines < 10)
		{
			this.maxNumLines = 100;
		}
	}
	
	@Override
	public String getID() 
	{
		return super.getName();
	}

	@Override
	protected void postCleanUp() throws Exception 
	{
		if( super.notifTask != null )
		{
			super.notifTask.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
			synchronized ( super.notifTask )
			{
				super.notifTask.notify();
			}
		}
	}

	@Override
	protected void managerData(byte[] dataArrayOfBytes, byte[] timeArrayOfBytes) throws Exception 
	{
		System.arraycopy( dataArrayOfBytes, 0, this.strLenBytes, 0, this.strLenBytes.length );
		
		Number[] lens = ConvertTo.ByteArray2ArrayOf( this.strLenBytes, super.streamSetting.getStringLegthType() );
		
		int init = strLenBytes.length;
			
		int itLen = 0;
		List< String > strs = new ArrayList<String>();
		while( init < dataArrayOfBytes.length && itLen < lens.length )
		{
			int L = lens[ itLen ].intValue();
			
			byte[] c = new byte[ L ];
			System.arraycopy( dataArrayOfBytes, init, c, 0, L );
			
			String s = new String( c );
			strs.add( s );
			
			itLen++;
			init += L;
		}
		
		Object[] dat = strs.toArray();
		if( super.streamSetting.isInterleavedData() )
		{
			dat = ConvertTo.Interleaved( dat, super.chunckLength, super.streamSetting.getStreamInfo().channel_count() );
		}
		
		if( dat != null )
		{
			for( Object st : dat )
			{
				this.dataBuffer.add( st.toString() );
				
				itLen++;
			}
			
			if( this.dataBuffer.size() >= this.minDataLengthToDraw )
			{			
				int ch = 0;
				
				for( String str : this.dataBuffer )
				{
					this.appendTextLog( Color.BLACK, str, null, ch );
					
					ch++;
					if( ch >= super.streamSetting.getStreamInfo().channel_count() )
					{
						ch = 0;
					}
				}
				
				this.dataBuffer.clear();
			}
		}
	}
	
	private void appendTextLog( Color c, String s, AttributeSet attr, int channel ) 
	{ 		
		JTextPane log = this.plot;

		StyledDocument doc = log.getStyledDocument();

		Color color = c;

		if( c == null )
		{
			color = Color.BLACK;
		}

		StyleContext sc = StyleContext.getDefaultStyleContext(); 

		AttributeSet attrs = attr;
		if( attrs == null )
		{
			attrs = SimpleAttributeSet.EMPTY;
		}

		AttributeSet aset = sc.addAttribute( attrs , StyleConstants.Foreground, color);
		aset = sc.addAttribute( aset, StyleConstants.Bold, true );

		try 
		{	
			int numLine = 0;

			String t = log.getText();

			if( !t.isEmpty() )
			{
				numLine = t.split("\n").length;
			}

			if( numLine >= this.maxNumLines )
			{
				log.setText( "" );
				numLine = 0;
			}
			
			int nl = s.split( "\n" ).length;

			String numTxt = "";

			if( nl + numLine > numLine )
			{
				numTxt += ( numLine + nl );
			}						
			
			int len = log.getDocument().getLength();
			len = log.getDocument().getLength();
			doc.insertString( len, numTxt + " " + Language.getLocalCaption( Language.LSL_CHANNEL ) + " " + channel + ": ", null );

			len = log.getDocument().getLength();
			doc.insertString( len , s, aset );

			log.setCaretPosition( len + s.length() );
		} 
		catch (BadLocationException e) 
		{
			this.plot.setText( this.plot.getText() + s );
		}
	} 

	@Override
	protected void startMonitor() throws Exception 
	{
	}
}
