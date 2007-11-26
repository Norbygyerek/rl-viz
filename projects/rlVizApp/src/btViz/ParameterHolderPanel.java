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

import java.awt.Component;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import rlVizLib.general.ParameterHolder;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import java.awt.Font;

public class ParameterHolderPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Vector<Component> allComponents=new Vector<Component>();
	Map<String, Component> nameToValueMap= new TreeMap<String, Component>();

        Font pFont = new Font("Courier",Font.PLAIN,10);

	public ParameterHolderPanel(){
		super(); 
	}


	public void setEnabled(boolean shouldEnable){
		for (Component thisComponent : allComponents)thisComponent.setEnabled(shouldEnable);
	}

	ParameterHolder currentParamHolder=null;

	public void switchParameters(ParameterHolder p) {
		this.currentParamHolder=p;
		allComponents.removeAllElements();
	}

	public ParameterHolder updateParamHolderFromPanel(){
		for(int i=0;i<currentParamHolder.getParamCount();i++){
			int thisParamType=currentParamHolder.getParamType(i);
			String thisParamName=currentParamHolder.getParamName(i);
                        if(thisParamName.toLowerCase().startsWith("###"))continue;
			Component theRelatedComponent=nameToValueMap.get(thisParamName);

			switch (thisParamType) {
			case ParameterHolder.boolParam:
				JCheckBox boolField=(JCheckBox)theRelatedComponent;
				currentParamHolder.setBooleanParam(thisParamName, boolField.isSelected());
				break;

			case ParameterHolder.intParam:
				JTextField intField=(JTextField)theRelatedComponent;
				currentParamHolder.setIntegerParam(thisParamName, Integer.parseInt(intField.getText()));
				break;

			case ParameterHolder.doubleParam:
				JTextField doubleField=(JTextField)theRelatedComponent;
				currentParamHolder.setDoubleParam(thisParamName, Double.parseDouble(doubleField.getText()));
				break;
			case ParameterHolder.stringParam:
				JTextField stringField=(JTextField)theRelatedComponent;
				currentParamHolder.setStringParam(thisParamName,stringField.getText());
				break;
			}

		}
		return currentParamHolder;

	}




	private Component addIntParameter(DefaultFormBuilder builder, String thisParamName, int theParam) {
		JLabel thisLabel=new JLabel(thisParamName+":", JLabel.TRAILING);
		JTextField thisField=new JTextField(5);
		thisLabel.setLabelFor(thisField);
		thisField.setText(""+theParam);

		builder.append(thisLabel); 
		builder.append(thisField); 
		builder.nextLine(); 

		allComponents.add(thisLabel);
		allComponents.add(thisField);


		return thisField;
	}

	private Component addDoubleParameter(DefaultFormBuilder builder, String thisParamName, double theParam) {
		JLabel thisLabel=new JLabel(thisParamName+":", JLabel.TRAILING);
		JTextField thisField=new JTextField(5);
		thisLabel.setLabelFor(thisField);
		thisField.setText(""+theParam);
		builder.append(thisLabel); 
		builder.append(thisField); 
		builder.nextLine(); 


		allComponents.add(thisLabel);
		allComponents.add(thisField);

		return thisField;
	}

    private void addLabel(DefaultFormBuilder builder, String thisLabelString) {
		JLabel thisLabel=new JLabel(thisLabelString);
                builder.append(thisLabel);
                builder.nextLine();
		allComponents.add(thisLabel);
    }

	private Component addStringParameter(DefaultFormBuilder builder, String thisParamName, String theParam) {
		JLabel thisLabel=new JLabel(thisParamName+":", JLabel.TRAILING);
		JTextField thisField=new JTextField(5);
		thisLabel.setLabelFor(thisField);
		thisField.setText(""+theParam);

		allComponents.add(thisLabel);
		allComponents.add(thisField);

		builder.append(thisLabel); 
		builder.append(thisField); 
		builder.nextLine(); 

		return thisField;
	}


	private Component addBoolParameter(DefaultFormBuilder builder, String thisParamName, boolean currentValue) {
		JLabel thisLabel=new JLabel(thisParamName+":", JLabel.TRAILING);
		JCheckBox thisField=new JCheckBox();
		thisField.setSelected(currentValue);
		thisLabel.setLabelFor(thisField);

		allComponents.add(thisLabel);
		allComponents.add(thisField);

		builder.append(thisLabel); 
		builder.append(thisField); 
		builder.nextLine(); 

		return thisField;
	}


	public void addParamFieldsToBuilder(DefaultFormBuilder builder) {
      
		for(int i=0;i<currentParamHolder.getParamCount();i++){
			int thisParamType=currentParamHolder.getParamType(i);
			String thisParamName=currentParamHolder.getParamName(i);
                        if(thisParamName.toLowerCase().startsWith("###"))
                            continue;
			Component newField=null;
			switch (thisParamType) {
			case ParameterHolder.boolParam:
				newField=addBoolParameter(builder,thisParamName, currentParamHolder.getBooleanParam(thisParamName));
				break;
			case ParameterHolder.intParam:
				newField=addIntParameter(builder,thisParamName, currentParamHolder.getIntegerParam(thisParamName));
				break;
			case ParameterHolder.doubleParam:
				newField=addDoubleParameter(builder,thisParamName, currentParamHolder.getDoubleParam(thisParamName));
				break;
			case ParameterHolder.stringParam:
				newField=addStringParameter(builder,thisParamName, currentParamHolder.getStringParam(thisParamName));
				break;
			}
			nameToValueMap.put(thisParamName, newField);
		}
                //Set a nice fixed width font
                for (Component component : allComponents)component.setFont(pFont);


                if(currentParamHolder.getParamCount()==0){
                    addLabel(builder, "No configurable Parameters");
                }
	}

}
