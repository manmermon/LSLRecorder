function [data, info] = importCLISData( filename, opts )

    %%data = importCLISData( filename, opts )
    %%
    %% Import data from a CLIS file.
    %%
    %% INPUTS
    %%========
    %%filename	-> CLIS file path.
    %%opts      -> Optional. Only for version 2 or greater of CLIS. 
    %%              Cell array of attributes. Each attribute is a
    %%              a tuple of 2 elements: identifier (text) and values.  
    %%     List of tuple of attributes:
    %%
    %%    {'rank', values, 'selVarIndexes', indexes, 'selVarNames', names, 'info', fields }
    %%
    %%    'rank', values:
    %%               Vector with the interval of selected rows to
    %%               import. If is empty (rank = []), then all rows are 
    %%               selected (default). If rank is a scalar K, the rows
    %%               from K up to end are selected. If this attribute is
    %%               missing, all rows are selected.
    %%               Examples:                   
    %%                  {'rank',[]}       -> data = D( :, : );
    %%                   {'rank',10}       -> data = D( 10:end, : );
    %%                   {'rank',[10 100]} -> data = D( 10:100, :);
    %%
    %%   'selVarIndexes', indexes:
    %%               Vector of integers with the indexes of selected 
    %%               variables. All valirables are selected if vector is
    %%               empty or contains a value less than or equal to 0. 
    %%               Examples:
    %%                   {'selVarIndexes', []}   ->  All selected variables.
    %%                   {'selVarIndexes', 2}    -> Select the second variable.
    %%                   {'selVarIndexes',[2 5]} -> Select the second and fifth variables.
    %%                   {'selVarIndexes',[2 5 0]} -> All selected variables.
    %%                   {'selVarIndexes',[2 5 -1]} -> All selected variables.
    %%
    %%   'selVarNames', names:
    %%               Cell array with the names of selected variables. All
    %%               valirables are selected if array is empty. Empty text
    %%               strings are ignored.
    %%               Examples:
    %%                   {'selVarNames', {}}   ->  All selected variables.
    %%                   {'selVarNames', ''}   ->  All selected variables.
    %%                   {'selVarNames', {''}}   ->  All selected variables.
    %%                   {'selVarNames', 'var1'} -> Select the variable whose name is 'var1'.
    %%                   {'selVarNames', {'var1'} } -> Select the variable whose name is 'var1'.
    %%                   {'selVarNames', {'var1', 'var3'} } -> Select the variables whose names are 'var1' and 'var3'.
    %%                   {'selVarNames', {'var1', '', 'var3', ''} } -> Select the variables whose names are 'var1' and 'var3'.
	%%
    %%   
    %%   The options 'selVarIndexes' and 'selVarNames' are incompatible.
    %%                  
    %% OUTPUTS
    %%========
    %%data 		-> struct with variables in CLIS file. 
    %%              
    %%info      -> Information from the file. Only for version 2 or greater of CLIS. 
    
    try
        
        info = struct();
        
        rank = [];
        selVarIndexes = [];
        
        if nargin < 2
            
            opts = {};
            
        end
        
        NOpts = length( opts );
        
        if mod( NOpts, 2 ) > 0
            
            error( '%s%s', 'Length of cell array of attributes is not correct.',...
                'This must contain tuple of 2 elements: identifier and values.' );
            
        end
        
        if NOpts > 0
            
            keys    = {'selVarIndexes'  , 'selVarNames' };
            values  = {'selVarNames'    , 'selVarIndexes' };
            optsIncompatibles = containers.Map( keys, values );
            regOpts = cell( NOpts/2, 1 );
            iRegOpts = 1;
            for i = 2 : 2 : NOpts
                
                id = opts{ i - 1 };
                values = opts{ i };
                
                if ~ischar( id )
                    
                    error( 'The attribute identifier must be a text string' );
                    
                end
                
                incompatibles = findStrInCell( optsIncompatibles.keys, id, 1, 1 );
                if ~isempty( incompatibles )
                    
                    val = optsIncompatibles( id );
                    if ~isempty( findStrInCell( regOpts( 1 : ( iRegOpts - 1 ) ), val ) )
                        
                        error( 'Options %s and %s are incompatibles.', id, val );
                        
                    end
                    
                end
                
                if strcmpi( id, 'rank' )
                    
                    rank = values;
                    
                elseif strcmpi( id, 'selVarIndexes' )
                    
                    selVarIndexes = sort( values(:));
                    
                    if ~isempty( selVarIndexes ) && selVarIndexes( 1 ) <= 0
                        
                        selVarIndexes = 0;
                        
                    end
                    
                elseif strcmpi( id, 'selVarNames' )
                    
                    if ischar( values )
                        
                        values = { values };
                        
                    end
                    
                    if ~iscellstr( values )
                        
                        error( 'Variable names must be a cell array of strings.') ;
                        
                    end
                    
                    values( cellfun( 'isempty', values) ) = [];
                    
                    selVarIndexes = unique( values );
                    
                elseif strcmpi( id, 'info' )
                    
                    if ~iscellstr( values )
                        
                        error( 'Info must be a cell array of strings.') ;
                        
                    end
                    
                    infoFields = values;
                    
                else
                    
                    error( '%s%s%s', 'Option ', id, ' unknown' );
                    
                end
                
                regOpts{ iRegOpts } = id;
                iRegOpts = iRegOpts +1;
                
            end
            
        end
        
        rank = rank(:);
        validateattributes( rank, {'numeric'},{'nonnegative', 'nonzero' }, 2 )
        
        if length( rank ) > 2
            
            error( 'rank is not a vector of 0, 1, or 2 elements.' );
            
        end
        
        if length( rank ) == 2
            
            if diff( rank ) < 0
                
                error( 'Expected input number 2 to be increasing valued or equals.' );
                
            end
            
        end
        
        fid = fopen( filename, 'r', 'n' );
        
        header = fgetl( fid );
        
        headerFields = strsplit( header, ';' );
        
        headerFields = headerFields( ~cellfun( @isempty, headerFields ) );
        
        vers = headerFields{ 1 };
        fields = headerFields( 2 : end );
        
        vs = strsplit( vers, '=' );
        

        if length( vs ) ~= 2 || ~strcmpi( vs{ 1 }, 'ver' )

            errorManager( 0, fid );

        else

            v = str2double( vs{ end } );

            switch v

                case 1

                    data = importCLISDataV1( fields, fid );

                case 2

                    [data, info] = importCLISDataV2( fields, fid, length( header ), rank, selVarIndexes );
					
				case 2.1
				
					[data, info] = importCLISDataV2_1( fields, fid, length( header ), rank, selVarIndexes );

                otherwise

                    errorManager( 1, fid );

            end
        end

    catch ME

        try

            fclose( fid );
        catch 
        end
        
        clearvars -except ME
        clearvars -GLOBAL
        
        rethrow( ME )
        
    end
