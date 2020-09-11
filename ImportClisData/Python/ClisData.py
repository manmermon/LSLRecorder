import gzip
import struct
from builtins import bytearray

import numpy as np


class ClisData():
    stringEncoding = "iso-8859-15"
    file = ""


    def __CheckMetadataFieldLen(self, field):

        if len( field ) != 2:
            self.__ErrorManager(0)

    def __ErrorManager(self, errorID):

        self.file.close()

        if errorID == 0:
            raise Exception("CLIS Metadata missing.")
        elif errorID == 1:
            raise Exception("Compressed-data-block size incorrect.")

    def importData(self, filePath ):
        if not filePath:
            raise Exception("File path empty or null")
        else:
            self.file = open(filePath, 'rb')

        file = None

        try:
            file = self.file
            metadata = str( file.readline(), self.stringEncoding ).rstrip()
            fields = metadata.split(";")
            if not fields[ -1 ]:
                fields.pop( -1 )

            metadataVersion = fields[0]
            metadataRest = fields[1:]

            vers = metadataVersion.split("=")

            self.__CheckMetadataFieldLen(vers)

            if vers[0] != "ver":
                self.__ErrorManager(0)
            else:
                v = float(vers[1])

                if v == 2.1:

                    return self.__importData21(metadataRest)

                elif v == 2.0:

                    return self.__importData2(metadataRest)

        except Exception as ex:
            print( ex )
        finally:

            if file:
                file.close()

    def __importData2(self, metadataRest):

        info = self.__checkMetadata2(metadataRest)
        vars = info[ "vars" ]

        data = {}
        data["header"] = info["header"]

        for var in vars:
            if len(var) < 5:
                self.__ErrorManager(0)

            v = var.split(",")
            varName = v[0]
            varType = (v[1], int(v[2]))
            if varType[0] != 'int' \
                    and varType[0] != 'float' \
                    and varType[0] != 'char':
                self.__ErrorManager(0)

            varCh = int(v[3])
            varCompressDataBlockSize = v[4:]
            for i in range(0, len(varCompressDataBlockSize)):
                varCompressDataBlockSize[i] = int(varCompressDataBlockSize[i])

            D = np.empty(0, 'b')
            for dataBlock in varCompressDataBlockSize:
                block = self.file.read( dataBlock)
                decompressed_data = self.__decompress( block, info[ "compress" ] )
                d = self.__convertByteArrayTo(decompressed_data, varType)
                D = np.append(D, d)

            rows = len(D) / varCh
            if int( rows ) - rows != 0:
                self.__ErrorManager( 1 )

            data[ varName ] = np.reshape( D, ( int( rows ), varCh))
            del D

        return data

    def __checkMetadata2(self, metadataRest):

        info = {}

        file = self.file

        compress = metadataRest[0]
        headerSize = metadataRest[1]

        dataHeader = metadataRest[-1]
        dataVars = metadataRest[2:-1]

        info[ "vars" ] = dataVars

        compress = compress.split("=")
        self.__CheckMetadataFieldLen(compress)

        if compress[0] != "compress":
            self.__ErrorManager(0)

        else:
            info[compress[0]] = compress[1]

        headerSize = headerSize.split("=")
        self.__CheckMetadataFieldLen(headerSize)

        if headerSize[0] != "headerByteSize":
            self.__ErrorManager(0)

        else:
            info[headerSize[0]] = int(headerSize[1])

        dataHeader = dataHeader.split(",")
        self.__CheckMetadataFieldLen(dataHeader)

        if dataHeader[0] != "header":
            self.__ErrorManager(0)
        else:
            if dataHeader[1].lower() == "true":
                info[dataHeader[0]] = dataHeader[1].lower()

            elif extension.lower() != "false":
                self.__ErrorManager(0)

        if info[ dataHeader[ 0 ] ] == "true":
            info[ dataHeader[ 0 ] ] = str( file.readline(), self.stringEncoding ).rstrip()

        file.seek( info["headerByteSize"] )

        return info

    def __importData21(self, metadataRest):

        info = self.__checkMetadata21(metadataRest)
        vars = info[ "vars" ]

        data = {}
        data["header"] = info["header"]

        for var in vars:
            if len(var) < 5:
                self.__ErrorManager(0)

            v = var.split(",")
            varName = v[0]
            varType = (v[1], int(v[2]))
            if varType[0] != 'int' \
                    and varType[0] != 'float' \
                    and varType[0] != 'char':
                self.__ErrorManager(0)

            varCh = int(v[3])
            varCompressDataBlockSize = v[4:]
            for i in range(0, len(varCompressDataBlockSize)):
                varCompressDataBlockSize[i] = int(varCompressDataBlockSize[i])

            D = np.empty(0, 'b')
            for dataBlock in varCompressDataBlockSize:
                block = self.file.read( dataBlock)
                decompressed_data = self.__decompress( block, info[ "compress" ] )
                d = self.__convertByteArrayTo(decompressed_data, varType)
                D = np.append(D, d)

            rows = len(D) / varCh
            if int( rows ) - rows != 0:
                self.__ErrorManager( 1 )

            data[ varName ] = np.reshape( D, ( int( rows ), varCh))
            del D

        return data

    def __checkMetadata21(self, metadataRest):

        info = {}

        file = self.file

        compress = metadataRest[0]
        headerSize = metadataRest[1]

        extension = metadataRest[-1]
        dataHeader = metadataRest[- 2]
        dataVars = metadataRest[2:-2]

        info[ "vars" ] = dataVars

        compress = compress.split("=")
        self.__CheckMetadataFieldLen(compress)

        if compress[0] != "compress":
            self.__ErrorManager(0)

        else:
            info[compress[0]] = compress[1]

        headerSize = headerSize.split("=")
        self.__CheckMetadataFieldLen(headerSize)

        if headerSize[0] != "headerByteSize":
            self.__ErrorManager(0)

        else:
            info[headerSize[0]] = int(headerSize[1])

        if extension.lower() == "true":
            extension = str( file.readline(), self.stringEncoding ).rstrip().split( ";" )
            if not extension[ -1 ]:
                extension.pop( -1 )

            if not extension:
                self.__ErrorManager(0)
            else:
                encrypt = extension[0] .split("=")
                checksum = extension[1].split("=")

                self.__CheckMetadataFieldLen(encrypt)
                self.__CheckMetadataFieldLen(checksum)

                if encrypt[0] != "encrypt":
                    self.__ErrorManager(0)

                if checksum[0] != "checksum":
                    self.__ErrorManager(0)

                encryptLen = int(encrypt[1])
                info[checksum[0]] = checksum[1]

                if encryptLen > 0:
                    secretKey = file.read(encryptLen)

        elif extension.lower() != "false":
            self.__ErrorManager(0)

        dataHeader = dataHeader.split(",")
        self.__CheckMetadataFieldLen(dataHeader)

        if dataHeader[0] != "header":
            self.__ErrorManager(0)
        else:
            info[dataHeader[0]] = int(dataHeader[1])

        if info[dataHeader[0]] > 0:
            info[ dataHeader[ 0 ] ] = file.read( info[ dataHeader[ 0 ] ] )
        else:
            info[dataHeader[0]] = ""

        if not "AES" in info:
            info[ dataHeader[ 0 ] ] = str( info[ dataHeader[ 0 ] ], self.stringEncoding )

        file.seek( info["headerByteSize"] )

        return info

    def __convertByteArrayTo(self, byteArray, dataType):

        type = ''
        data = []

        if dataType[0] == 'int':
            if dataType[1] == 1:
                type = 'b'
            elif dataType[1] == 2:
                type = 'h'
            elif dataType[1] == 4:
                type = 'i'
            elif dataType[1] == 8:
                type = 'q'
        elif dataType[0] == 'float':
            if dataType[1] == 4:
                type = 'f'
            elif dataType[1] == 8:
                type = 'd'
        elif dataType[0] == 'char':
            type = 'c'

        if not type:
            self.__ErrorManager(0)

        else:

            dataSize =  dataType[ 1 ]
            outSize = len( byteArray ) / dataSize ;
            if int( outSize ) - outSize != 0:
                self.__ErrorManager( 1 )

            data = np.empty( int( outSize ) )
            indexData = 0
            for i in range( dataSize, len( byteArray )  + dataSize, dataSize ):
                ar = byteArray[ ( i - dataSize ):i ]
                val = struct.unpack(type, ar[ ::-1 ])
                data[ indexData ] = val[ 0 ]
                indexData += 1
        return data

    def __decompress(self, array, tech ):
        if tech.lower() == "gzip":
            return gzip.decompress( array )