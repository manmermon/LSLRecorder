import ClisData
from matplotlib import pyplot as plt
import numpy as np
import h5py

""
f = 'D:/NextCloud/WorkSpace/GitHub/LSLRecorder/data_SimulationA.clis'
f2 = 'D:/NextCloud/WorkSpace/GitHub/LSLRecorder/data_SimulationA_SimulationA.clis'
clis = ClisData.ClisData()

d = clis.importData( f )
d2 = clis.importData( f2 )

s = d[ 'data_SimulationA']
""
"""
f = h5py.File( 'D:/NextCloud/WorkSpace/GitHub/LSLRecorder/data_SimulationA.h5' )
"""
s = d[ 'data_SimulationA']
dimS = s.shape
for i in range( 0, dimS[ -1 ]-1 ):
    c = s[ :, i ]
    c = c[ ~np.isnan( c ) ]
    dft = np.abs( np.fft.fft( c ) );
    N = len( dft )
    F = np.array( [ 256 * n / N for n in range( 0, N ) ] )
    plt.plot(  F, abs( dft ) )
    plt.title( 'dataSimulationA')
    plt.show()

s = d2[ 'data_SimulationA']
dimS = s.shape
for i in range( 0, dimS[ -1 ]-1 ):
    c = s[ :, i ]
    c = c[ ~np.isnan( c ) ]
    dft = np.abs( np.fft.fft( c ) );
    N = len( dft )
    F = np.array( [ 256 * n / N for n in range( 0, N ) ] )
    plt.plot(  F, abs( dft ) )
    plt.title( 'dataSimulationA')
    plt.show()