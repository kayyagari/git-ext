package com.kayyagari;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.joda.time.Period;

/**
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class RevisionInfoTableModel extends AbstractTableModel {

    private List<RevisionInfo> revisions;

    private static DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    private static final String[] columnNames = {"Id", "Message", "Committer", "Date"};

    public RevisionInfoTableModel(List<RevisionInfo> revisions) {
        this.revisions = revisions;
    }

    @Override
    public int getRowCount() {
        return revisions.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if(columnIndex == 0) {
            return RevisionInfo.class;
        }

        return super.getColumnClass(columnIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object val = null;
        RevisionInfo r = revisions.get(rowIndex);

        switch (columnIndex) {
        case 0:
            val = r.getShortHash();
            break;

        case 1:
            val = r.getMessage();
            break;

        case 2:
            val = r.getCommitterName();
            break;

        case 3:
            val = formatTime(r.getTime());
            break;

        default:
            throw new IllegalArgumentException("unknown column number " + columnIndex);
        }
        return val;
    }

    public RevisionInfo getRevisionAt(int row) {
        return revisions.get(row);
    }

    private String formatTime(long t) {
        Period period = new Period(t, System.currentTimeMillis());
        String txt = null;
        int hours = period.getHours();
        if(hours > 0) {
            txt = df.format(new Date(t));
        }
        else {
            int min = period.getMinutes();
            if(min > 0) {
                txt = min + " minutes ago";
            }
            else {
                txt = period.getSeconds() + " seconds ago";
            }
        }

        return txt;
    }
}
