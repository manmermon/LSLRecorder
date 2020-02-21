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
            import javax.crypto.SecretKey;
            import javax.crypto.SecretKeyFactory;
            import javax.crypto.spec.PBEKeySpec;
            
            keyEncrypt = passwordEntryDialog( 'PasswordLengthMin', 1, 'PasswordLengthMax', Inf );      
            if isempty( keyEncrypt )
                
                ME = MException( 'MyComponent:noSuchVariable', 'Password incorrect' );
                throw(ME)
            end
            
            obj.password = keyEncrypt;
            
            %skf = SecretKeyFactory.getInstance( 'PBKDF2WithHmacSHA256' );
            skf = SecretKeyFactory.getInstance( 'PBKDF2WithHmacSHA1' );
            
            stat = java.util.Arrays.copyOf( int8( zeros( 8,1 ) ), 8) ;
			spec = PBEKeySpec( String( keyEncrypt ) .toCharArray(), stat, 10000, 128 );
			tmp = skf.generateSecret( spec );
                        
            obj.secretKey = SecretKeySpec( tmp.getEncoded(), 'AES' );
            obj.cipher = Cipher.getInstance( 'AES/CBC/PKCS5Padding');
         
            buf = java.util.Arrays.copyOf( int8( zeros( 16,1 ) ), 16) ;
            ivpar = IvParameterSpec( buf );
            
            obj.IVPar = ivpar;
            
            
        end
        
        function eq = checkEncryptPassword( obj, encryptKey )

            import java.lang.String;
            
            decryptKey = decrypt( obj, encryptKey );
            
            eq = strcmp( obj.password, char( String( decryptKey, 'UTF-8' ) ) );
            
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