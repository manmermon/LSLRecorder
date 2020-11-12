/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.plugin.impl.gui.memory;

import java.awt.Point;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * @author Manuel Merino Monge
 *
 */
public class MemoryButton extends JButton 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 9072149303058938249L;
	
	private Point locationInMatrix;
	
	private int figID = 0;
	private int answer = figID;
	
	/**
	 * 
	 */
	public MemoryButton( int f, int c, int figId ) 
	{
		this( f, c, figId, null, null );
	}

	public MemoryButton( int f, int c, int figId, Action a ) 
	{
		super( a );
		
		this.locationInMatrix = new Point( f, c );
		this.figID = figId;
	}
	
	public MemoryButton( int f, int c, int figId, String txt ) 
	{
		this( f, c, figId, txt, null );
	}
	
	public MemoryButton( int f, int c, int figId, Icon ic ) 
	{
		this( f, c, figId, null, ic );
	}
	
	public MemoryButton( int f, int c, int figId, String txt, Icon ic ) 
	{
		super( txt, ic );
		
		this.locationInMatrix = new Point( f, c );
		this.figID = figId;
	}
	
	public Point getLocationInMatrix( )
	{
		return this.locationInMatrix;
	}
	
	public int getFigureID()
	{
		return this.figID;
	}
	
	public void setAnswerFigure( int id )
	{
		this.answer = id;
	}
	
	public boolean isCorrectAnswer()
	{
		return this.figID == this.answer;
	}
	
	public int getAnswer()
	{
		return this.answer;
	}
}