end

function errorManager( type, fid )

    clearvars -except fid type
    clearvars -GLOBAL
    
    fclose( fid );
        
    switch type

        case 0
            error( 'File format error' );

        case 1
            error( 'Version unsupported' );

        case 2
            error( 'Undecoding algorythm unsupported' );

        case 3
            error( 'Data type unknow' );
            
        case 4
            error( 'Header: format number is not a non-zero,positive integer number' );
            
        case 5
            error( 'Header size is smaller than its text' );
            
        case 6
            error( 'Incorrect decrypt key' );
            
        otherwise
            error( 'Unknow error' );
    end

end

function data = checkHeaderV1( headerFields, fid )

    data.version = 1.0;
    data.compress = 'GZip';
    data.variables = {};
    data.header = 1;

    compress = headerFields{ 1 };
    vars = headerFields( 2 : end-1 );
    headerInfo = headerFields{ end };

    %
    % Header
    %
    hInfo = strsplit( headerInfo, ',' );

    if length( hInfo ) ~= 2

        errorManager( 0, fid );

    elseif ~strcmpi( 'header', hInfo{ 1 } )

        errorManager( 0, fid );

    elseif strcmpi( hInfo{ 2 }, 'false' )

        data.header = 0;

    elseif ~strcmpi( hInfo{ 2 }, 'true' )

        errorManager( 0, fid );

    end

    %
    % Compress technique
    %
    comp = strsplit( compress, '=' );
    if length( comp ) ~= 2

        errorManager( 0, fid );

    elseif ~strcmpi( 'compress', comp{ 1 } )

        errorManager( 0, fid );

    else

        data.compress = comp{ 2 };

    end

    %
    % variable info
    %
    variables = cell( length( vars ), 6 );

    for iVar = 1 : length( vars )

        v = strsplit( vars{ iVar }, ',' );

        if length( v ) ~= size( variables, 2 )

            errorManager( 0, fid );

        else

            varName = v{ 1 };
            varType = v{ 2 };
            varTypeBytes = convertStr2Integer( v{ 3 }, fid );
            checkVaribleType( varType, varTypeBytes, fid );

            varBytes = convertStr2Integer( v{ 4 }, fid );
            varCols = convertStr2Integer( v{ 5 }, fid );
            varRows = convertStr2Integer( v{ 6 }, fid );

            variables{ iVar, 1 } = varName;
            variables{ iVar, 2 } = varType;
            variables{ iVar, 3 } = varTypeBytes;
            variables{ iVar, 4 } = varBytes;
            variables{ iVar, 5 } = varCols;
            variables{ iVar, 6 } = varRows;
        end

    end

    data.variables = variables;
