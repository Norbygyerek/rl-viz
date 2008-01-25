/* RL-VizLib, a library for C++ and Java for adding advanced visualization and dynamic capabilities to RL-Glue.
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
package rlVizLib.messaging.agent;

import java.util.StringTokenizer;
import java.util.Vector;

import rlVizLib.glueProxy.RLGlueProxy;
import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.utilities.UtilityShop;
import rlVizLib.visualization.QueryableAgent;
import rlglue.RLGlue;
import rlglue.agent.Agent;
import rlglue.types.Observation;
/**
 * This mostly by the {@URL ValueFunctionVizComponent}, this message sends a vector
 * of observations to the agent and receives in return a vector of values.
 * @author Brian Taner
 */
public class AgentValueForObsRequest extends AgentMessages {

    Vector<Observation> theRequestObservations = new Vector<Observation>();

    public AgentValueForObsRequest(GenericMessage theMessageObject) {
        super(theMessageObject);

        String thePayLoad = super.getPayLoad();

        StringTokenizer obsTokenizer = new StringTokenizer(thePayLoad, ":");

        String numValuesToken = obsTokenizer.nextToken();
        int numValues = Integer.parseInt(numValuesToken);
        assert (numValues >= 0);
        for (int i = 0; i < numValues; i++) {
            String thisObsString = obsTokenizer.nextToken();
            theRequestObservations.add(UtilityShop.buildObservationFromString(thisObsString));
        }

    }
    static boolean printedReturnError = false;

    public static AgentValueForObsResponse Execute(Vector<Observation> theRequestObservations) {
        StringBuffer thePayLoadBuffer = new StringBuffer();

        //Tell them how many
        thePayLoadBuffer.append(theRequestObservations.size());

        for (int i = 0; i < theRequestObservations.size(); i++) {
            thePayLoadBuffer.append(":");
            UtilityShop.serializeObservation(thePayLoadBuffer, theRequestObservations.get(i));
        }

        String theRequest = AbstractMessage.makeMessage(
                MessageUser.kAgent.id(),
                MessageUser.kBenchmark.id(),
                AgentMessageType.kAgentQueryValuesForObs.id(),
                MessageValueType.kStringList.id(),
                thePayLoadBuffer.toString());

        String responseMessage = RLGlueProxy.RL_agent_message(theRequest);

        AgentValueForObsResponse theResponse;
        try {
            theResponse = new AgentValueForObsResponse(responseMessage);
        } catch (NotAnRLVizMessageException e) {
            if (!printedReturnError) {
                System.err.println("AgentValueForObsResponse received a non RLViz response.  I won't print this again.");
                printedReturnError = true;
            }
            theResponse = null;
        }

        return theResponse;

    }

    /**
     * @return the theRequestStates
     */
    public Vector<Observation> getTheRequestObservations() {
        return theRequestObservations;
    }

    @Override
    public boolean canHandleAutomatically(Object theReceiver) {
        return (theReceiver instanceof QueryableAgent);
    }

    @Override
    public String handleAutomatically(Agent theAgent) {
        QueryableAgent castedAgent = (QueryableAgent) theAgent;

        Vector<Double> theValues = new Vector<Double>();

        for (int i = 0; i < theRequestObservations.size(); i++) {
            theValues.add(castedAgent.getValueForState(theRequestObservations.get(i)));
        }

        AgentValueForObsResponse theResponse = new AgentValueForObsResponse(theValues);
        String stringResponse = theResponse.makeStringResponse();

        return stringResponse;
    }
}
