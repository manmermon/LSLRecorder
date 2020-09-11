import ClisData
from matplotlib import pyplot as plt

f = 'D:/NextCloud/WorkSpace/GitHub/LSLRecorder/data_SimulationA.clis'
clis = ClisData.ClisData()

d = clis.importData( f )

s = d[ 'data_SimulationA']

plt.plot( s[ :, 0 ] )
plt.show()