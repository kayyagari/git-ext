package com.kayyagari;

import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.ChannelPlugin;
import com.mirth.connect.server.controllers.ControllerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class ChannelVersionController implements ChannelPlugin {

    private static Logger log = LoggerFactory.getLogger(ChannelVersionController.class);

    private GitChannelRepository repo;
    
    private ObjectXMLSerializer serializer;

    private VersionControllerUtil vcUtil;

    @Override
    public String getPluginPointName() {
        return GitExtServletInterface.PLUGIN_NAME;
    }

    @Override
    public void start() {
        log.info("starting git-ext channel version controller");
        vcUtil = new VersionControllerUtil(ControllerFactory.getFactory().createUserController());
        serializer = ObjectXMLSerializer.getInstance();
        String appDataDir = Donkey.getInstance().getConfiguration().getAppData();
        GitChannelRepository.init(appDataDir, serializer);
        repo = GitChannelRepository.getInstance();
    }

    @Override
    public void stop() {
        repo.close();
    }

    @Override
    public void save(Channel channel, ServerEventContext sec) {
        log.info("saving channel {}", sec.getUserId());
        repo.updateChannel(channel, vcUtil.getCommitter(sec));
    }

    @Override
    public void remove(Channel channel, ServerEventContext sec) {
        repo.removeChannel(channel, vcUtil.getCommitter(sec));
    }

    @Override
    public void deploy(Channel channel, ServerEventContext arg1) {
    }

    @Override
    public void deploy(ServerEventContext sec) {
    }

    @Override
    public void undeploy(ServerEventContext sec) {
    }

    @Override
    public void undeploy(String id, ServerEventContext sec) {
    }
}
