import ClisData
from matplotlib import pyplot as plt
import h5py

""""
f = 'D:/NextCloud/WorkSpace/GitHub/LSLRecorder/data_SimulationA.clis'
clis = ClisData.ClisData()

d = clis.importData( f )

s = d[ 'data_SimulationA']
"""

f = h5py.File( 'D:/NextCloud/WorkSpace/GitHub/LSLRecorder/data_SimulationA.h5' )

s = f[ 'data_SimulationA']
plt.plot( s[ :, 0 ] )
plt.show()
