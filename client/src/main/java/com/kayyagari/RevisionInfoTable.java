package com.kayyagari;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;

public class RevisionInfoTable extends MirthTable {

    public RevisionInfoTable() {
        super();
        Highlighter rowStriper = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
        setHighlighters(rowStriper);
    }
}
