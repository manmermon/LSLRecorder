classdef ClisDecryptAES < handle
    %UNTITLED Summary of this class goes here
    %   Detailed explanation goes here
    
    properties (Access = private)        
        password
        secretKey
        cipher
        IVPar
    end
    
    methods
        function obj = ClisDecryptAES( )
            %AES Construct an instance of this class
            %   algorithm options are https://docs.oracle.com/javase/9/docs/specs/security/standard-names.html#messagedigest-algorithms            
            
            keyEncrypt = passwordEntryDialog( );   
            printf( keyEncrypt )   
            if isempty( keyEncrypt )
                
                ME = MException( 'MyComponent:noSuchVariable', 'Password incorrect' );
                throw(ME)
            end
            
            path = [fileparts(mfilename('fullpath')) '/apache-commons/commons-io-2.11.0.jar'];
            javaaddpath( path )
            path = [fileparts(mfilename('fullpath')) '/apache-commons/commons-lang3-3.7.jar'];
            javaaddpath( path )
            
            obj.password = keyEncrypt;
            
            %skf = SecretKeyFactory.getInstance( 'PBKDF2WithHmacSHA256' );
            skf = javaMethod( 'getInstance', 'javax.crypto.SecretKeyFactory', 'PBKDF2WithHmacSHA1' );
            
            stat = javaMethod( 'copyOf', 'java.util.Arrays', int8( zeros( 8,1 ) ), 8);
            keyEncryptTx = javaObject( 'java.lang.String', keyEncrypt );
            spec = javaObject( 'javax.crypto.spec.PBEKeySpec', keyEncryptTx.toCharArray(), stat, 10000, 128 );                                
            tmp = skf.generateSecret( spec );
                        
            obj.secretKey = javaObject( 'javax.crypto.spec.SecretKeySpec', tmp.getEncoded(), 'AES' );
            obj.cipher = javaMethod( 'getInstance', 'javax.crypto.Cipher', 'AES/CBC/PKCS5Padding');
         
            buf = javaMethod( 'copyOf', 'java.util.Arrays', int8( zeros( 16,1 ) ), 16) ;
            ivpar = javaObject( 'javax.crypto.spec.IvParameterSpec', buf );
            
            obj.IVPar = ivpar;            
            
        end
        
        function eq = checkEncryptPassword( obj, encryptKey )
            
            decryptKey = decrypt( obj, encryptKey );
            
            decryptKeyTx = javaObject( 'java.lang.String', decryptKey, 'UTF-8' )
            eq = strcmp( obj.password, char( decryptKeyTx ) );
            
        end
        
        function decryptData = decrypt( obj, encryptData )
            %DECRYPT Summary of this method goes here
            %   Detailed explanation goes here          

            encryptData = int8( encryptData );
            
            obj.cipher.init( Cipher.DECRYPT_MODE,  obj.secretKey, obj.IVPar );
            
            dataLen = length( encryptData );
            dataBuf = javaMethod( 'copyOf', 'java.util.Arrays', encryptData, dataLen );
            
            decryptData = obj.cipher.doFinal( dataBuf );
        end
    end
end