end

function data = checkHeaderV2( headerFields, fid )

    data.version = 2.0;
    data.compress = 'GZip';
    data.variables = {};
    data.header = 1;  % logical value: 1 -> True; 0 -> False

    compress = headerFields{ 1 };
    
    headerByteSize = headerFields{ 2 };
    
    vars = headerFields( 3 : end-1 );
    headerInfo = headerFields{ end };

    %
    % Header
    %
    hInfo = strsplit( headerInfo, ',' );

    if length( hInfo ) ~= 2

        errorManager( 0, fid );

    elseif ~strcmpi( 'header', hInfo{ 1 } )

        errorManager( 0, fid );

    elseif strcmpi( hInfo{ 2 }, 'false' )

        data.header = 0;

    elseif ~strcmpi( hInfo{ 2 }, 'true' )

        errorManager( 0, fid );

    end

    %
    % Compress technique
    %
    comp = strsplit( compress, '=' );
    if length( comp ) ~= 2

        errorManager( 0, fid );

    elseif ~strcmpi( 'compress', comp{ 1 } )

        errorManager( 0, fid );

    else

        data.compress = comp{ 2 };

    end

    %
    % Header Size
    %
    
    hSize = strsplit( headerByteSize, '=' );
    if length( hSize ) ~= 2

        errorManager( 0, fid );

    elseif ~strcmpi( 'headerByteSize', hSize{ 1 } )

        errorManager( 0, fid );

    else
        
        data.headerByteSize = convertStr2Integer( hSize{ 2 }, fid );
        
        if ( data.headerByteSize <= 0 )
           
            errorManager( 4, fid );
            
        end

    end
    
    %
    % variable info
    %
    
    N = cellfun( @(x) length( strsplit( x, ',' ) ), vars, 'UniformOutput', true );
    N = max( N );
    variables = cell( length( vars ), N );

    for iVar = 1 : length( vars )

        v = strsplit( vars{ iVar }, ',' );

        if length( v ) < 5

            errorManager( 0, fid );

        else

            varName = v{ 1 };
            varType = v{ 2 };
            varTypeBytes = convertStr2Integer( v{ 3 }, fid );
            checkVaribleType( varType, varTypeBytes, fid );

            varCols= convertStr2Integer( v{ 4 }, fid );
            
            varBytes = zeros( 1, N - 4 );
            varBytes( 1 : ( length( v ) - 5 + 1 ) )= convertStr2Integer( v( 5 : end ), fid );

            variables{ iVar, 1 } = varName;
            variables{ iVar, 2 } = varType;
            variables{ iVar, 3 } = varTypeBytes;
            variables{ iVar, 4 } = varCols;
            variables( iVar, 5 : end ) = num2cell( varBytes );
        end

    end

    data.variables = variables;
end

