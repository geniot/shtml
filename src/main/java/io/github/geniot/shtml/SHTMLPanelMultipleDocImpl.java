/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Created on 04.10.2006
 * Copyright (C) 2006 Dimitri Polivaev
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package io.github.geniot.shtml;

import java.awt.event.ActionEvent;

import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.html.HTML;

public class SHTMLPanelMultipleDocImpl extends SHTMLPanelImpl implements ChangeListener {
    public static final String newAction = "new";
    public static final String openAction = "open";
    public static final String closeAction = "close";
    public static final String closeAllAction = "closeAll";
    public static final String saveAction = "save";
    public static final String saveAsAction = "saveAs";
    /** the tabbed pane for adding documents to show to */
    private JTabbedPane jtpDocs;
    /** tool bar selector for styles */
    private StyleSelector styleSelector;
    /** number of currently active tab */
    private int activeTabNo;

    public SHTMLPanelMultipleDocImpl(boolean isComponent, boolean showContextMenu, boolean showMenuBar, boolean showToolbar) {
        super(isComponent, showContextMenu, showMenuBar, showToolbar);
    }

    protected void initDocumentPane(boolean isComponent) {
        dynRes.getAction(newAction).actionPerformed(null);
        getDocumentPane().getEditor().setCaretPosition(0);
    }

    /* (non-Javadoc)
     * @see com.lightdev.app.shtm.SHTMLPanelImpl#initActions()
     */
    protected void initActions() {
        super.initActions();
        addAction(findReplaceAction, new SHTMLEditorKitActions.MultipleDocFindReplaceAction(this));
        addAction(setStyleAction, new SHTMLEditorKitActions.SetStyleAction(this));
        addAction(newAction, new SHTMLEditorKitActions.SHTMLFileNewAction(this));
        addAction(openAction, new SHTMLEditorKitActions.SHTMLFileOpenAction(this));
        addAction(closeAction, new SHTMLEditorKitActions.SHTMLFileCloseAction(this));
        addAction(closeAllAction, new SHTMLEditorKitActions.SHTMLFileCloseAllAction(this));
        addAction(saveAction, new SHTMLEditorKitActions.SHTMLFileSaveAction(this));
        addAction(saveAllAction, new SHTMLEditorKitActions.SHTMLFileSaveAllAction(this));
        addAction(saveAsAction, new SHTMLEditorKitActions.SHTMLFileSaveAsAction(this));
        addAction(exitAction, new SHTMLEditorKitActions.SHTMLFileExitAction(this));
    }

    /* (non-Javadoc)
     * @see com.lightdev.app.shtm.SHTMLPanelImpl#customizeFrame()
     */
    protected void customizeFrame(boolean isComponent, boolean showMenuBar, boolean showToolbar) {
        jtpDocs = new JTabbedPane();
        super.customizeFrame(isComponent, showMenuBar, showToolbar);
        jtpDocs.addChangeListener(this);
        splitPanel.addComponent(jtpDocs, SplitPanel.CENTER);
    }

    /* (non-Javadoc)
     * @see com.lightdev.app.shtm.SHTMLPanelImpl#createToolbarItem(javax.swing.JToolBar, java.lang.String)
     */
    protected void createToolbarItem(final JToolBar toolBar, final String itemKey) {
        if (itemKey.equalsIgnoreCase(setStyleAction)) {
            styleSelector = new StyleSelector(this, HTML.Attribute.CLASS);
            styleSelector.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXX");
            styleSelector.setAction(dynRes.getAction(setStyleAction));
            jtpDocs.addChangeListener(styleSelector);
            toolBar.add(styleSelector);
        }
        else {
            super.createToolbarItem(toolBar, itemKey);
        }
    }

    /* (non-Javadoc)
     * @see com.lightdev.app.shtm.SHTMLPanelImpl#registerDocument()
     */
    protected void registerDocument() {
        super.registerDocument();
        ((SHTMLDocument) getDocumentPane().getDocument()).getStyleSheet().addChangeListener(styleSelector);
    }

    /* (non-Javadoc)
     * @see com.lightdev.app.shtm.SHTMLPanelImpl#unregisterDocument()
     */
    protected void unregisterDocument() {
        super.unregisterDocument();
        ((SHTMLDocument) getDocumentPane().getDocument()).getStyleSheet().removeChangeListener(styleSelector);
    }

    /**
     * catch requests to close the application's main frame to
     * ensure proper clean up before the application is
     * actually terminated.
     */
    boolean close() {
        dynRes.getAction(exitAction).actionPerformed(new ActionEvent(this, 0, exitAction));
        return jtpDocs.getTabCount() == 0;
    }

    /**
     * change listener to be applied to our tabbed pane
     * so that always the currently active components
     * are known
     */
    public void stateChanged(final ChangeEvent e) {
        activeTabNo = jtpDocs.getSelectedIndex();
        if (activeTabNo >= 0) {
            setDocumentPane((DocumentPane) jtpDocs.getComponentAt(activeTabNo));
            setEditorPane(getDocumentPane().getEditor());
            //System.out.println("FrmMain stateChanged docName now " + documentPane.getDocumentName());
            doc = (SHTMLDocument) getSHTMLEditorPane().getDocument();
            //fireDocumentChanged();
            if (!ignoreActivateDoc) {
                getDocumentPane().fireActivated();
            }
        }
        else {
            setDocumentPane(null);
            setEditorPane(null);
            doc = null;
        }
    }

    /**
     * @return Returns the jtpDocs.
     */
    JTabbedPane getTabbedPaneForDocuments() {
        return jtpDocs;
    }

    /* (non-Javadoc)
     * @see com.lightdev.app.shtm.SHTMLPanelImpl#updateFormatControls()
     */
    void updateFormatControls() {
        super.updateFormatControls();
        styleSelector.update();
    }

    void incNewDocCounter() {
        newDocCounter++;
    }

    void createNewDocumentPane() {
        setDocumentPane(new DocumentPane(null, ++newDocCounter, false));
    }

    void selectTabbedPane(final int index) {
        ignoreActivateDoc = true;
        getTabbedPaneForDocuments().setSelectedIndex(index);
        ignoreActivateDoc = false;
    }

    int getActiveTabNo() {
        return activeTabNo;
    }

    /**
     * @param activeTabNo The activeTabNo to set.
     */
    void setActiveTabNo(final int activeTabNo) {
        this.activeTabNo = activeTabNo;
    }
}
