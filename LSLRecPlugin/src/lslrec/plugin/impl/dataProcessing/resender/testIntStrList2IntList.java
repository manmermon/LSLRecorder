package lslrec.plugin.impl.dataProcessing.resender;

import java.util.List;

public class testIntStrList2IntList {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List< Integer > lst = LSLStreamResenderTools.convertIntegerStringList2IntArray( "1,   3" );
		System.out.println("testIntStrList2IntList.main() " + lst);
	}

}
