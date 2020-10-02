package com.kayyagari;

import java.awt.BorderLayout;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Collections;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.apache.commons.io.IOUtils;

import com.kayyagari.objmeld.OgnlComparison;
import com.kayyagari.objmeld.StringContent;
import com.mirth.connect.client.ui.ChannelSetup;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

/**
 * The main window for showing diff.
 * 
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class DiffWindow extends JDialog {
    private Object left;
    private Object right;
    private String leftLabel;
    private String rightLabel;
    private JTabbedPane tabbedPane;
    private JPanel objDiffPanel = null; // object diff panel
    
    private String leftStrContent;
    private String rightStrContent;
    
    private JPanel labelPanel;
    private DiffWindow(String title, String leftLabel, String rightLabel, Object left, Object right) {
        this.left = left;
        this.right = right;
        this.leftLabel = leftLabel;
        this.rightLabel = rightLabel;
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);

        labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(new JLabel("\t" + leftLabel), BorderLayout.EAST);
        labelPanel.add(new JLabel(rightLabel + "\t"), BorderLayout.WEST);

        setTitle(title);
        add(tabbedPane);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public static DiffWindow create(String title, String leftLabel, String rightLabel, Object leftObj, Object rightObj, String leftStrContent, String rightStrContent) {
        DiffWindow dd = new DiffWindow(title, leftLabel, rightLabel, leftObj, rightObj);
        dd.prepareObjectView();
        //dd.prepareChannelView();
        dd.prepareTextView(leftStrContent, rightStrContent);
        return dd;
    }
    
    private void prepareObjectView() {
        try {
            ObjectDiff od = new ObjectDiff(left, right);
            od.create();
            objDiffPanel = od.getVisualPanel();
        }
        catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            JTextArea errorArea = new JTextArea();
            errorArea.setText(sw.toString());
            objDiffPanel = new JPanel(new BorderLayout());
            objDiffPanel.add(errorArea);
        }
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(labelPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(objDiffPanel), BorderLayout.CENTER);
        tabbedPane.add("Object View", panel);
    }
    
    private void prepareChannelView() {
        if(!(left instanceof Channel && right instanceof Channel)) {
           return; 
        }
        
        JSplitPane channelPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        channelPane.setDividerLocation(0.5);
        ChannelSetup channelLeft = new ChannelSetup();
        channelLeft.addChannel((Channel)left, "");
        channelPane.setLeftComponent(channelLeft);

        ChannelSetup channelRight = new ChannelSetup();
        channelRight.addChannel((Channel)right, "");
        channelPane.setRightComponent(channelRight);

        tabbedPane.add("Channel View", channelPane);
    }
    
    private void prepareTextView(String leftStrContent, String rightStrContent) {
        JPanel panel = OgnlComparison.prepare(Collections.singletonList(new StringContent("", leftStrContent)), Collections.singletonList(new StringContent("", rightStrContent)), true);
        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(panel);
        tabbedPane.add("XML View", panel2);
    }
    
    public static void main(String[] args) throws Exception {
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        serializer.init("3.9.1");
        
        String chXml1 = IOUtils.resourceToString("channel-for-diffing-version1.xml", Charset.forName("utf-8"), ObjectDiff.class.getClassLoader());
        Channel chLeft = serializer.deserialize(chXml1, Channel.class);
        
        String chXml2 = IOUtils.resourceToString("channel-for-diffing-version2.xml", Charset.forName("utf-8"), ObjectDiff.class.getClassLoader());
        Channel chRight = serializer.deserialize(chXml2, Channel.class);
        
        DiffWindow dw = DiffWindow.create("Channel Diff", "left rev abcd", "right rev efgh", chLeft, chRight, chXml1, chXml2);
        dw.validate();
        dw.setSize(1100, 700);
        dw.setVisible(true);
    }
}
