package lslrec.testing.jEXL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import lslrec.auxiliar.WarningMessage;
import lslrec.config.ConfigApp;
import lslrec.config.language.Language;
import lslrec.control.message.checklist.CheckMessage;
import lslrec.control.message.checklist.CheckMessagePartFromSetting;
import lslrec.control.message.checklist.CheckMessagePartFromText;
import lslrec.control.message.checklist.ICheckMessagePart;
import lslrec.control.message.checklist.MessageEvaluator;
import lslrec.dataStream.family.setting.IMutableStreamSetting;
import lslrec.dataStream.sync.SyncMethod;
import lslrec.dataStream.tools.StreamUtils;

public class testJexl {

	public static void main(String[] args) 
	{
		//
		//
		//
		CheckMessage msg = new CheckMessage( "syncMethodCheck", CheckMessage.WARNING );
		
		ICheckMessagePart part = new CheckMessagePartFromText( Language.getLocalCaption( Language.CHECK_SYNC_METHOD_WARNING_MSG )
													, "", false );
		part.setIDLangCaption( Language.CHECK_SYNC_METHOD_WARNING_MSG );
		msg.addMessagePart( part );
		msg.setMessageEvaluator( new MessageEvaluator( ConfigApp.SELECTED_SYNC_METHOD, "!({0}.contains(\""+SyncMethod.SYNC_NONE + "\"))", null ) );
		
		WarningMessage res = msg.evaluateMessage();
		System.out.println("testJexl.main() " +res);
		
		//
		//
		//
		msg = new CheckMessage( "specialInputCheck", CheckMessage.WARNING );
		part = new CheckMessagePartFromText( Language.getLocalCaption( Language.CHECK_SPECIAL_IN_WARNING_MSG )
									, "", false );
		part.setIDLangCaption( Language.CHECK_SPECIAL_IN_WARNING_MSG );
		msg.addMessagePart( part );
		msg.setMessageEvaluator( new MessageEvaluator( ConfigApp.IS_ACTIVE_SPECIAL_INPUTS, "{0}==true", null ) );
		res = msg.evaluateMessage();
		System.out.println("testJexl.main() " +res);
		
		//
		//
		//
		msg = new CheckMessage( "chunkSizeCheck", CheckMessage.WARNING );
		part = new CheckMessagePartFromText( Language.getLocalCaption( Language.CHECK_LSL_CHUNCKSIZE_WARNING_MSG )
									, "", false );
		part.setIDLangCaption( Language.CHECK_LSL_CHUNCKSIZE_WARNING_MSG );
		msg.addMessagePart( part );
		res = msg.evaluateMessage();
		System.out.println("testJexl.main() " +res);
		
		//
		//
		//
		msg = new CheckMessage( "numSelectedDataStreamChecker", CheckMessage.ERROR );
		part = new CheckMessagePartFromText( Language.getLocalCaption( Language.MSG_ERROR_NUMBER_SELECTED_DATA_STREAMS )
									, "", false );
		part.setIDLangCaption( Language.MSG_ERROR_NUMBER_SELECTED_DATA_STREAMS );
		msg.addMessagePart( part );
		
		part = new CheckMessagePartFromText( "1", "^\\d+$", true );									
		msg.addMessagePart( part );
	
		List< ICheckMessagePart > parts = new ArrayList<ICheckMessagePart>();
		parts.add( part );
		
		MessageEvaluator ev = new MessageEvaluator( ConfigApp.ID_STREAMS, "", parts )
		{
			public boolean evalue() 
			{
				int nstreams = StreamUtils.getNumberOfSelectedStreams( false );
				int ncmp = Integer.parseInt( parts.get( 0 ).getMessagePart() );
				return  nstreams == ncmp ;
			};
		};
		
		msg.setMessageEvaluator( ev );
		res = msg.evaluateMessage();
		System.out.println("testJexl.main() " +res);
		
		//
		//
		//
		msg = new CheckMessage( "numSelectedSyncStreamChecker", CheckMessage.ERROR );
		part = new CheckMessagePartFromText( Language.getLocalCaption( Language.MSG_ERROR_NUMBER_SELECTED_SYNC_STREAMS )
									, "", false );
		part.setIDLangCaption( Language.MSG_ERROR_NUMBER_SELECTED_SYNC_STREAMS );
		msg.addMessagePart( part );
		
		part = new CheckMessagePartFromText( "1", "^\\d+$", true );									
		msg.addMessagePart( part );
	
		List< ICheckMessagePart > parts2 = new ArrayList< ICheckMessagePart >();
		parts2.add( part );
		
		ev = new MessageEvaluator( ConfigApp.ID_STREAMS, "", parts2 )
		{
			public boolean evalue() 
			{
				int nstreams = StreamUtils.getNumberOfSelectedStreams( true );
				int ncmp = Integer.parseInt( parts2.get( 0 ).getMessagePart() );
				return  nstreams == ncmp ;
			};
		};
		
		msg.setMessageEvaluator( ev );
		res = msg.evaluateMessage();
		System.out.println("testJexl.main() " +res);
		
		//
		//
		//
		msg = new CheckMessage( "subjectIDChecker", CheckMessage.WARNING );
		part = new CheckMessagePartFromText( Language.getLocalCaption( Language.CHECK_SUBJECT_IDS_WARNING_MSG )
									, "", false );
		part.setIDLangCaption( Language.CHECK_SUBJECT_IDS_WARNING_MSG );
		msg.addMessagePart( part );
		
		part = new CheckMessagePartFromText( Language.getLocalCaption( Language.SUBJECT_ID_TEXT ), "", false );
		part.setIDLangCaption( Language.SUBJECT_ID_TEXT );
		msg.addMessagePart( part );
		
		part = new CheckMessagePartFromSetting( ConfigApp.OUTPUT_SUBJ_ID );
		msg.addMessagePart( part );

		//ConfigApp.setProperty( ConfigApp.OUTPUT_SUBJ_ID, "TEST_SJ");
		res = msg.evaluateMessage();
		System.out.println("testJexl.main() " +res);
		
		//
		//
		//
		msg = new CheckMessage( "sessionIDChecker", CheckMessage.WARNING );
		part = new CheckMessagePartFromText( Language.getLocalCaption( Language.CHECK_SESSION_IDS_WARNING_MSG )
									, "", false );
		part.setIDLangCaption( Language.CHECK_SESSION_IDS_WARNING_MSG);
		msg.addMessagePart( part );
		
		part = new CheckMessagePartFromText( Language.getLocalCaption( Language.TEST_ID_TEXT ), "", false );
		part.setIDLangCaption( Language.TEST_ID_TEXT );
		msg.addMessagePart( part );
		
		part = new CheckMessagePartFromSetting( ConfigApp.OUTPUT_TEST_ID );
		msg.addMessagePart( part );

		//ConfigApp.setProperty( ConfigApp.OUTPUT_TEST_ID, "TEST_SESSION");
		res = msg.evaluateMessage();
		System.out.println("testJexl.main() " +res);
		
		//
		//
		//
		msg = new CheckMessage( "emptySessionIDChecker", CheckMessage.ERROR );
		part = new CheckMessagePartFromText( "Sessiong id empty", "", false );
		msg.addMessagePart( part );
		
		msg.setMessageEvaluator( new MessageEvaluator( ConfigApp.OUTPUT_TEST_ID, "{0}!=null && !{0}.trim().isEmpty()", null ) );
		res = msg.evaluateMessage();
		System.out.println("testJexl.main() " +res);
		
		//
		//
		//
		msg = new CheckMessage( "emptySubjectIDChecker", CheckMessage.ERROR );
		part = new CheckMessagePartFromText( "Subject id empty", "", false );
		msg.addMessagePart( part );
		
		msg.setMessageEvaluator( new MessageEvaluator( ConfigApp.OUTPUT_SUBJ_ID, "{0}!=null && !{0}.trim().isEmpty()", null ) );
		res = msg.evaluateMessage();
		System.out.println("testJexl.main() " +res);
		
	}
}
