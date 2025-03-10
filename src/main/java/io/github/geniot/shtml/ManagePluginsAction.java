/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2002 Ulrich Hilger
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

/**
 * Action to invoke a PluginManagerDialog
 *
 * @author Ulrich Hilger
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the
 *      GNU General Public License,
 *      for details see file gpl.txt in the distribution
 *      package of this software
 *
 * 
 */
class ManagePluginsAction extends AbstractAction implements SHTMLAction {
    public static final String managePluginsAction = "managePlugins";

    public ManagePluginsAction() {
        super();
        SHTMLPanelImpl.configureActionProperties(this, managePluginsAction);
    }

    public void actionPerformed(final ActionEvent e) {
        final JPopupMenu menu = (JPopupMenu) ((Component) e.getSource()).getParent();
        final SHTMLPanelImpl shtmlPanel = (SHTMLPanelImpl) SwingUtilities.getAncestorOfClass(SHTMLPanelImpl.class,
            menu.getInvoker());
        final PluginManagerDialog pmd = new PluginManagerDialog(JOptionPane.getFrameForComponent(shtmlPanel),
            Util.getResourceString("pluginManagerDialogTitle"));
        Util.center(shtmlPanel, pmd);
        pmd.setModal(true);
        pmd.setVisible(true);
        /** if the user made a selection, apply it to the document */
        if (pmd.getResult() == DialogShell.RESULT_OK) {
            shtmlPanel.clearDockPanels();
            final Enumeration plugins = SHTMLPanelImpl.pluginManager.plugins();
            SHTMLPlugin pi;
            while (plugins.hasMoreElements()) {
                pi = (SHTMLPlugin) plugins.nextElement();
                shtmlPanel.refreshPluginDisplay(pi);
            }
            shtmlPanel.paintComponents(shtmlPanel.getGraphics());
        }
        shtmlPanel.adjustDividers();
        shtmlPanel.updateActions();
    }

    public void update() {
    }
}