function data = checkHeaderV2_1( headerFields, fid )

    headerV2 = headerFields( 1 : ( end - 2 ) );
    headerV2{ end + 1 } = 'header,false';
        
    data = checkHeaderV2(  headerV2, fid ); 
    
    data.version = 2.1;
    data.header = 0;  % data header length
	
    %
    % Extensions
    %
    
    data.extension = 0;
    
    if strcmpi( headerFields{ end }, 'true' )
        
        data.extension = 1;
        
    end
    
    if data.extension
        
        headerExtension = fgetl( fid );
        
        if ~isempty( headerExtension )

            extensions = strsplit( headerExtension, ';' );
            
            encryptHeaderInfo = strsplit( extensions{ 1 }, '=' );
            checksumHeaderInfo = strsplit( extensions{ 2 }, '=' );
            
            %
            % Checksum
            %
            
            if length( checksumHeaderInfo ) ~= 2
                
                errorManager( 0, fid );
                
            elseif ~strcmpi( 'checksum', checksumHeaderInfo{ 1 } )
                
                errorManager( 0, fid );
                
            else
                
                data.checksum = checksumHeaderInfo{ 2 };                
                
            end
            
            %
            % Encrypt
            %
            
            if length( encryptHeaderInfo ) ~= 2
                
                errorManager( 0, fid );
                
            elseif ~strcmpi( 'encrypt', encryptHeaderInfo{ 1 } )
                
                errorManager( 0, fid );
                
            else
                
                data.encrypt = str2double( encryptHeaderInfo{ 2 } );                
                
            end
            
            if data.encrypt > 0 
               
                aes = ClisDecryptAES( );
                
                encryptLen = data.encrypt;
                
                encryptedKey = fread( fid, encryptLen, 'int8' );
                
                %try
                    
                    if ~aes.checkEncryptPassword( encryptedKey )

                        errorManager( 6, fid );

                    end
                    
                %catch
                    
                 %   errorManager( 6, fid );
                    
                %end
                
                data.AES = aes;
                
                clear encryptedKey aes
                
            end
        end
    
    end
        
    %
    % Header
    %
	
	headerLength = headerFields{ end - 1};
	
    hInfo = strsplit( headerLength, ',' );

    if length( hInfo ) ~= 2

        errorManager( 0, fid );

    elseif ~strcmpi( 'header', hInfo{ 1 } )

        errorManager( 0, fid );

    else
		
        data.header = str2double( hInfo{ 2 } );
		
		if isnan( data.header ) || isinf( data.header )
		
			errorManager( 0, fid );
		
		end

    end
    
    if data.header > 0

        headerLen = data.header;
        
        if isfield( data, 'AES' )
        
            h = fread( fid, headerLen, 'int8' );        
            
            h = data.AES.decrypt( h );
            
            data.header = sprintf( '%s', h );
            
        else
            
            h = fread( fid, headerLen, 'char' );        
            data.header = sprintf( '%s', h );
            
        end
        
    else
        
        data.header = [];
        
    end
    
    skip = data.headerByteSize - ftell( fid );
    fseek( fid, skip, 0 ); % Header padding
    
end

function value = convertStr2Integer( str, fid )

    d = str2double( str );
    value = uint32( d );

    if sum( fix( d ) - d ) ~= 0
        
        value = [];
        
    end
            
    if isempty( value )

        errorManager( 4, fid );

    end

end

function checkVaribleType( type, bytes, fid )

    if ( ~strcmpi( type, 'int' ) ...
            && ~strcmpi( type, 'float' ) ...
            && ~strcmpi( type, 'char' ) ) ...
            ||( bytes ~=1 && bytes ~= 2 && bytes ~= 4 && bytes ~= 8 )

        errorManager( 3, fid );

    elseif( strcmpi( type, 'float' ) ) && bytes < 4
            
        errorManager( 3, fid );
        
    end

end

function data = importCLISDataV1( headerFields, fid )

    info = checkHeaderV1( headerFields, fid );

    if info.header

        data.header = fgetl( fid );

    end

    vars = info.variables;

    for iVars = 1 : size( vars, 1 )

        varName = vars{ iVars, 1 };
        varTypeBytes = vars{ iVars, 3 };
        varType = convertType2MatlabType( vars{ iVars, 2 }, varTypeBytes );
        varBytes = vars{ iVars, 4 };
        varCols = vars{ iVars, 5 };
        varRows = vars{ iVars, 6 };

        if varBytes > 0

            if varCols < 1

                varCols = 1;

            end

            if varRows < 1

                varRows = 1;

            end

            compressDATA = fread( fid, varBytes, 'uint8' );
            compressDATA = uint8( compressDATA );
            DATA =  castTo( varType, varTypeBytes, decodeData( info.compress, compressDATA ), 1 );

            if varCols == 1

                DATA = DATA( : );

            elseif varRows == 1

                DATA = DATA( : )';

            else
                DATA = reshape( DATA, varCols, varRows )';

            end

            data.( strrep( varName, '-', '_' ) ) = DATA;

        end

    end

    fclose( fid );

end

