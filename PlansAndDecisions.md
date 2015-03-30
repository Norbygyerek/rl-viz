# Separate or Joint Jars #
I've been debating back and forth between splitting up all of the projects (agentShell, envShell, RLVizLib, rlVizApp). There are several questions... like, would you ever want just some of them?  Or, this is the wrong question, because the cost of putting them all together isn't high.

How much is it worth being able to only update rlVizLib without all of the other stuff.  Or RL-Glue for that matter?  It is very attractive to put them all together, because distribution and use becomes that much better.  But wouldn't you like to develop and update rlVizApp without releasing a new version of RLVizLib?  In theory, RLVizLib should be able to live on its own.  I think I'm going to stick with that plan.  All separate.

# Unit Testing #

We're working hard to start writing unit tests as much as possible and automating (yet separating) their build and run process.  Right now to run the unit tests you need to have junit.jar in your ant directory, hope to fix that soon.  We've got some HTML stuff setup to automatically generate nice reports also.