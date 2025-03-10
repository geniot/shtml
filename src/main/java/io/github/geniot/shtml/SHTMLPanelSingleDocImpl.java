/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Created on 07.10.2006
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

public class SHTMLPanelSingleDocImpl extends SHTMLPanelImpl {
    public SHTMLPanelSingleDocImpl(boolean isComponent, boolean showContextMenu, boolean showMenuBar, boolean showToolbar) {
        super(isComponent, showContextMenu, showMenuBar, showToolbar);
    }

    /* (non-Javadoc)
     * @see com.lightdev.app.shtm.SHTMLPanelImpl#initDocumentPane()
     */
    protected void initDocumentPane(boolean isComponent) {
//        super.initDocumentPane(isComponent);
        setDocumentPane(new DocumentPane(null, 1, isComponent));
        setEditorPane(getDocumentPane().getEditor());
        doc = (SHTMLDocument) getEditorPane().getDocument();
        registerDocument();
        getDocumentPane().getEditor().setCaretPosition(0);
        splitPanel.addComponent(getDocumentPane(), SplitPanel.CENTER);
    }

    protected void initActions() {
        super.initActions();
        addAction(findReplaceAction, new SHTMLEditorKitActions.SingleDocFindReplaceAction(this));
    }
}
