function output = gzipdecode(input)

	%GZIPDECODE Decompress input bytes using GZIP.
	%
	%    output = gzipdecode(input)
	%
	% The function takes a compressed byte array INPUT and returns inflated
	% bytes OUTPUT. The INPUT is a result of GZIPENCODE function. The OUTPUT
	% is always an 1-by-N uint8 array. JAVA must be enabled to use the function.
	%
	% See also gzipencode typecast

  
    narginchk( 1, 1 );
	error( javachk( 'jvm' ) );
	if ischar( input )
	
	  warning( 'gzipdecode:inputTypeMismatch', ...
		   'Input is char, but treated as uint8.' );
	  
	  input = uint8( input );
	  
	end
  
	if ~isa( input, 'int8' ) && ~isa( input, 'uint8' )
	
	    error( 'Input must be either int8 or uint8.' );
	end

  javaaddpath( './', 'apache-commons/commons-io-2.11.0.jar' )
  
  inByteArray = javaObject( 'java.io.ByteArrayInputStream', input );
	gzip = javaObject( 'java.util.zip.GZIPInputStream', inByteArray );
	buffer = javaObject( 'java.io.ByteArrayOutputStream' );  
  javaMethod( 'copy', 'org.apache.commons.io.IOUtils', gzip, buffer );
	gzip.close();  
  buffer.close();
	output = typecast( buffer.toByteArray(), 'uint8' )';

end
