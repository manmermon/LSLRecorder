package lslrec.dataStream.outputDataFile.format.matlab;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.Deflater;

import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.dataStream.family.lsl.LSLUtils;
import lslrec.dataStream.outputDataFile.dataBlock.CharBlock;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlock;
import lslrec.dataStream.outputDataFile.dataBlock.StringBlock;
import lslrec.dataStream.outputDataFile.format.IOutputDataFileWriter;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.format.Mat5File;
import us.hebi.matlab.mat.types.MatFile;
import us.hebi.matlab.mat.types.MatlabType;
import us.hebi.matlab.mat.types.Sink;
import us.hebi.matlab.mat.types.Sinks;
import us.hebi.matlab.mat.util.Casts;

public class OutputMatDataWriter implements IOutputDataFileWriter
{
	private Map< String, StreamingMatrix2D> matStreams = null;
	
	private ITaskMonitor monitor = null;
	
	private File filePath;
	
	private List< String > header = null ;
	
	private AtomicBoolean savedDataBlock = new AtomicBoolean( true );
	
	public OutputMatDataWriter( String file, ITaskMonitor monitor ) throws Exception 
	{
		this.filePath = new File( file );
		
		this.matStreams = new HashMap<String, StreamingMatrix2D>();
		
		this.taskMonitor( monitor );
		
		this.header = new ArrayList<String>();
	}
	
	@Override
	public void taskMonitor(ITaskMonitor monitor) 
	{
		this.monitor = monitor;
	}

	@Override
	public void addMetadata(String id, String text) throws Exception 
	{
		id = id.replace("\n", "").replace("\r", "");
		text = text.replace("\n", "").replace("\r", "");

		this.header.add( id + "=" + text );
	}

	@Override
	public boolean saveData( DataBlock data ) throws Exception 
	{
		this.savedDataBlock.set( false );
		
		boolean res = true;
		
		String varName = data.getName();
		long numCols = data.getNumCols();
				
		StreamingMatrix2D stream = this.matStreams.get( varName );
		
		
		if( stream == null )
		{
			File folder = this.filePath.getAbsoluteFile().getParentFile().getAbsoluteFile();
			stream = StreamingMatrix2D.createRowMajor( folder, varName, numCols, this.getMatlabDataType( data.getDataType() ) );
			this.matStreams.put( varName, stream );
		}
		
		if( data instanceof StringBlock )
		{	
			Character[] d = ((StringBlock) data ).getData();
			
			for( char c : d )
			{
				stream.addValue( c );
			}
		}
		else if( data instanceof CharBlock )
		{
			Character[] d = ((CharBlock) data ).getData();
			
			for( char c : d )
			{
				stream.addValue( c );
			}
		}	
		else
		{
			for( Object n : data.getData() )
			{
				stream.addValue( (Number)n );
			}
		}
	
		this.savedDataBlock.set( true );
		
		return res;
	}
	
	private MatlabType getMatlabDataType( int type )
	{
		MatlabType mtype = MatlabType.Double;
		
		switch ( type ) 
		{
			case LSLUtils.double64:
			{
				mtype = MatlabType.Double;
				break;
			}
			case LSLUtils.float32:
			{
				mtype = MatlabType.Single;
				break;
			}
			case LSLUtils.int64:
			{
				mtype = MatlabType.Int64;
				break;
			}
			case LSLUtils.int32:
			{
				mtype = MatlabType.Int32;
				break;
			}
			case LSLUtils.int16:
			{
				mtype = MatlabType.Int16;
				break;
			}
			case LSLUtils.int8:
			{
				mtype = MatlabType.Int8;
				break;
			}
			case LSLUtils.string:
			{
				mtype = MatlabType.Character;
				break;
			}
			default:
			{
				mtype = null;
				break;
			}
		}
		
		return mtype;
	}

	@Override
	public String getFileName() 
	{
		return this.filePath.getAbsolutePath();
	}

	@Override
	public boolean finished() 
	{
		return this.savedDataBlock.get();
	}

	@Override
	public void close() throws Exception 
	{
		Mat5File matFile = Mat5.newMatFile();
		
		for( String varName : this.matStreams.keySet() )
        {
			StreamingMatrix2D matrix = this.matStreams.get( varName );
       	 
			matFile.addArray( varName, matrix ); // add content
        }
		
        // Write to disk
        try ( MatFile mat = matFile;
             Sink sink = Sinks.newMappedFile( this.filePath, Casts.sint32(matFile.getUncompressedSerializedSize() ) ) ) 
        {
            Mat5.newWriter( sink.nativeOrder() )
                    .enableConcurrentCompression( Executors.newCachedThreadPool() )
                    .setDeflateLevel( Deflater.BEST_SPEED )
                    .writeMat( mat );
        }        
	}
}
