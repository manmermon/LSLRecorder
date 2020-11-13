package lslrec.testing.Others;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class testPasswordDialog 
{
	public testPasswordDialog()
	  {
	    JPasswordField pwd = new JPasswordField(10);
	    int action = JOptionPane.showConfirmDialog(null, pwd,"Enter Password",JOptionPane.OK_CANCEL_OPTION);
	    if(action < 0)JOptionPane.showMessageDialog(null,"Cancel, X or escape key selected");
	    else JOptionPane.showMessageDialog(null,"Your password is "+new String(pwd.getPassword()));
	    System.exit(0);
	  }
	  public static void main(String args[]){new testPasswordDialog();}

}
