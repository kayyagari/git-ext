package com.kayyagari;

import org.eclipse.jgit.lib.PersonIdent;

import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.User;
import com.mirth.connect.server.controllers.UserController;

/**
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class VersionControllerUtil {
    protected UserController userController;

    public VersionControllerUtil(UserController userController) {
        this.userController = userController;
    }

    public PersonIdent getCommitter(ServerEventContext sec) {
        try {
            User user = userController.getUser(sec.getUserId(), null);
            String username = user.getUsername();
            String email = user.getEmail();
            if(email == null) {
                email = username + "@" + "local";
            }

            return new PersonIdent(username, email, System.currentTimeMillis(), 0); // UTC
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
