package com.kayyagari;

import com.mirth.connect.client.ui.AbstractChannelTabPanel;
import com.mirth.connect.plugins.ChannelTabPlugin;

public class VersionHistoryTabPlugin extends ChannelTabPlugin {

    private VersionHistoryTabPanel tabPanel;
    
    public VersionHistoryTabPlugin(String name) {
        super(GitExtServletInterface.PLUGIN_NAME);
        System.out.println("instantiated VersionHistoryTabPlugin " + name);
    }

    @Override
    public void start() {
        GitExtServletInterface gitServlet = parent.mirthClient.getServlet(GitExtServletInterface.class);
        tabPanel = new VersionHistoryTabPanel(gitServlet);
        System.out.println("started VersionHistoryTabPlugin");
    }

    @Override
    public AbstractChannelTabPanel getChannelTabPanel() {
        return tabPanel;
    }

    @Override
    public String getPluginPointName() {
        return "Version History";
    }

}
