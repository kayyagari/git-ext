package com.kayyagari;

import com.mirth.connect.client.ui.AbstractChannelTabPanel;
import com.mirth.connect.plugins.ChannelTabPlugin;

/**
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class VersionHistoryTabPlugin extends ChannelTabPlugin {

    private VersionHistoryTabPanel tabPanel;

    public VersionHistoryTabPlugin(String name) {
        super(GitExtServletInterface.PLUGIN_NAME);
        System.out.println("instantiated VersionHistoryTabPlugin " + name);
    }

    @Override
    public void start() {
        tabPanel = new VersionHistoryTabPanel(parent);
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