function [data, infoClis] = importCLISDataV2( headerFields, fid, headerLength, rank, selVarIndexes )

    infoClis = struct();
    
    info = checkHeaderV2( headerFields, fid );
    
    infoClis.varNames =  info.variables( :,1 );
    
    if iscell( selVarIndexes )
       
        aux =  infoClis.varNames;
        
        aux = strrep( aux, '-', '_' );
                
        aux2 = selVarIndexes( ~ismember( selVarIndexes, aux ) );
        
        if ~isempty( aux2 )
           
            unknknowVars = '';
            vars = '';
            
            for i = 1 : length( aux2 )
               
                unknknowVars = sprintf( '%s%s', unknknowVars, aux2{ i } );
                
                if i < length( aux2 )
                    
                    unknknowVars = sprintf( '%s%s', unknknowVars, ', ' );
                end
                
            end
            
            for i = 1 : length( aux )
               
                vars = sprintf( '%s%s', vars, aux{ i } );
                
                if i < length( aux )
                    
                    vars = sprintf( '%s%s', vars, ', ' );
                end
                
            end
            
            error( 'File variables are %s. Input selected name(s) %s unknown.', vars, unknknowVars );
            
        end
                
        selVarIndexes = findStrInCell( aux, selVarIndexes, 1, 1, 1 );
        
    end
    
    
    if info.header

        data.header = fgetl( fid );
        headerLength = headerLength + length( data.header );
        
    end

    skip = info.headerByteSize - headerLength - 2; % 2 correspond to \n
    fseek( fid, skip, 0 ); % Header padding
    clear skip;
    
    vars = info.variables;
    
    infoClis.varDims = zeros( length( infoClis.varNames ), 2 );
    d = cell2mat( vars( :, 4 ) );
    infoClis.varDims( :, 2 ) = d( : );
        
    for iVars = 1 : size( vars, 1 )

        nonIgnoredVar = true;
        
        if isscalar( selVarIndexes ) && selVarIndexes > 0
            
            nonIgnoredVar = any( ismember( selVarIndexes, iVars ) );
            
        end
        
        varName = vars{ iVars, 1 };
        varTypeBytes = vars{ iVars, 3 };
        varType = convertType2MatlabType( vars{ iVars, 2 }, varTypeBytes );
        varCols = double( vars{ iVars, 4 } );
        varBytes = vars( iVars, 5 : end );
        
        iVar = length( varBytes );
        while iVar > 0 && varBytes{ iVar } == 0
           
            varBytes( iVar ) = [];
            iVar = iVar - 1;
        end
        clear iVar;
        
        if varCols < 1
            
            varCols = 1;
            
        end
                
        firstElement = 1;
        lastSelElement = +Inf;
        if ~isempty( rank )
           
            firstElement = ( rank( 1 ) - 1 ) * varCols + 1;
            
        end
        
        if length( rank ) > 1
           
            lastSelElement = rank( 2 ) * varCols;
            
        end
                
        maxArraySize = diff( rank ) + 1;
        
        dataSize = +Inf;
        
        if ~isempty( maxArraySize )
            
            dataSize = maxArraySize * varCols;
                        
        end
        
        userMemory = memory;
        maxArraySize = (( userMemory.MemAvailableAllArrays - userMemory.MemUsedMATLAB )/ varTypeBytes);
        
        if dataSize > maxArraySize 
        
            dataSize = length( varBytes ) * ( 5 * 2^20) / varTypeBytes;
            dataSize = ( dataSize - firstElement + 1 ) * varCols;
            
            if dataSize > maxArraySize 
                
                dataSize = maxArraySize;
                
            end
        
        end
        
        clear userMemory maxArraySize;
        
        if nonIgnoredVar
        
            DATA = allocationArray( [ dataSize 1], varType );
            
        end
        
        indDATA = 1;
        countElements = 0;                
        for iVBytes = 1 : length( varBytes )
            
            bytes = varBytes{ iVBytes };
            
            if bytes > 0
                
                if countElements < lastSelElement && nonIgnoredVar
                
                    compressDATA = fread( fid, bytes, 'uint8' );
                    
                    compressDATA = uint8( compressDATA );
                    D =  castTo( varType, varTypeBytes, decodeData( info.compress, compressDATA ), 1 );
                                            
                    nElementsInD = length( D ); % / varCols;
                    
                    selElement1 = nElementsInD + 1;
                    selElement2 = nElementsInD;
                    
                    if ceil( countElements + nElementsInD ) >= firstElement
                        
                        selElement1 = firstElement - countElements;                        
                        
                        if selElement1 <= 0
                            
                            selElement1 = 1;
                        end
                        
                    end
                    
                    if ceil( countElements + nElementsInD ) >= lastSelElement
                        
                        selElement2 = lastSelElement - countElements;
                        if selElement2 > nElementsInD
                            
                            selElement2 = nElementsInD;
                            
                        end                        
                        
                    end
                                        
                    if selElement1 <= selElement2
                        
                        %selRow1 = ( selRow1 - 1 ) * varCols + 1;                        
                        %selRow2 = selRow2 * varCols;
                        
                        %selRow1 = fix( selRow1 );
                        %selRow2 = ceil( selRow2 );
                        %DATA( indDATA : ( indDATA + length( D ) -1 ) ) = D(:);
                        %indDATA = indDATA + length( D );
                        %[ (selRow2 - selRow1 + 1 ) length( D ) ]
                        DATA( indDATA : ( indDATA + ( selElement2 - selElement1 ) ) ) = D( selElement1 : selElement2 );
                        indDATA = indDATA + (selElement2 - selElement1 + 1);
                    
                    end
                    
                    countElements = countElements + nElementsInD;
                
                else
                    
                    fseek( fid, bytes, 0 );
                    
                end
                
                clear D compressDATA;
                
            end
        end
        
        if nonIgnoredVar
        
            DATA = DATA( 1 : ( indDATA - 1 ) ); 
            % DO NOT USE: DATA( indDATA : end ) = [] 
            % MEMORY PROBLEMS

            if ~isempty( DATA )

                if varCols > 1

                    varRows = length( DATA ) / varCols;            

                    DATA = reshape( DATA, varCols, varRows )';
                    
                    infoClis.varDims( iVars, 1 ) = varRows;

                end

                if ~strcmpi( varType, 'char' )

                    data.( strrep( varName, '-', '_' ) ) = DATA;

                else

                    data.( strrep( varName, '-', '_' ) ) = strcat( DATA )';

                end

            end
            
        end

    end
        
    fclose( fid );

