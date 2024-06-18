package com.kayyagari;

/*
   Copyright [2024] [Kiran Ayyagari]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

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
