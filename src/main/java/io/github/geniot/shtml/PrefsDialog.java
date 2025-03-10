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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Dialog to set user preferences for application SimplyHTML.
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
class PrefsDialog extends DialogShell implements ActionListener {
    /** the look and feels avaliable in the system */
    private UIManager.LookAndFeelInfo[] lfinfo;
    /** reference for user preferences for this class */
    protected Preferences prefs = Preferences.userNodeForPackage(getClass());
    /** constant for dock location setting in preferences file */
    public static final String PREFSID_LOOK_AND_FEEL = "Laf";
    public static final String PREFS_USE_STD_STYLE_SHEET = "use_std_styles";
    public static final String PREFS_DEFAULT_PASTE_MODE ="default_paste_mode";
    public static final String PREFS_IMAGES_COPIED_BY_EDITOR ="images_copied_by_editor";
    private final String lafName = UIManager.getLookAndFeel().getName();
    private final JComboBox lafCombo;
    private final JCheckBox useStdStyleSheet;
    private final JComboBox pasteModeCombo;
    /** the help id for this dialog */
    private static final String helpTopicId = "item167";
    private final List<SHTMLPrefsChangeListener> prefChangeListeners = new LinkedList<SHTMLPrefsChangeListener>();

    public PrefsDialog(final Frame parent, final String title) {   	
        super(parent, title, helpTopicId);
        // have a grid bag layout ready to use
        final GridBagLayout g = new GridBagLayout();
        final GridBagConstraints c = new GridBagConstraints();
        final JPanel layoutPanel = new JPanel(g);
        // build a panel for preferences related to the application
        final JPanel appPrefsPanel = new JPanel(g);
        Util.addGridBagComponent(appPrefsPanel, new JLabel(Util.getResourceString("prfLafLabel")), g, c, 0, 0,
            GridBagConstraints.EAST);
        
        lafCombo = new JComboBox();
        initLfComboBox();
        Util.addGridBagComponent(appPrefsPanel, lafCombo, g, c, 1, 0, GridBagConstraints.EAST);
        
        pasteModeCombo = new JComboBox();
        initPasteModeComboBox();
        Util.addGridBagComponent(appPrefsPanel, new JLabel(Util.getResourceString("prefsPasteModeLabel")), g, c, 0, 1,
                GridBagConstraints.EAST);
        Util.addGridBagComponent(appPrefsPanel, pasteModeCombo, g, c, 1, 1, GridBagConstraints.EAST);
        
        // build a panel for preferences related to documents
        /*
        JPanel docPrefsPanel = new JPanel(g);
        Util.addGridBagComponent(docPrefsPanel,
                                 new JCheckBox(
                                 FrmMain.dynRes.getResourceString(
                                 FrmMain.resources, "prfShareDocResourcesLabel")),
                                 g, c, 0, 1,
                                 GridBagConstraints.EAST);
        */
        Util.addGridBagComponent(layoutPanel, appPrefsPanel, g, c, 0, 0, GridBagConstraints.WEST);
        // add option for standard stlye sheet
        useStdStyleSheet = new JCheckBox(Util.getResourceString("linkDefaultStyleSheetLabel"));
        final boolean useStyle = prefs.getBoolean(PrefsDialog.PREFS_USE_STD_STYLE_SHEET, false);
        useStdStyleSheet.setSelected(useStyle);
        Util.addGridBagComponent(layoutPanel, useStdStyleSheet, g, c, 0, 2, GridBagConstraints.WEST);
                
        // add to content pane of DialogShell
        final Container contentPane = super.getContentPane();
        contentPane.add(layoutPanel, BorderLayout.CENTER);
        //contentPane.add(appPrefsPanel, BorderLayout.NORTH);
        //contentPane.add(docPrefsPanel, BorderLayout.CENTER);
        // cause optimal placement of all elements
        pack();
    }
    
    public void addPrefChangeListener(final SHTMLPrefsChangeListener listener)
    {
    	prefChangeListeners.add(listener);
    }
    
    public void removePrefChangeListener(final SHTMLPrefsChangeListener listener)
    {
    	prefChangeListeners.remove(listener);
    }

    private void initLfComboBox() {
        lfinfo = UIManager.getInstalledLookAndFeels();
        final int count = lfinfo.length;
        final String[] lfNames = new String[count];
        for (int i = 0; i < count; i++) {
            lfNames[i] = lfinfo[i].getName();
        }
        lafCombo.setModel(new DefaultComboBoxModel(lfNames));
        lafCombo.setSelectedItem(lafName);
    }
    
    private void initPasteModeComboBox()
    {
    	pasteModeCombo.setModel(new DefaultComboBoxModel(SHTMLEditorPane.PasteMode.values()));
    	pasteModeCombo.setSelectedItem(
    			SHTMLEditorPane.PasteMode.valueOf(SHTMLEditorPane.PasteMode.class, prefs.get(PREFS_DEFAULT_PASTE_MODE, SHTMLEditorPane.PasteMode.PASTE_HTML.name())));
    	pasteModeCombo.setRenderer(new ListCellRenderer() {

			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) 
			{
				switch ((SHTMLEditorPane.PasteMode)value)
				{
				case PASTE_HTML:
					return new JLabel(Util.getResourceString("pasteModeHTML"));
				case PASTE_PLAIN_TEXT:
					return new JLabel(Util.getResourceString("pasteModePlainText"));
				default:
					throw new AssertionError();
				}
			}
    		
    	});
    }

    /**
     * implements the ActionListener interface to be notified of
     * clicks onto the ok and cancel button.
     */
    public void actionPerformed(final ActionEvent e) {
        final Component src = (Component) e.getSource();
        if (src == okButton) {
            savePrefs(src);
        }
        super.actionPerformed(e);
    }

    private void savePrefs(final Component src) {
        try {
            final String newLaf = lfinfo[lafCombo.getSelectedIndex()].getClassName();
            if (!lafName.equalsIgnoreCase(newLaf)) {
                prefs.put(PREFSID_LOOK_AND_FEEL, newLaf);
                UIManager.setLookAndFeel(newLaf);
                SwingUtilities.updateComponentTreeUI(JOptionPane.getFrameForComponent(src));
            }
            boolean oldStyleSheetPref = prefs.getBoolean(PREFS_USE_STD_STYLE_SHEET, false);
            prefs.putBoolean(PREFS_USE_STD_STYLE_SHEET, useStdStyleSheet.isSelected());
            String oldDefaultPasteMode = prefs.get(PREFS_DEFAULT_PASTE_MODE, SHTMLEditorPane.PasteMode.PASTE_HTML.name());
            prefs.put(PREFS_DEFAULT_PASTE_MODE, ((SHTMLEditorPane.PasteMode)pasteModeCombo.getSelectedItem()).name());

            for (SHTMLPrefsChangeListener listener: prefChangeListeners)
            {
            	listener.shtmlPrefChanged(PREFS_USE_STD_STYLE_SHEET, new Boolean(useStdStyleSheet.isSelected()).toString(),
            			new Boolean(oldStyleSheetPref).toString());
            
            	listener.shtmlPrefChanged(PREFS_DEFAULT_PASTE_MODE, prefs.get(PREFS_DEFAULT_PASTE_MODE, null),
            			oldDefaultPasteMode);
            }
        }
        catch (final Exception ex) {
            Util.errMsg(this, ex.getMessage(), ex);
        }
    }
}
