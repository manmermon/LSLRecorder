package testing.LSL;

import java.util.Timer;
import java.util.TimerTask;

import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.ProcState;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/*
 * Copyright (c) 2006 Hyperic, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
Example to show the process state for a given pid.
Compile the example:
% javac -classpath sigar-bin/lib/sigar.jar ProcessState.java
State of the java process running the example:
% java -classpath sigar-bin/lib/sigar.jar:. ProcessState
java: Running
State of the bash shell when invoking the example is running:
% java -classpath sigar-bin/lib/sigar.jar:. ProcessState $$
bash: Sleeping
State of emacs editor used to write the example:
% java -classpath sigar-bin/lib/sigar.jar:. ProcessState 2673
emacs: Suspended
See also: examples/Ps.java, examples/Top.java
*/

public class ProcessState {

    private static String getStateString(char state) {
        switch (state) {
          case ProcState.SLEEP:
            return "Sleeping";
          case ProcState.RUN:
            return "Running";
          case ProcState.STOP:
            return "Suspended";
          case ProcState.ZOMBIE:
            return "Zombie";
          case ProcState.IDLE:
            return "Idle";
          default:
            return String.valueOf(state);
        }
    }

    public static void main(String[] args) throws Exception {

    	SigarLoadMonitor sg = new SigarLoadMonitor();
    	for( int i = 0; i < 10000; i++ )
    	{
    		System.out.println("ProcessState.main() " + sg.getLoad() );
    		
    		Thread.sleep( 500L );
    		
    	}
    	
    }
    
    public static class SigarLoadMonitor {

        private static final int TOTAL_TIME_UPDATE_LIMIT = 2000;

        private final Sigar sigar;
        private final int cpuCount;
        private final long pid;
        private ProcCpu prevPc;
        private double load;

        private TimerTask updateLoadTask = new TimerTask() {
            @Override public void run() {
                try {
                    ProcCpu curPc = sigar.getProcCpu(pid);
                    long totalDelta = curPc.getTotal() - prevPc.getTotal();
                    long timeDelta = curPc.getLastTime() - prevPc.getLastTime();
                    if (totalDelta == 0) {
                        if (timeDelta > TOTAL_TIME_UPDATE_LIMIT) load = 0;
                        if (load == 0) prevPc = curPc;
                    } else {
                        load = 100. * totalDelta / timeDelta / cpuCount;
                        prevPc = curPc;
                    }
                } catch (SigarException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };

        public SigarLoadMonitor() throws SigarException {
            sigar = new Sigar();
            cpuCount = sigar.getCpuList().length;
            pid = 11876;
            prevPc = sigar.getProcCpu( pid );
            load = 0;
            new Timer(true).schedule(updateLoadTask, 0, 1000);
        }

        public double getLoad() {
            return load;
        }
    }
}