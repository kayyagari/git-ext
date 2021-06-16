package com.kayyagari;

import com.mirth.connect.client.ui.AbstractChannelTabPanel;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.InvalidChannel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class VersionHistoryTabPanel extends AbstractChannelTabPanel {

    private RevisionInfoTable tblRevisions;

    private GitExtServletInterface gitServlet;

    private String cid;

    private JPopupMenu popupMenu;
    
    private JMenuItem mnuShowDiff;

    private Frame parent;
    
    private ObjectXMLSerializer serializer;

    public VersionHistoryTabPanel(Frame parent) {
        this.parent = parent;
        this.serializer = ObjectXMLSerializer.getInstance();
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
                    mnuShowDiff.setEnabled(tblRevisions.getSelectedRowCount() == 2);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };
        tblRevisions.addMouseListener(popupListener);
        JScrollPane scrollPane = new JScrollPane(tblRevisions);
        scrollPane.addMouseListener(popupListener);
        add(scrollPane);
    }

    @Override
    public void load(Channel channel) {
        cid = channel.getId();
        //System.out.println("loading channel " + cid);
        SwingUtilities.invokeLater(new Runnable() {
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

            String labelPrefix = leftCh.getName();
            String leftLabel = labelPrefix + " Revision: " + ri1.getShortHash();
            String rightLabel = labelPrefix + " Revision: " + ri2.getShortHash();
            DiffWindow dw = DiffWindow.create("Channel Diff", leftLabel, rightLabel, leftCh, rightCh, left, right);
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
}
