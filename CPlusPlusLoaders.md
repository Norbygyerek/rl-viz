# Introduction #

This is an advanced and EXPERIMENTAL topic.  And this tutorial has been written somewhat quickly to help out one particular person who was asking about how to do this.  As such, please send feeback/fixes/bugs etc to btanner@rl-community.org so this document can be improved over time.

# Purpose #
RL-Viz (with the help of AgentShell and EnvironmentShell) lets you graphically or programatically load environments and agents at runtime, and to configure their parameters at the same time.  There are plenty of examples of this (although not well explained) in the RL-Library and the RL-Competition.  Most people will be most familiar with RLVizApp, which is the graphical Java application used in the competition and the library.  You might know it as "rundemo.bash" or "guiTrainerJava" from the competition software (2008-2009).

RLVizApp can either accept connections made from vanilla RL-Glue environments and agents that connect to the rl\_glue(.exe) executable socket server (as normal), or by a proxy called an agent or environment _shell_.  These shells themselves are valid RL-Glue environments and agents, with extra functionality.  The shells listen for certain commands through env\_message and agent\_message, and if they receive certain commands they will look to the hard disk for environments and agents that have been stored in JAR files (Java) or dylib/so files (C/C++).

The JAR loading works out of the box when you have AgentShell.jar, EnvironmentShell.jar, and RLVizLib.jar.  The dylib/so loading needs some extra configuration steps because it relies on the Java Native Interface (JNI) which requires something to be compiled on your machine (JAR files are portable, JNI is not).

# Compiling the External CPP Environment and/or Agent Loader #

```bsh

#I'll give instructions for the environment, the agent is the same

#Check out RL-Viz from Google Code (sorry now downloads available yet)
svn checkout http://rl-viz.googlecode.com/svn/trunk/ rl-viz-read-only

#Change to the appropriate directory
cd rl-viz-read-only/projects/externalLoaders/CPPEnvLoader

#Build the loader, place it in rl-viz-read-only/system/dist with the other built components
make
```

This might not work for you.  It requires JDK to be installed, and certain paths be set correctly.  The makefile needs to find javah, and also the JNI headers.  There are some instructions in the Makefile that will explain what you might need to do.  On Ubuntu 9.04, I just had to do this:
```bash

#Your mileage may vary
JAVA_HOME=/usr/lib/jvm/java-6-openjdk/include/ make
```

Some people have reported that the openjdk works and that Sun's doesn't on Linux.  I haven't tried.  Play around, figure it out, and make sure you have libRLVizCPPEnvLoader.dylib in your rl-viz-read-only/system/dist directory.

Probably we should have a better name, and it shouldn't be .dylib on all platforms, but I'll get to that later.

# Compiling an Environment #
We have included an example agent and environment in the RL-Viz distribution.  To build it:
```bsh

cd rl-viz-read-only/examples/CPPLoadingExample/mines-sarsa-c-sharedlibrary

#Depending on your platform
make OSX
#or
make Linux
```

There is a more detailed explanation of this project and it's components in the README.txt file in the mines-sarsa-c-sharedlibrary directory.

This should build you (depending on platform) SampleMinesEnvironment.{dylib,so} and SampleSarsaAgent{.dylib,so}.

# Running RL-Viz to load these dynamic components #

```bsh

#Go into the dist folder
cd rl-viz-read-only/system/dist

#Execute this gross command
java -enableassertions -Xmx128M -jar RLVizApp.jar agent-environment-jar-path=$(pwd)/../../examples/CPPLoadingExample/mines-sarsa-c-sharedlibrary/ list-agents=true list-environments=true env-viz=true agent-viz=true local-glue=true cpp-agent-loading=true cpp-env-loading=true
```

You should now have an RL-Viz window popped up with the SampleMinesEnvironment in the drop-down list of environments.   If you built the CPPAgentLoader also, you'll have SampleSarsaAgent in the agents list.

You might want another agent and visualization too.  If so, follow these instructions:

Download the latest Epsilon Greedy Tile-Coding Sarsa Lambda agent from rl-library:
```bsh

cd ../../examples
mkdir tutorial
cd tutorial
wget http://rl-library.googlecode.com/files/EpsilonGreedyTileCodingSarsaLambda-Java-R18.tar.gz

#This agent distribution keeps the JAR in a folder called "products"
tar -zxvf EpsilonGreedyTileCodingSarsaLambda-Java-R18.tar.gz

#It would also be good to have a visualizer so you can see what's happening
wget http://rl-library.googlecode.com/files/GenericVisualizer-Java-R1255.tar.gz

tar -zxvf GenericVisualizer-Java-R1255.tar.gz

#Copy your environment shared library to products (use dylib instead of so on Mac)
cp ../CPPEnvLoading/mines-sarsa-c-sharedlibrary/SampleMinesEnvironment.so products/

#Use this different line to run RL-Viz from here
java -enableassertions -Xmx128M -jar ../../system/dist/RLVizApp.jar agent-environment-jar-path=./products list-agents=true list-environments=true env-viz=true agent-viz=true local-glue=true cpp-env-loading=true cpp-agent-loading=true
```

Now you can run the gross line again to start RL-Viz and you should be able to choose/configure the agent from the list and run your graphical experiment.

# Next Steps #
This tutorial is brief and doesn't touch on the finer parts of writing programmatic code to load these environments and use them in a more traditional experiment.

It also doesn't explain how to add dynamic configuration capabilities (options for the drop-down boxes) like the agent has to your C/C++ environment.  The sample agent and environment DO have this functionality, so feel free to check it out.

Please ask me if you care about these things and I'll write some more here :)