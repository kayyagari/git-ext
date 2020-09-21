package com.kayyagari;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.SwingUtilities;

import com.mirth.connect.client.ui.AbstractChannelTabPanel;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.model.Channel;

public class VersionHistoryTabPanel extends AbstractChannelTabPanel {

    private RevisionInfoTable tblRevisions;

    private GitExtServletInterface gitServlet;

    public VersionHistoryTabPanel(GitExtServletInterface gitServlet) {
        this.gitServlet = gitServlet;
        setLayout(new BorderLayout());

        tblRevisions = new RevisionInfoTable();
        add(tblRevisions);
    }

    @Override
    public void load(Channel channel) {
        String cid = channel.getId();
        System.out.println("loading channel " + cid);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    List<RevisionInfo> revisions = gitServlet.getHistory(cid);
                    RevisionInfoTableModel model = new RevisionInfoTableModel(revisions);
                    tblRevisions.setModel(model);
                } catch (Exception e) {
                    PlatformUI.MIRTH_FRAME.alertThrowable(PlatformUI.MIRTH_FRAME, e);
                }
            }
        });
    }

    @Override
    public void save(Channel channel) {
        System.out.println("saving channel " + channel.getId());
    }
}
