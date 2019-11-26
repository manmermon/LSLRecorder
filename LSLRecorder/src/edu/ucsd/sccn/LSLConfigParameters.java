/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018-2019 by Manuel Merino Monge <manmermon@dte.us.es>
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

package edu.ucsd.sccn;

import edu.ucsd.sccn.LSL.XMLElement;

/**
 * 
 * @author Manuel Merino Monge
 *
 */
public class LSLConfigParameters
{
	public static final String ID_GENERAL_DESCRIPTION_LABEL = "general";
	public static final String ID_SOCKET_MARK_INFO_LABEL = "socketMarkInfo";

	public static final String ID_LSLREC_SETTING_LABEL = "lslRecSetting";
	public static final String ID_CHUNKSIZE_LABEL = "chunkSize";
	public static final String ID_INTERLEAVED_LABEL = "interleaved";
	
	private final String ID_EXTRA_INFO_LABEL = "extra";	
	
	private int extraCount = 1;
	
	private String uid;
	private String deviceName;
	private String deviceType;
	private String additionalInfo;
	private boolean selectedDevice;
	private String source_id;
	private int chunckSize = 1;
	private boolean interleavedData = false;
	private boolean isSyncStream = false;
	private double samplingRate;
	
	/**
	 * 
	 * @param uid
	 * @param name
	 * @param type
	 * 
	 * LSLConfigParameters( uid, name, type, "", name + type, false, 1, false );
	 *  
	 */
	public LSLConfigParameters( String uid, String name, String type )
	{
		this( uid, name, type, name + type );
	}
	
	/**
	 * 
	 * @param uid
	 * @param name
	 * @param type
	 * @param sourceID
	 * 
	 * LSLConfigParameters( uid, name, type, sourceID, "", false, 1, false );
	 *  
	 */
	public LSLConfigParameters( String uid, String name, String type, String sourceID )
	{
		this( uid, name, type, sourceID, "" );
	}
	
	/**
	 * 
	 * @param uid
	 * @param name
	 * @param type
	 * @param sourceID
	 * @param info
	 * 
	 * LSLConfigParameters( uid, name, type, sourceID, info, false, 1, false );
	 *  
	 */
	public LSLConfigParameters( String uid, String name, String type, String sourceID, String info )
	{
		this( uid, name, type, sourceID, info, false );
	}
	
	/**
	 * 
	 * @param uid
	 * @param name
	 * @param type
	 * @param sourceID
	 * @param info
	 * @param selected
	 * 
	 * LSLConfigParameters( uid, name, type, sourceID, info, selected, 1, false );
	 *  
	 */
	public LSLConfigParameters( String uid, String name, String type, String sourceID, String info, boolean selected )
	{
		this( uid, name, type, sourceID, info, selected, 1 );
	}
	
	/**
	 * 
	 * @param uid
	 * @param name
	 * @param type
	 * @param sourceID
	 * @param info
	 * @param selected
	 * @param chunckSize
	 * 
	 * LSLConfigParameters( uid, name, type, sourceID, info, selected, chunckSize, false );
	 *  
	 */
	public LSLConfigParameters( String uid, String name, String type, String sourceID, String info
								, boolean selected, int chunckSize )
	{
		this( uid, name, type, sourceID, info, selected, chunckSize, false );
	}
	
	/**
	 * LSL settings
	 * 
	 * @param uid			-> LSL UID
	 * @param name			-> LSL name
	 * @param type			-> LSL type value
	 * @param sourceID		-> LSL source ID
	 * @param info			-> LSL additional information
	 * @param selected		-> LSL streaming selected
	 * @param chunckSize	-> LSL chunck size
	 * @param interleaved 	-> LSL data of channels are interleaved.
	 */
	public LSLConfigParameters( String uid, String name, String type, String sourceID, String info
								, boolean selected, int chunckSize, boolean interleaved )
	{
		this( uid, name, type, sourceID, info, selected, chunckSize, interleaved, false, 0 );
	}
	
	/**
	 * LSL Setting.
	 *  
	 * @param uid			-> LSL UID
	 * @param name			-> LSL name
	 * @param type			-> LSL type value
	 * @param sourceID		-> LSL source ID
	 * @param info			-> LSL additional information
	 * @param selected		-> LSL streaming selected
	 * @param chunckSize	-> LSL chunck size
	 * @param interleaved 	-> LSL data of channels are interleaved.
	 * @param synchronization -> Indicate if synchronization marks are caught from this stream. 
	 */
	public LSLConfigParameters( String uid, String name, String type, String sourceID, String info
								, boolean selected, int chunckSize, boolean interleaved, boolean synchronization )
	{
		this( uid, name, type, sourceID, info, selected, chunckSize, interleaved, synchronization, 0 );
	}
	
