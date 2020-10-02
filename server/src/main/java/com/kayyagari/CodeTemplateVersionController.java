package com.kayyagari;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrary;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.CodeTemplateServerPlugin;
import com.mirth.connect.server.controllers.ControllerFactory;

/**
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class CodeTemplateVersionController extends VersionControllerBase implements CodeTemplateServerPlugin {

    private static Logger log = Logger.getLogger(CodeTemplateVersionController.class);

    private GitChannelRepository repo;
    
    private ObjectXMLSerializer serializer;

    @Override
    public String getPluginPointName() {
        return GitExtServletInterface.PLUGIN_NAME;
    }

    @Override
    public void start() {
        log.info("starting git-ext CodeTemplate version controller");
        userController = ControllerFactory.getFactory().createUserController();
        serializer = ObjectXMLSerializer.getInstance();
        String appDataDir = Donkey.getInstance().getConfiguration().getAppData();
        GitChannelRepository.init(appDataDir, serializer);
        repo = GitChannelRepository.getInstance();
    }

    @Override
    public void stop() {
        // do not close the repo here it gets called from ChannelVersionController
    }

    @Override
    public void remove(CodeTemplate ct, ServerEventContext sec) {
        repo.removeCodeTemplate(ct, getCommitter(sec));
    }

    @Override
    public void remove(CodeTemplateLibrary ctLib, ServerEventContext sec) {
    }

    @Override
    public void save(CodeTemplate ct, ServerEventContext sec) {
        repo.updateCodeTemplate(ct, getCommitter(sec));
    }

    @Override
    public void save(CodeTemplateLibrary ctLib, ServerEventContext sec) {
    }
}
