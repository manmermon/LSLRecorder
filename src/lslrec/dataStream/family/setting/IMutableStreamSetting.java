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

package lslrec.dataStream.family.setting;

/**
 * 
 * @author Manuel Merino Monge
 *
 */
public interface IMutableStreamSetting extends IStreamSetting
{		
	/**
	 * Set addtional information.
	 * 
	 * @param info
	 */
	public void setAdditionalInfo( String info );

	/**
	 * Set if the LSL streaming is selected.
	 * @param select
	 */
	public void setSelected( boolean select );

	/**
	 * 
	 * @param sync
	 */
	public void setSynchronizationStream( boolean sync );
		
	/**
	 * Set the chunck size.
	 * 
	 * @param size
	 */
	public void setChunckSize( int size );
	
	/**
	 * Set LSL data of channels are interleaved.
	 * 
	 * @param interleaved
	 */
	public void setInterleaveadData( boolean interleaved );
}
