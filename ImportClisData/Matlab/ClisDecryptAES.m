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
            import java.security.Key;
            import java.util.Arrays;
            import java.lang.String;
            import javax.crypto.Cipher;
            import javax.crypto.spec.IvParameterSpec;
            import javax.crypto.spec.SecretKeySpec;
            
            keyEncrypt = passcode;      
            obj.password = keyEncrypt;
            
            while( length( keyEncrypt ) < 16 )
                
                keyEncrypt = sprintf( '%s_', keyEncrypt );
                
            end
            
            keyBytes = String( keyEncrypt ).getBytes( 'UTF-8' );
            
            obj.secretKey = SecretKeySpec( keyBytes ,'AES' );
            obj.cipher = Cipher.getInstance( 'AES/CBC/PKCS5Padding');
            
            buf = java.util.Arrays.copyOf( int8( zeros( 16,1 ) ), 16) ;
            ivpar = IvParameterSpec( buf );
            
            obj.IVPar = ivpar;
            
            
        end
        
        function eq = checkEncryptPassword( obj, encryptKey )

            decryptKey = decrypt( obj, encryptKey );
            
            eq = strcmp( obj.password, sprintf( '%s', decryptKey ) );
            
        end
        
        function decryptData = decrypt( obj, encryptData )
            %DECRYPT Summary of this method goes here
            %   Detailed explanation goes here
            
            import java.security.Key;
            import java.util.Arrays;
            import java.lang.String;
            import javax.crypto.Cipher;
            import javax.crypto.spec.IvParameterSpec;
            import javax.crypto.spec.SecretKeySpec;
            
            encryptData = int8( encryptData );
            
            obj.cipher.init( Cipher.DECRYPT_MODE,  obj.secretKey, obj.IVPar );
            
            dataLen = length( encryptData );
            dataBuf = java.util.Arrays.copyOf( encryptData, dataLen ) ;
            
            decryptData = obj.cipher.doFinal( dataBuf );
        end
    end
end