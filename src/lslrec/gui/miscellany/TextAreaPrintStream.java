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

/**
 * Class TextAreaPrintStream
 * extends PrintStream.
 * A custom made PrintStream which overrides methods println(String)
 * and print(String).
 * Thus, when the out stream is set as this PrintStream (with System.setOut
 * method), all calls to System.out.println(String) or System.out.print(String)
 * will result in an output stream of characters in the JTextArea given as an
 * argument of the constructor of the class.
 **/

package lslrec.gui.miscellany;

import java.awt.Color;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class TextAreaPrintStream extends PrintStream 
{

    //The JTextPane to wich the output stream will be redirected.
    private JTextPane textArea;

    private AttributeSet attSet;

    /**
     * Method TextAreaPrintStream
     * The constructor of the class.
     * @param the JTextPane to wich the output stream will be redirected.
     * @param a standard output stream (needed by super method)
     **/
    public TextAreaPrintStream( JTextPane area, OutputStream out) 
    {
    	super(out);
    	this.textArea = area;
    	
    	this.attSet = SimpleAttributeSet.EMPTY;
    }

    /**
     * Method println
     * @param the String to be output in the JTextArea textArea (private
     * attribute of the class).
     * After having printed such a String, prints a new line.
     **/
    public void println(String string) 
    {
    	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
    	LocalDateTime now = LocalDateTime.now();  
    	string = dtf.format(now ) + " " + string;   
    	   
    	int len = this.textArea.getDocument().getLength();
    	
    	this.textArea.setCaretPosition( len );
    	this.textArea.setCharacterAttributes( this.attSet, true );
    	
    	StyledDocument doc = this.textArea.getStyledDocument();
    	try 
    	{
			doc.insertString( len, string + "\n", this.attSet );
		} 
    	catch (BadLocationException e) 
    	{
    		this.textArea.setText( this.textArea.getText() + "\n" + string );
		}
    	
    }



    /**
     * Method print
     * @param the String to be output in the JTextArea textArea (private
     * attribute of the class).
     **/
    public void print(String string) 
    {
    	//textArea.append( string );
    	this.println( string );
    }
    
    /**
     * 
     */
    @Override
    public void flush() 
    {
    	super.flush();
    	
    	this.textArea.setText( "" );
    }
    
    public void SetColorText( Color color )
    {    	
    	StyleContext sc = StyleContext.getDefaultStyleContext();
    	
    	AttributeSet attrs = SimpleAttributeSet.EMPTY;
		
    	this.attSet = sc.addAttribute( attrs , StyleConstants.Foreground, color);
    }
    
    public void requestFocus()
    {
    	this.textArea.requestFocusInWindow();
    }
    
    public void transferFocus()
    {
    	this.textArea.transferFocus();
    }
}
