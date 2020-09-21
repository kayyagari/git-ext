package com.kayyagari;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.server.api.MirthServlet;

public class GitExtServlet extends MirthServlet implements GitExtServletInterface {

    private static Logger log = Logger.getLogger(GitExtServlet.class);

    private GitChannelRepository repo;

    public GitExtServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, PLUGIN_NAME);
        repo = GitChannelRepository.getInstance();
    }

    @Override
    public List<RevisionInfo> getHistory(String fileName) throws ClientException {
        try {
            return repo.getHistory(fileName);
        }
        catch(Exception e) {
            log.warn("failed to get the history of file " + fileName, e);
            throw new ClientException(e);
        }
    }

    @Override
    public String getContent(String fileName, String revision) throws ClientException {
        try {
            return repo.getContent(fileName, revision);
        }
        catch(Exception e) {
            log.warn("failed to get the content of file " + fileName + " at revision " + revision, e);
            throw new ClientException(e);
        }
    }

}
