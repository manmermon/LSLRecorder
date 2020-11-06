import ClisData
from matplotlib import pyplot as plt
import numpy as np
import h5py

sufix = 'C'
name = 'data_Simulation' + sufix
nameProc = 'data_processedData_Simulation' + sufix

f = 'D:/NextCloud/WorkSpace/GitHub/LSLRecorder/' + name + '.clis'
f2 = 'D:/NextCloud/WorkSpace/GitHub/LSLRecorder/' + nameProc + '.clis'
clis = ClisData.ClisData()

d = clis.importData( f )
d2 = clis.importData( f2 )

"""
f = h5py.File( 'D:/NextCloud/WorkSpace/GitHub/LSLRecorder/data_SimulationA.h5' )
"""

s = d[ name ]
dimS = s.shape
for i in range( 0, dimS[ -1 ]-1 ):
    c = s[ :, i ]
    c = c[ ~np.isnan( c ) ]
    dft = np.abs( np.fft.fft( c ) );
    dft = 2 * dft / len( dft )
    N = int( np.floor( len( dft ) / 2 ) )
    dft = dft[0: N]
    F = np.array( [ 128 * n / N for n in range( 0, N ) ] )
    plt.plot(  F, abs( dft ) )
    plt.title( ['raw data Simulation ' + sufix + ' - Channel ', i ]  )
    #plt.axis([0, 128, 0, 2500])
    plt.show()

print( s.shape )
s = d2[ name ]
print( s.shape )
dimS = s.shape
for i in range( 0, dimS[ -1 ]-1 ):
    c = s[ :, i ]
    c = c[ ~np.isnan( c ) ]
    dft = np.abs( np.fft.fft( c ) );
    dft = 2 * dft / len(dft)
    N = int( np.floor( len( dft ) / 2 ) )
    dft = dft[0: N]
    F = np.array( [ 128 * n / N for n in range( 0, N ) ] )
    plt.plot(  F, abs( dft ) )
    plt.title( ['processed Data Simulation ' + sufix + ' - channel ', i])
    #plt.axis( [0, 128, 0, 2500])
    plt.show()