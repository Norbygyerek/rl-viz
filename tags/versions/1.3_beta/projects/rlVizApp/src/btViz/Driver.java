/* RLViz Application, a visualizer and dynamic loader for C++ and Java RL-Glue agents/environments
* Copyright (C) 2007, Brian Tanner brian@tannerpages.com (http://brian.tannerpages.com/)
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. */
package btViz;

import java.io.IOException;
import java.util.Vector;

import rlVizLib.messaging.agent.AgentValueForObsRequest;
import rlVizLib.messaging.agent.AgentValueForObsResponse;
import rlVizLib.messaging.environment.EnvObsForStateRequest;
import rlVizLib.messaging.environment.EnvObsForStateResponse;
import rlVizLib.messaging.environment.EnvRangeRequest;
import rlVizLib.messaging.environment.EnvRangeResponse;
import rlglue.RLGlue;
import rlglue.types.Observation;


public class Driver
{
    protected static final int kNumEpisodes = 500000;
    protected static int rlNumSteps[];
    protected static double rlReturn[];

    protected static void run(int numEpisodes) throws IOException
    {
	/*run for num_episode number of episodes and store the number of steps and return from each episode*/        	
    	for(int x = 0; x < numEpisodes; ++x) {
	    RLGlue.RL_episode(0);
	    System.out.println(RLGlue.RL_num_steps());
	    
	    rlNumSteps[x] = RLGlue.RL_num_steps();
	    rlReturn[x] = RLGlue.RL_return();
	}
    }
    
    public static void main(String [] args) throws IOException {
	double avgSteps = 0.0;
	double avgReturn = 0.0;
	
	rlNumSteps = new int[Driver.kNumEpisodes];
	rlReturn = new double[Driver.kNumEpisodes];

	/*basic main loop*/
	
	
	Vector<Observation> theQueryStates=new Vector<Observation>();
	Observation tmpObs=new Observation(0,2);
	tmpObs.doubleArray[0]=0;
	tmpObs.doubleArray[1]=0;
	theQueryStates.add(tmpObs);
	tmpObs=new Observation(0,2);
	tmpObs.doubleArray[0]=0;
	tmpObs.doubleArray[1]=.5;
	theQueryStates.add(tmpObs);
	EnvObsForStateResponse theObsForStateResponse=EnvObsForStateRequest.Execute(theQueryStates);

	System.out.println("Reponse to EnvObsForState Request  was: "+theObsForStateResponse);
	
	EnvRangeResponse theERResponse=EnvRangeRequest.Execute();
	System.out.println("Reponse to Range request was: "+theERResponse);

	System.out.println("Continuing with the experiment");
	
	System.out.println("Going to sleep");
	
	
	try {
		Thread.sleep(20000000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	System.exit(1);
	
	RLGlue.RL_init();
	
	


	run(kNumEpisodes);
	
	System.out.println("Now I want to know the value of those two states...");

	Vector<Observation> theQueryObservations=theObsForStateResponse.getTheObservations();
	
	AgentValueForObsResponse theValueResponse = AgentValueForObsRequest.Execute(theQueryObservations);
	
	System.out.println("The learned values are: "+theValueResponse);

	
	RLGlue.RL_cleanup();
	
	/*add up all the steps and all the returns*/
	for (int i = 0; i < Driver.kNumEpisodes; ++i) {
	    avgSteps += rlNumSteps[i];
	    avgReturn += rlReturn[i];
	}
	
	/*average steps and returns*/
	avgSteps /= Driver.kNumEpisodes;
	avgReturn /= Driver.kNumEpisodes;
	
	/*print out results*/
	System.out.println("\n-----------------------------------------------\n");
	System.out.println("Number of episodes: " + Driver.kNumEpisodes);
	System.out.println("Average number of steps per episode: " +  avgSteps);
	System.out.println("Average return per episode: " + avgReturn);
	System.out.println("-----------------------------------------------\n");
    }   
}