end 

function [data, infoClis] = importCLISDataV2_1( headerFields, fid, headerLength, rank, selVarIndexes )

    infoClis = struct();
    
    info = checkHeaderV2_1( headerFields, fid );
    
    infoClis.varNames =  info.variables( :,1 );
    
    if iscell( selVarIndexes )
       
        aux =  infoClis.varNames;
        
        aux = strrep( aux, '-', '_' );
                
        aux2 = selVarIndexes( ~ismember( selVarIndexes, aux ) );
        
        if ~isempty( aux2 )
           
            unknknowVars = '';
            vars = '';
            
            for i = 1 : length( aux2 )
               
                unknknowVars = sprintf( '%s%s', unknknowVars, aux2{ i } );
                
                if i < length( aux2 )
                    
                    unknknowVars = sprintf( '%s%s', unknknowVars, ', ' );
                end
                
            end
            
            for i = 1 : length( aux )
               
                vars = sprintf( '%s%s', vars, aux{ i } );
                
                if i < length( aux )
                    
                    vars = sprintf( '%s%s', vars, ', ' );
                end
                
            end
            
            error( 'File variables are %s. Input selected name(s) %s unknown.', vars, unknknowVars );
            
        end
                
        selVarIndexes = findStrInCell( aux, selVarIndexes, 1, 1, 1 );
        
    end
    
    if isfield( info, 'header' )

        data.header = info.header;
        
    end

    %skip = info.headerByteSize - headerLength - 2; % 2 correspond to \n
    %fseek( fid, skip, 0 ); % Header padding
    %clear skip;
    
    AES = [];
    
    readDataType = 'uint8';
    
    if isfield( info, 'AES' );
        
        AES = info.AES;
        readDataType = 'int8';
    end
    
    vars = info.variables;
    
    infoClis.varDims = zeros( length( infoClis.varNames ), 2 );
    d = cell2mat( vars( :, 4 ) );
    infoClis.varDims( :, 2 ) = d( : );
        
    for iVars = 1 : size( vars, 1 )

        nonIgnoredVar = true;
        
        if isscalar( selVarIndexes ) && selVarIndexes > 0
            
            nonIgnoredVar = any( ismember( selVarIndexes, iVars ) );
            
        end
        
        varName = vars{ iVars, 1 };
        varTypeBytes = vars{ iVars, 3 };
        varType = convertType2MatlabType( vars{ iVars, 2 }, varTypeBytes );
        varCols = double( vars{ iVars, 4 } );
        varBytes = vars( iVars, 5 : end );
        
        iVar = length( varBytes );
        while iVar > 0 && varBytes{ iVar } == 0
           
            varBytes( iVar ) = [];
            iVar = iVar - 1;
        end
        clear iVar;
        
        if varCols < 1
            
            varCols = 1;
            
        end
                
        firstElement = 1;
        lastSelElement = +Inf;
        if ~isempty( rank )
           
            firstElement = ( rank( 1 ) - 1 ) * varCols + 1;
            
        end
        
        if length( rank ) > 1
           
            lastSelElement = rank( 2 ) * varCols;
            
        end
                
        maxArraySize = diff( rank ) + 1;
        
        dataSize = +Inf;
        
        if ~isempty( maxArraySize )
            
            dataSize = maxArraySize * varCols;
                        
        end
        
        [~,userMemory] = computer();
        maxArraySize = ( userMemory / 2 )/ varTypeBytes;
        
        if dataSize > maxArraySize 
        
            dataSize = length( varBytes ) * ( 5 * 2^20) / varTypeBytes;
            dataSize = ( dataSize - firstElement + 1 ) * varCols;
            
            if dataSize > maxArraySize 
                
                dataSize = maxArraySize;
                
            end
        
        end
        
        clear userMemory maxArraySize;
        
        if nonIgnoredVar
        
            DATA = allocationArray( [ dataSize 1], varType );
            
        end
        
        indDATA = 1;
        countElements = 0;                
        for iVBytes = 1 : length( varBytes )
            
            bytes = varBytes{ iVBytes };
            
            if bytes > 0
                
                if countElements < lastSelElement && nonIgnoredVar
                
                    compressDATA = fread( fid, bytes, readDataType );
                    
                    compressDATA = feval( readDataType, compressDATA );
                    
                    compressDATA = decrypt( compressDATA, AES );
                    
                    D =  castTo( varType, varTypeBytes, decodeData( info.compress, compressDATA ), 1 );
                                            
                    nElementsInD = length( D ); % / varCols;
                    
                    selElement1 = nElementsInD + 1;
                    selElement2 = nElementsInD;
                    
                    if ceil( countElements + nElementsInD ) >= firstElement
                        
                        selElement1 = firstElement - countElements;                        
                        
                        if selElement1 <= 0
                            
                            selElement1 = 1;
                        end
                        
                    end
                    
                    if ceil( countElements + nElementsInD ) >= lastSelElement
                        
                        selElement2 = lastSelElement - countElements;
                        if selElement2 > nElementsInD
                            
                            selElement2 = nElementsInD;
                            
                        end                        
                        
                    end
                                        
                    if selElement1 <= selElement2
                        
                        %selRow1 = ( selRow1 - 1 ) * varCols + 1;                        
                        %selRow2 = selRow2 * varCols;
                        
                        %selRow1 = fix( selRow1 );
                        %selRow2 = ceil( selRow2 );
                        %DATA( indDATA : ( indDATA + length( D ) -1 ) ) = D(:);
                        %indDATA = indDATA + length( D );
                        %[ (selRow2 - selRow1 + 1 ) length( D ) ]
                        DATA( indDATA : ( indDATA + ( selElement2 - selElement1 ) ) ) = D( selElement1 : selElement2 );
                        indDATA = indDATA + (selElement2 - selElement1 + 1);
                    
                    end
                    
                    countElements = countElements + nElementsInD;
                
                else
                    
                    fseek( fid, bytes, 0 );
                    
                end
                
                clear D compressDATA;
                
            end
        end
        
        if nonIgnoredVar
        
            DATA = DATA( 1 : ( indDATA - 1 ) ); 
            % DO NOT USE: DATA( indDATA : end ) = [] 
            % MEMORY PROBLEMS

            if ~isempty( DATA )

                if varCols > 1

                    varRows = length( DATA ) / varCols;            

                    DATA = reshape( DATA, varCols, varRows )';
                    
                    infoClis.varDims( iVars, 1 ) = varRows;

                end

                if ~strcmpi( varType, 'char' )

                    data.( strrep( varName, '-', '_' ) ) = DATA;

                else

                    data.( strrep( varName, '-', '_' ) ) = strcat( DATA )';

                end

            end
            
        end

    end
        
    fclose( fid );

 end 
 