	/**
	 * LSL Setting.
	 *  
	 * @param uid			-> LSL UID
	 * @param name			-> LSL name
	 * @param type			-> LSL type value
	 * @param sourceID		-> LSL source ID
	 * @param info			-> LSL additional information
	 * @param selected		-> LSL streaming selected
	 * @param chunckSize	-> LSL chunck size
	 * @param interleaved 	-> LSL data of channels are interleaved.
	 * @param synchronization -> Indicate if synchronization marks are caught from this stream.
	 * @param sampling		-> Sampling rate 
	 */
	public LSLConfigParameters( String uid, String name, String type, String sourceID, String info
								, boolean selected, int chunckSize, boolean interleaved, boolean synchronization
								, double sampling )
	{		
		this.uid = uid;
		this.deviceName = name;
		this.deviceType = type;
		this.additionalInfo = info;
		this.selectedDevice = selected;
		this.source_id = sourceID;
		this.chunckSize = chunckSize;
		this.interleavedData = interleaved;
		this.isSyncStream = synchronization;		
		this.samplingRate = sampling;
	}

	/**
	 * 
	 * @return LSL name.
	 */
	public String getDeviceName()
	{
		return this.deviceName;
	}

	/**
	 * Set LSL name.
	 * 
	 * @param name
	 */
	public void setDeviceName( String name )
	{
		this.deviceName = name;
	}

	/**
	 * 
	 * @return LSL type value.
	 */
	public String getDeviceType()
	{
		return this.deviceType;
	}

	/**
	 * Set LSL type value.
	 * 
	 * @param type
	 */
	public void setDeviceType( String type )
	{
		this.deviceType = type;
	}

	/**
	 * 
	 * @return additional information.
	 */
	public String getAdditionalInfo()
	{
		return this.additionalInfo;
	}

	/**
	 * Set addtional information.
	 * 
	 * @param info
	 */
	public void setAdditionalInfo( String info )
	{
		this.additionalInfo = info;
	}
	
	/**
	 * 
	 * @return if is selected the LSL streaming.
	 */
	public boolean isSelected()
	{
		return this.selectedDevice;
	}
	
	/**
	 * Set if the LSL streaming is selected.
	 * @param select
	 */
	public void setSelected( boolean select )
	{
		this.selectedDevice = select;
	}
	
	public void setSynchronizationStream( boolean sync )
	{
		this.isSyncStream = sync;
	}
	
	/**
	 * 
	 * @return LSL UID.
	 */
	public String getUID()
	{
		return this.uid;
	}	
	
	/**
	 * 
	 * @return source ID.
	 */
	public String getSourceID()
	{
		return this.source_id;
	}
	
	/**
	 * 
	 * @return chunck size.
	 */
	public int getChunckSize()
	{
		return this.chunckSize;
	}
	
	/**
	 * Set the chunck size.
	 * 
	 * @param size
	 */
	public void setChunckSize( int size )
	{
		this.chunckSize = size;		
		
		if( this.chunckSize < 1 )
		{
			this.chunckSize = 1;
		}
	}
	
	/**
	 * 
	 * @return If LSL data of channels are interleaved.
	 */
	public boolean isInterleavedData()
	{
		return this.interleavedData;
	}
	
	/**
	 * Set LSL data of channels are interleaved.
	 * 
	 * @param interleaved
	 */
	public void setInterleaveadData( boolean interleaved )
	{
		this.interleavedData = interleaved;
	}
	
	/**
	 * @return Indicate if synchronization marks are caught from this stream.
	 */
	public boolean isSynchronationStream()
	{
		return this.isSyncStream;
	}
	
	/**
	 * 
	 * @return Sampling rate
	 */
	public double getSamplingRate()
	{
		return this.samplingRate;
	}
	
	/**
	 * Set sampling rate.
	 * 
	 * @param sampling
	 */
	public void setSamplingRate( double sampling )
	{
		this.samplingRate = sampling;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "<" + source_id 					// 1
				+ ", " + deviceName 			// 2
				+ ", " + deviceType 			// 3
				+ ", " + additionalInfo 		// 4
				+ ", " + selectedDevice 		// 5
				+ ", " + chunckSize 			// 6
				+ ", " + this.interleavedData	// 7
				+ ", " + this.isSyncStream		// 8
				//+ ", " + this.samplingRate
 				+ ">";
	}	
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() 
	{		
		return ( deviceName + deviceType ).hashCode();
	}
	
	public void increaseExtraCountLabel()
	{
		this.extraCount++;
	}
	
	public String getExtraInfoLabel()
	{
		String lab = this.ID_EXTRA_INFO_LABEL;
		
		if( this.extraCount > 1 )
		{
			lab += this.extraCount;
		}
		
		return lab;
	}
	
	public static int existNodoName( XMLElement child , String label )
	{		
		int countEq = 0;
		while( child != null && !child.name().isEmpty() )
		{
			String name = child.name().toLowerCase();
			if( name.equals( label ) )
			{
				countEq++;
			}
			
			child = child.next_sibling();
		}					
		
		return countEq;
	}
}
