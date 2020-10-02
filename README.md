# Git Extension
This is a MirthConnect extension for version controlling Channels and CodeTemplates.
Git is used as the VCS on the serverside using JGit.

# Building
This project depends on Objmeld project for creating visual diffs and it should be built first. Its source is not included here due
to licensing issues (The project JMeld from which Objmeld was derived was released under LGPL).

Another caveat is that various libraries of MirthConnect distribution must be present in your Maven repo (local or remote).

Once the above are taken care of then run `mvn clean install` to build the extenstion. The final artifact will be present under
`package/target` folder with the name `git-ext-<version>.zip`.  
