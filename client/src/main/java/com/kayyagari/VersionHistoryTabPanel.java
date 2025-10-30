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
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.InvalidChannel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class VersionHistoryTabPanel extends AbstractChannelTabPanel {
    private static Logger log = LoggerFactory.getLogger(VersionHistoryTabPanel.class);

    private RevisionInfoTable tblRevisions;

    private GitExtServletInterface gitServlet;

    private String cid;
    private String channelName;

    private JPopupMenu popupMenu;
    
    private JMenuItem mnuShowDiff;
    private JMenuItem mnuRevert;

    private Frame parent;
    
    private ObjectXMLSerializer serializer;

    public VersionHistoryTabPanel(Frame parent) {
        this.parent = parent;
        this.serializer = ObjectXMLSerializer.getInstance();
        this.serializer.allowTypes(Collections.EMPTY_LIST, Arrays.asList(RevisionInfo.class.getPackage().getName() + ".**"), Collections.EMPTY_LIST);
        setLayout(new BorderLayout());

        tblRevisions = new RevisionInfoTable();
        tblRevisions.setRowSelectionAllowed(true);
        tblRevisions.setColumnSelectionAllowed(false);
        tblRevisions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        popupMenu = new JPopupMenu();
        mnuShowDiff = new JMenuItem("Show Diff");
        mnuShowDiff.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDiffWindow();
            }
        });
        popupMenu.add(mnuShowDiff);

        mnuRevert = new JMenuItem("Revert to this version");
        mnuRevert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                revertToRevision();
            }
        });
        popupMenu.add(mnuRevert);

        MouseAdapter popupListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handlePopupEvent(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handlePopupEvent(e);
            }

            public void handlePopupEvent(MouseEvent e) {
                if(e.isPopupTrigger()) {
                    //System.out.println("popup triggered");
                    int selectedRows = tblRevisions.getSelectedRowCount();
                    mnuShowDiff.setEnabled(selectedRows == 2);

                    if(selectedRows == 1) {
                        int index = tblRevisions.getSelectedRow();
                        // select only when it is not the latest revision
                        mnuRevert.setEnabled(index != 0);
                    }
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };
        tblRevisions.addMouseListener(popupListener);
        JScrollPane scrollPane = new JScrollPane(tblRevisions);
        scrollPane.addMouseListener(popupListener);
        add(scrollPane);

        parent.addTask("loadHistory", "Refresh history", "Refresh version history.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_refresh.png")), parent.channelEditTasks, parent.channelEditPopupMenu, this);
    }

    @Override
    public void load(Channel channel) {
        cid = channel.getId();
        channelName = channel.getName();
        this.loadHistory(false);
    }

    @Override
    public void save(Channel channel) {
        log.info("saving channel " + channel.getId());
    }

    public void loadHistory() {
        this.loadHistory(true);
    }

    public void loadHistory(boolean shouldNotifyOnComplete) {
        SwingUtilities.invokeLater(new LoadGitHistoryRunnable(shouldNotifyOnComplete));
    }
    
    private void showDiffWindow() {
        popupMenu.setVisible(false);
        int[] rows = tblRevisions.getSelectedRows();
        RevisionInfoTableModel model = (RevisionInfoTableModel)tblRevisions.getModel();
        RevisionInfo ri1 = model.getRevisionAt(rows[0]);
        RevisionInfo ri2 = model.getRevisionAt(rows[1]);

        try {
            String left = gitServlet.getContent(cid, ri1.getHash());
            Channel leftCh = parse(left, ri1.getShortHash());
            String right = gitServlet.getContent(cid, ri2.getHash());
            Channel rightCh = parse(right, ri2.getShortHash());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String leftLabel = String.format("Revision: %s (user: %s, time:%s)", ri1.getShortHash(), ri1.getCommitterName(), sdf.format(new Date(ri1.getTime())));
            String rightLabel = String.format("Revision: %s (user: %s, time:%s)", ri2.getShortHash(), ri2.getCommitterName(), sdf.format(new Date(ri2.getTime())));
            DiffWindow dw = DiffWindow.create("Channel Diff - " + channelName, leftLabel, rightLabel, leftCh, rightCh, left, right);
            dw.setSize(parent.getWidth() - 10, parent.getHeight()-10);
            dw.setVisible(true);
        }
        catch(Exception e) {
            PlatformUI.MIRTH_FRAME.alertThrowable(PlatformUI.MIRTH_FRAME, e);
        }
    }
    
    private Channel parse(String xml, String rev) {
        Channel ch = serializer.deserialize(xml, Channel.class);
        if(ch instanceof InvalidChannel) {
            throw new IllegalStateException("could not parse channel at revision " + rev);
        }
        
        return ch;
    }

    private void revertToRevision() {
        popupMenu.setVisible(false);
        int row = tblRevisions.getSelectedRow();
        RevisionInfoTableModel model = (RevisionInfoTableModel)tblRevisions.getModel();
        RevisionInfo targetRevision = model.getRevisionAt(row);
        try {
            boolean reverted = gitServlet.revertChannel(cid, targetRevision.getHash());
            if(reverted) {
                PlatformUI.MIRTH_FRAME.channelPanel.clearChannelCache();
                PlatformUI.MIRTH_FRAME.channelPanel.retrieveChannels(true);
                PlatformUI.MIRTH_FRAME.channelPanel.switchPanel();
            }
        }
        catch(Exception e) {
            PlatformUI.MIRTH_FRAME.alertThrowable(PlatformUI.MIRTH_FRAME, e);
        }
    }

    private class LoadGitHistoryRunnable implements Runnable {
        private boolean shouldNotifyOnComplete;

        LoadGitHistoryRunnable(boolean shouldNotifyOnComplete) {
            this.shouldNotifyOnComplete = shouldNotifyOnComplete;
        }

        @Override
        public void run() {
            try {
                // initialize once
                // doing here because do not want to delay the startup of MC client which takes several seconds to start.
                if(gitServlet == null) {
                    gitServlet = parent.mirthClient.getServlet(GitExtServletInterface.class);
                }

                // then fetch revisions
                List<RevisionInfo> revisions = gitServlet.getHistory(cid);
                RevisionInfoTableModel model = new RevisionInfoTableModel(revisions);
                tblRevisions.setModel(model);

                if (shouldNotifyOnComplete) {
                    PlatformUI.MIRTH_FRAME.alertInformation(parent, "History refreshed!");
                }
            } catch (Exception e) {
                PlatformUI.MIRTH_FRAME.alertThrowable(PlatformUI.MIRTH_FRAME, e);
            }
        }
    }
}
