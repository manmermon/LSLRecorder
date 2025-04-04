package lslrec.plugin.impl.dataProcessing.resender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.stream.lsl.LSLStreamInfo;
import lslrec.dataStream.tools.StreamUtils.StreamDataType;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

public class LSLStreamResender extends LSLRecPluginDataProcessing 
{
	public static final String STREAM_NAME = "Stream name"; 
	public static final String SELECTED_CHANNELS = "Selected channels";

	private StreamOutlet outStream;
	
	private Object lock = new Object();
	
	private ParameterList pars = new ParameterList();
	
	private String streamName = "streamResender";
	private StreamDataType dataType = StreamDataType.float32;
	private Boolean[] selectedChannels = new Boolean[] { true }; // all channels
	
	private int bufferLen = 1;
	
	private int channelIndex = 0;
	private int numSelectedChannels = 0;
	
	private List< Number[] > data2Resend = new ArrayList< Number[] >();
	
	public LSLStreamResender( IStreamSetting setting, LSLRecPluginDataProcessing prevProc ) 
	{
		super(setting, prevProc);
		
		this.pars.addParameter( new Parameter< String >( STREAM_NAME, streamName ) );
		
		this.setDefaultSelectedChannels();
		
		this.pars.addParameter( new Parameter< Boolean[] >( SELECTED_CHANNELS, selectedChannels ) );
	}
	
	private void setDefaultSelectedChannels()
	{
		this.selectedChannels = new Boolean[ super.streamSetting.channel_count() ];
		
		for( int i = 0; i < this.selectedChannels.length; i++ )
		{
			this.selectedChannels[ i ] = true;
		}
	}
	
	@Override
	public String getID() 
	{
		return "LSLStreamResender";
	}

	@Override
	protected void finishProcess() 
	{
		if( this.outStream != null )
		{
			this.outStream.close();
			this.outStream = null;
		}
	}

	@Override
	public int getBufferLength() 
	{
		return this.bufferLen;
	}

	@Override
	public int getOverlapOffset() 
	{
		return 0;
	}

	@Override
	public void loadProcessingSettings(List<Parameter<String>> arg0) 
	{
		if( arg0 != null )
		{
			for( Parameter< String > par : arg0 )
			{
				String id = par.getID();
				String val = par.getValue();
				
				switch ( par.getID() )
				{
					case STREAM_NAME:
					{
						try
						{
							synchronized ( this.lock )
							{
								this.streamName = val;
							}
							
							this.pars.getParameter( id ).setValue( this.streamName );
							
						}
						catch (Exception e) 
						{
						}
						
						break;
					}
					case SELECTED_CHANNELS:
					{
						try
						{
							synchronized ( this.lock )
							{
								
								List< Integer > channels = new ArrayList<Integer>( new HashSet< Integer >(  LSLStreamResenderTools.convertIntegerStringList2IntArray( val ) ) );
								
								Collections.sort( channels );
																
								int maxNumChannels = super.streamSetting.channel_count();
								
								while( !channels.isEmpty() && channels.get( channels.size() - 1 ) > maxNumChannels )
								{
									channels = channels.subList( 0 , channels.size() - 1 );
								}
								
								this.setDefaultSelectedChannels();
								if( !channels.isEmpty() && channels.get( 0 ) > 0 )
								{
									for( int i = 0; i < maxNumChannels; i++ )
									{
										boolean state = channels.contains( i +1);
										
										this.selectedChannels[ i ] = state;
									}
								}									
							}
							
							this.pars.getParameter( id ).setValue( this.selectedChannels );							
						}
						catch (Exception e) 
						{
						}
						
						break;
					}
					default:
						break;
				}
			}
			
			int nchannels = 0;
			
			for( boolean sel : this.selectedChannels )
			{
				nchannels = ( sel ) ? nchannels + 1 : nchannels;
			}
			
			this.numSelectedChannels = nchannels;
						
			try 
			{				
				this.createOutStream();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}

	private void createOutStream() throws IOException
	{
		LSLStreamInfo info = new LSLStreamInfo( this.streamName
												, super.streamSetting.content_type()
												, this.numSelectedChannels
												, super.streamSetting.sampling_rate()
												, super.streamSetting.data_type().ordinal()
												, super.streamSetting.source_id() + "-" + this.getID() );

		info.setInterleaveadData( super.streamSetting.isInterleavedData() );
		info.setDescription( super.streamSetting.description() );
		info.setChunckSize( super.streamSetting.getChunkSize() );

		if( this.outStream != null )
		{
			this.outStream.close();
		}
		
		this.outStream = new StreamOutlet( info );
	}
	
	@Override
	protected Number[] processData( Number[] inData ) 
	{
		if( this.outStream != null )
		{
			if( this.selectedChannels[ channelIndex ] )
			{
				this.data2Resend.add( inData );
			}
			
			if( this.data2Resend.size() >= this.numSelectedChannels )
			{	
				int dataLen = 0;
				for( Number[] d : this.data2Resend )
				{
					dataLen += d.length;
				}
							
				Number[] dataArray = new Number[ dataLen ];
				int index = 0;
				for( Number[] data : this.data2Resend )
				{
					for( Number d : data )
					{
						dataArray[ index ] = d;
						index++;
					}
				}
				
				if( super.streamSetting.isInterleavedData() )
				{
					ConvertTo.Transform.Interleaved( dataArray, this.numSelectedChannels, super.streamSetting.getChunkSize() );
				}
											
				switch ( this.dataType ) 
				{
					case int16:
					{
						this.outStream.push_chunk( ConvertTo.Casting.NumberArray2ShortArray( dataArray ) );
						
						break;
					}	
					case int32:
					{
						this.outStream.push_chunk( ConvertTo.Casting.NumberArray2IntegerArray( dataArray ) );
						
						break;
					}
					case int64:
					{
						this.outStream.push_chunk( ConvertTo.Casting.NumberArray2LongArray( dataArray ) );
						
						break;
					}
					case float32:
					{
						this.outStream.push_chunk( ConvertTo.Casting.NumberArray2FloatArray( dataArray ) );
						
						break;
					}
					case double64:
					{
						this.outStream.push_chunk( ConvertTo.Casting.NumberArray2DoubleArray( dataArray ) );
						
						break;
					}
					default:
					{
						this.outStream.push_chunk( ConvertTo.Transform.NumberArray2byteArray( dataArray, StreamDataType.int8 ) );
						
						break;
					}
				}
				
				this.data2Resend.clear();
			}
			
			this.channelIndex++;
			if( this.channelIndex >= super.streamSetting.channel_count() )
			{
				this.channelIndex = 0;
			}
		}
		
		return inData;
	}
}