function d = decodeData( idTech, data, fid )

    if strcmpi( idTech, 'gzip' )

        d = gzipdecode( data );

    elseif strcmpi( idTech, 'bzip2' )
        
        d = bzip2decode( data );
        
    else

        errorManager( 2, fid );

    end

end

function matlabDataType = convertType2MatlabType( type, bytes )

    matlabDataType = type;

    if strcmpi( type, 'float' )

        if bytes == 4

            matlabDataType = 'single';

        else

            matlabDataType = 'double';

        end

    elseif strcmpi( type, 'int' )

        if bytes == 1
            
            matlabDataType = 'int8';
            
        elseif bytes == 2
                
           matlabDataType = 'int16';
            
        elseif bytes == 4

            matlabDataType = 'int32';

        else

            matlabDataType = 'int64';

        end

    end

end

function castValue = castTo( type, typeBytes, values, swap )

    if strcmpi( type, 'char' )

        switch typeBytes

            case 2
                values = typecast( values, 'uint16' );

            case 4

                values = typecast( values, 'uint32' );

            case 8

                values = typecast( values, 'uint64' );
        end

        if swap

            values = swapbytes( values );
        end
        
        castValue = char( values );

    else

        castValue = typecast( values, type );

        if swap

            castValue = swapbytes( castValue );

        end

    end

