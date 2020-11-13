package lslrec.testing.Others;

public class testLaunchBatFile {

	public static void main(String[] args) 
	{		
		try 
			{
				//Process p = Runtime.getRuntime().exec( "cmd /c start " + args[ 0 ] );
				Process p = Runtime.getRuntime().exec( "java -jar G:\\Sync_datos\\WorkSpace\\GitHub\\LSLSerialPort\\LabStreamingLayerSerialPort\\LSLSerialPort.jar  --ini start --port 3 --baud 115200 --name Inputs --sampling 16 --channels 1 --outType float32 --inType string" );
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
	}
}
