function output = bzip2decode( input )

	%BZIP2DECODE Decompress input bytes using BZIP2.
	%
	%    output = bzip2decode(input)
	%
	% The function takes a compressed byte array INPUT and returns inflated
	% bytes OUTPUT. The INPUT is a result of BZIP2 ENCODE function. The OUTPUT
	% is always an 1-by-N uint8 array. JAVA must be enabled to use the function.
	%

    narginchk( 1, 1 );
	error( javachk( 'jvm' ) );
	if ischar( input )
	
	  warning( 'bzip2decode:inputTypeMismatch', ...
                'Input is char, but treated as uint8.' );
	  
	  input = uint8( input );
	  
	end
  
	if ~isa( input, 'int8' ) && ~isa( input, 'uint8' )
	
	    error( 'Input must be either int8 or uint8.' );
        
    end

   javaaddpath( './', 'apache-commons/commons-io-2.11.0.jar' )
   javaaddpath( './', 'apache-commons/commons-compress-1.21.jar' )
    
  inByteArray = javaObject( 'java.io.ByteArrayInputStream', input );
	bzip2 =  javaObject( 'org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream', inByteArray );
	buffer = javaObject( 'java.io.ByteArrayOutputStream' );  
	
  javaMethod( 'copy', 'org.apache.commons.io.IOUtils', bzip2, buffer );  
  
  bzip2.close();
  buffer.close();
    
	output = typecast( buffer.toByteArray(), 'uint8' )';

end
