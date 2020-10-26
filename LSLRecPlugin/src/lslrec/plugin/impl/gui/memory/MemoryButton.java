/**
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