end

function array = allocationArray( size, matlabDataType )
    
    array = ones( size, matlabDataType );
    
end

function pos = findStrInCell( stringCell, strs, type, insentiviceCase, exact )

    %%pos = findStrInCell( stringCell, strs, type, insentiviceCase, exact )
    %%
    %%INPUT
    %%=======
    %%stringCell    -> cell of strings
    %%strs          -> cell of PATTERNs.
    %%type          -> searching type: 0 -> first; 1 -> all
    %%insentivice   -> insensitive characters: 
    %%                      1: insensitive
    %%                      otherwise: sensitive (default).
    %%
    %%exact         -> exact match for the string: 
    %%                      1: finding exact match.
    %%                      0: finding it as a substring (default).
    %%
    %%OUTPUT
    %%======
    %%pos           -> position in cell

    if ischar( strs )
       
        strs = { strs };
        
    end
    
    pos = [];
        
    if nargin < 3 
        
        type = 0;
        
    end
    
    if nargin < 4
        
        insentiviceCase = 1;
        
    end
    
    if nargin < 5
        
        exact = 0;
        
    end
    
    if exact == 0
        
        checkString = @( c, patter)( regexpi( c, patter ) );

        if insentiviceCase == 0

            checkString = @( c, patter)( regexp( c, patter ) );

        end
        
    else
       
        checkString = @( c, patter)( ones( strcmp( c, patter ), 1 ) );
        
        if insentiviceCase == 1
            
            checkString = @( c, patter)( ones( strcmpi( c, patter ), 1 ) );
            
        end
        
    end
        
    auxStrCell = stringCell( : );
    aux =[];
    for iStrs = 1 : numel( strs )
        
        str = strs{ iStrs };
        
        for i = 1 : numel( auxStrCell )
        
            %if ~isempty( strfind( auxStrCell{ i }, str ) )
            if ~isempty( checkString(  auxStrCell{ i }, str ) )

                aux( end + 1 ) = i;

                if type == 0
                    break;        
                end
            end
        end
    end
    
    if ~isempty( aux )
       
        pos = cell( 1, ndims( stringCell ) );
        
        [ pos{:} ] = ind2sub( size( stringCell ), aux );
        
        if isvector( stringCell )
            
            if isrow( stringCell )
            
                pos = pos{ 2 };
                
            else
                
                pos = pos{ 1 };
                
            end
            
        else
            
            d = ndims( stringCell );
            pos = cell2mat( pos );            
            r = numel( pos ) / d;
            
            pos = reshape( pos, r, d );
        end
            
    end     
    
    pos = unique( pos );
    
end

function decryptArray = decrypt( encrypArray, AES )

    decryptArray = encrypArray;
    
    if ~isempty( AES )
        
        decryptArray = AES.decrypt( encrypArray );
        
    end

end