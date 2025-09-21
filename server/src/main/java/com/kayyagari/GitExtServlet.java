package com.kayyagari;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.ControllerFactory;
import org.eclipse.jgit.lib.PersonIdent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class GitExtServlet extends MirthServlet implements GitExtServletInterface {

    private static Logger log = LoggerFactory.getLogger(GitExtServlet.class);

    private GitChannelRepository repo;

    private VersionControllerUtil vcUtil;

    public GitExtServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, PLUGIN_NAME);
        repo = GitChannelRepository.getInstance();
        vcUtil = new VersionControllerUtil(ControllerFactory.getFactory().createUserController());
    }

    @Override
    public List<RevisionInfo> getHistory(String fileName) throws ClientException {
        try {
            return repo.getHistory(fileName);
        }
        catch(Exception e) {
            log.warn("failed to get the history of file {}", fileName, e);
            throw new ClientException(e);
        }
    }

    @Override
    public String getContent(String fileName, String revision) throws ClientException {
        try {
            return repo.getContent(fileName, revision);
        }
        catch(Exception e) {
            log.warn("failed to get the content of file {} at revision {}", fileName, revision, e);
            throw new ClientException(e);
        }
    }

    @Override
    public void revert(String fileName, String revision) throws ClientException {
        try {
            PersonIdent committer = vcUtil.getCommitter(context);
            repo.revertFile(fileName, revision, committer);
        }
        catch (Exception e) {
            log.warn("failed to revert file {} to revision {}", fileName, revision);
            throw new ClientException(e);
        }
    }
}
