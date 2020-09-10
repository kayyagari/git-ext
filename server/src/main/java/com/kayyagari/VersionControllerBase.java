package com.kayyagari;

import org.eclipse.jgit.lib.PersonIdent;

import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.User;
import com.mirth.connect.server.controllers.UserController;

public abstract class VersionControllerBase {
    protected UserController userController;
    
    protected PersonIdent getCommitter(ServerEventContext sec) {
        try {
            User user = userController.getUser(sec.getUserId(), null);
            String username = user.getUsername();
            String email = user.getEmail();
            if(email == null) {
                email = username + "@" + "local";
            }

            return new PersonIdent(username, email);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
