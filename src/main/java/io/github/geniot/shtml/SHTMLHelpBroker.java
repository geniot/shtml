/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Created on 16.12.2006
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

import java.awt.event.KeyEvent;
import java.net.URL;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

class SHTMLHelpBroker {
    public static final String APP_HELP_NAME = "help";
    public static final String JAVA_HELP_EXT = ".hs";

    private SHTMLHelpBroker() {
    }

    /** our help broker */
    private static HelpBroker helpBroker;

    /**
     * get the <code>HelpBroker</code> of our application
     *
     * @return the <code>HelpBroker</code> to be used for help display
     */
    private static HelpBroker getHelpBroker() {
        if (helpBroker == null) {
            final URL url = Thread.currentThread().getContextClassLoader().getResource(APP_HELP_NAME + Util.URL_SEPARATOR + APP_HELP_NAME
                    + JAVA_HELP_EXT);
            HelpSet hs;
            try {
                hs = new HelpSet(null, url);
            }
            catch (final HelpSetException e) {
                return null;
            }
            helpBroker = hs.createHelpBroker();
        }
        return helpBroker;
    }

    static AbstractButton createHelpButton(final String helpTopicId) {
        AbstractButton newButton;
        newButton = new JButton();
        CSH.setHelpIDString(newButton, helpTopicId);
        newButton.addActionListener(new CSH.DisplayHelpFromSource(SHTMLHelpBroker.getHelpBroker()));
        return newButton;
    }

    static void initJavaHelpItem(final JMenuItem mi, final String helpTopicId) {
        CSH.setHelpIDString(mi, helpTopicId);
        mi.addActionListener(new CSH.DisplayHelpFromSource(SHTMLHelpBroker.getHelpBroker()));
        mi.setIcon(DynamicResource.getIconForCommand(SHTMLPanelImpl.getUiResources(), SHTMLPanelImpl.helpTopicsAction));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        mi.setEnabled(true);
    }
}
