/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec and CLIS.
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

package GUI.Miscellany;

import java.io.*;

import javax.swing.*;

public class TextAreaPrintStream extends PrintStream 
{

    //The JTextArea to wich the output stream will be redirected.
    private JTextArea textArea;


    /**
     * Method TextAreaPrintStream
     * The constructor of the class.
     * @param the JTextArea to wich the output stream will be redirected.
     * @param a standard output stream (needed by super method)
     **/
    public TextAreaPrintStream(JTextArea area, OutputStream out) 
    {
    	super(out);
    	this.textArea = area;
    }

    /**
     * Method println
     * @param the String to be output in the JTextArea textArea (private
     * attribute of the class).
     * After having printed such a String, prints a new line.
     **/
    public void println(String string) 
    {
    	this.textArea.append( string + "\n" );
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
}
