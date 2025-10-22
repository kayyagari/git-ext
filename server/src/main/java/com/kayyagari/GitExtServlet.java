package com.kayyagari;

import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelMetadata;
import com.mirth.connect.server.api.CheckAuthorizedChannelId;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.ChannelController;
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

    private ChannelController channelController;

    public GitExtServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, PLUGIN_NAME);
        repo = GitChannelRepository.getInstance();
        vcUtil = new VersionControllerUtil(ControllerFactory.getFactory().createUserController());
        channelController = ChannelController.getInstance();
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
    @CheckAuthorizedChannelId
    public boolean revertChannel(String channelId, String revision) throws ClientException {
        try {
            Channel channel = repo.getChannelAtRevision(channelId, revision);
            ChannelMetadata metadata = channel.getExportData().getMetadata();
            if(metadata == null) {
                metadata = new ChannelMetadata();
                channel.getExportData().setMetadata(metadata);
            }
            // without this the userId will be null and will result in a warning
            // on the client when user tries to save the same channel after reverting
            metadata.setUserId(context.getUserId());

            String desc = channel.getDescription();
            desc = desc + "\n(" + "reverted to revision " + revision + ")";
            channel.setDescription(desc);
            boolean result = channelController.updateChannel(channel, context, true, Calendar.getInstance());
            log.debug("reverted Channel {} to revision {}", channelId, revision);
            return result;
        }
        catch (Exception e) {
            log.warn("failed to revert Channel {} to revision {}", channelId, revision);
            throw new ClientException(e);
        }
    }
}
