#/bin/bash
java -Xmx1024M  -DRLVIZ_LIB_PATH=../../rlcomplibrary/libraries -classpath ../system/dist/RLVizApp.jar:../system/dist/RLVizLib.jar:../system/dist/EnvironmentShell.jar:../system/dist/AgentShell.jar -DCPPEnv=true btViz.LocalGraphicalDriver