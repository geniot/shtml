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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.StyleSheet;

/**
 * Utility methods for application SimplyHTML.
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
public class Util {
    /* some constants */
    public static final String JAR_PREFIX = "jar:";
    public static final String JAR_EXTENSION = ".jar";
    public static final String FILE_PREFIX = "file:";
    public static final String CLASS_EXT = ".class";
    public static final String JAR_SEPARATOR = "!/";
    public static final String URL_SEPARATOR = "/";
    public static final char URL_SEPARATOR_CHAR = '/';
    public static final String CLASS_SEPARATOR = ".";
    public static final char CLASS_SEPARATOR_CHAR = '.';
    public static final String DIR_UP_INDICATOR = "..";
    public static final String RELATIVE_PREFIX = "../";
    public static final String PROTOCOL_SEPARATOR = ":";
    public static final String ANCHOR_SEPARATOR = "#";
    public static final String pct = "%";
    public static final String pt = "pt";
    public static final String px = "px";
    /** the default block size in bytes for file operations */
    private static int blockSize = 1024;
    private static Vector startTimes = new Vector();
    private static final String ERR_TITLE = "Error";
    private static String unit = "";
    /** a style sheet instanciated once for access to its utility methods */
    private static StyleSheet s = new StyleSheet();
    /* CSS Attribute constants */
    public static final String CSS_ATTRIBUTE_NORMAL = "normal";
    public static final String CSS_ATTRIBUTE_UNDERLINE = "underline";
    public static final String CSS_ATTRIBUTE_LINE_THROUGH = "line-through";
    public static final String CSS_ATTRIBUTE_NONE = "none";
    public static final String CSS_ATTRIBUTE_ALIGN_LEFT = "left";
    public static final String CSS_ATTRIBUTE_ALIGN_CENTER = "center";
    public static final String CSS_ATTRIBUTE_ALIGN_RIGHT = "right";

    public Util() {
    }

    /**
     * rename a file to have a given extension
     *
     * @param from  the file to rename
     * @param newExt  the new extension the file shall have
     *
     * @return the renamed file
     */
    public static File renameFile(final File from, final String newExt) {
        final String fileName = Util.removeExtension(from.getName());
        final String saveFileName = from.getParentFile().getAbsolutePath() + File.separator + fileName + newExt;
        final File newFile = new File(saveFileName);
        from.renameTo(newFile);
        return newFile;
    }

    /**
     * find the next link attribute from a given element upwards
     * through the element hierarchy
     *
     * @param elem  the element to start looking at
     *
     * @return the link attribute found, or null, if none was found
     */
    public static Object findLinkUp(Element elem) {
        //Element elem = ((SHTMLDocument) doc).getCharacterElement(selStart);
        Object linkAttr = null; //elem.getAttributes().getAttribute(HTML.Tag.A);
        Object href = null;
        while ((elem != null) && (linkAttr == null)) {
            linkAttr = elem.getAttributes().getAttribute(HTML.Tag.A);
            if (linkAttr != null) {
                href = ((AttributeSet) linkAttr).getAttribute(HTML.Attribute.HREF);
            }
            elem = elem.getParentElement();
        }
        if (linkAttr != null && href != null) {
            return linkAttr;
        }
        else {
            return null;
        }
    }

    /**
     * remove the extension from a file name
     *
     * @param  fileName  the file name to remove the extension from
     *
     * @return the file name without extension
     */
    public static String removeExtension(final String fileName) {
        String newName = fileName;
        final int pos = newName.lastIndexOf(".");
        if (pos > -1) {
            newName = fileName.substring(0, pos);
        }
        return newName;
    }

    /**
     * resolve sets of attributes that are recursively stored in each other
     *
     * @param style  the set of attributes containing other sets of attributes
     */
    public static AttributeSet resolveAttributes(final AttributeSet style) {
        final SimpleAttributeSet set = new SimpleAttributeSet();
        if (style != null) {
            final Enumeration names = style.getAttributeNames();
            Object value;
            Object key;
            while (names.hasMoreElements()) {
                key = names.nextElement();
                //System.out.println("Util resolveAttributes key=" + key);
                value = style.getAttribute(key);
                //System.out.println("Util resolveAttributes value=" + value);
                if ((!key.equals(StyleConstants.NameAttribute)) && (!key.equals(StyleConstants.ResolveAttribute))
                        && (!key.equals(AttributeSet.ResolveAttribute)) && (!key.equals(AttributeSet.NameAttribute))) {
                    set.addAttribute(key, value);
                }
                else {
                    if (key.equals(StyleConstants.ResolveAttribute) || key.equals(AttributeSet.ResolveAttribute)) {
                        //System.out.println("Util resolveAttributes resolving key=" + key);
                        set.addAttributes(Util.resolveAttributes((AttributeSet) value));
                    }
                }
            }
        }
        return set;
    }

    /**
     * get a name by asking from the user
     *
     * <p>Wrapper for JOptionPane with I18N support</p>
     *
     * @param initialName  the name initially shown in option pane
     * @param title  the title to be shown in the option pane
     * @param text  the text to be shown in the option pane
     *
     * @return the entered name or null if action was cancelled
     */
    public static String nameInput(final Frame parent, final String initialName, final String regex,
                                   final String title, final String text) {
        String name;
        do {
            final Object input = JOptionPane.showInputDialog(null, Util.getResourceString(text),
                Util.getResourceString(title), JOptionPane.QUESTION_MESSAGE, null, null, initialName);
            name = input == null ? null : input.toString();
        } while (name != null && !name.matches(regex));
        return name;
    }

    /**
     * Show a message with options to choose from
     *
     * <p>Wrapper for JOptionPane with I18N support</p>
     *
     * @param options  the options to be shown in the dialog
     * @param title  the title to be shown in the dialog
     * @param msg  the message to be shown in the dialog
     * @param item  a variable part to be shown before msg
     * @param sep  a separator for msg and item (return or blank etc.)
     *
     * @return the choice
     */
    public static int msgChoice(final int options, final String title, final String msg, final String item,
                                final String sep) {
        final String message = item + sep + Util.getResourceString(msg);
        return JOptionPane.showConfirmDialog(null, message, Util.getResourceString(title), options,
            JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Show a message with options to choose from
     *
     * <p>Wrapper for JOptionPane with I18N support</p>
     *
     * @param options  the options to be shown in the dialog
     * @param title  the title to be shown in the dialog
     * @param msg  the message to be shown in the dialog
     * @param item  a variable part to be shown before msg
     * @param sep  a separator for msg and item (return or blank etc.)
     *
     * @return true, if YES was chosen, false if not
     */
    public static boolean msg(final int options, final String title, final String msg, final String item,
                              final String sep) {
        return (Util.msgChoice(options, title, msg, item, sep) == JOptionPane.YES_OPTION);
    }

    /**
     * get names of all styles for a given tag
     *
     * @param styles  the style sheet to look for style names
     * @param tag  the tag to find style names for
     *
     * @return a Vector with all style names found
     */
    public static Vector getStyleNamesForTag(final StyleSheet styles, final String tag) {
        return Util.getStyleClassVector(tag, styles.getStyleNames());
    }

    /**
     * get names of all styles for a given tag
     *
     * @param styles  the style sheet to look for style names
     * @param tag  the tag to find style names for
     *
     * @return a Vector with all style names found
     */
    public static Vector getStyleNamesForTag(final AttributeSet styles, final String tag) {
        return Util.getStyleClassVector(tag, styles.getAttributeNames());
    }

    private static Vector getStyleClassVector(final String tag, final Enumeration e) {
        String name;
        final Vector v = new Vector(0);
        while (e.hasMoreElements()) {
            name = (String) e.nextElement();
            if (name.toLowerCase().startsWith(tag + ".")) {
                //System.out.println("getStyleClassVector adding name '" + name + "'");
                v.addElement(name.substring(2));
            }
        }
        return v;
    }

    /**
     * get the names of all styles found in a given StyleSheet
     *
     * @param styles  the StyleSheet to look for style names
     *
     * @return a Vector with all names found
     */
    public static Vector getStyleNames(final StyleSheet styles) {
        final Vector styleNames = new Vector(0);
        try {
            final Enumeration rules = styles.getStyleNames();
            while (rules.hasMoreElements()) {
                styleNames.addElement(rules.nextElement());
            }
        }
        catch (final Exception ee) {
            Util.errMsg(null, ee.getMessage(), ee);
        }
        return styleNames;
    }

    /**
     * delete a directory with all its contents
     *
     * <p>CAUTION: This method deletes all content of the given
     * directory including all subdirectories and their conent</p>
     *
     * @param dir the directory to delete
     */
    public static void deleteDir(final File dir) {
        if (dir.exists()) {
            final File[] list = dir.listFiles();
            for (int i = 0; i < list.length; i++) {
                if (list[i].isDirectory()) {
                    Util.deleteDir(list[i]);
                }
                else {
                    list[i].delete();
                }
            }
            dir.delete();
        }
    }

    /**
     * copies a single file.
     *
     * <p>If destFile already exists or if both files are the same
     * the method does nothing. The complete destination path will be
     * created before copying, if necessary.</p>
     *
     * @param srcFile   the file to copy from
     * @param destFile  the file to copy to
     */
    public static void copyFile(final File srcFile, final File destFile) throws FileNotFoundException, IOException {
        if (!srcFile.toString().equals(destFile.toString())) {
            if (!destFile.exists()) {
                File destDir;
                final byte[] buf = new byte[blockSize];
                int bytesRead = 0;
                destDir = new File(destFile.getParent());
                destDir.mkdirs();
                try(RandomAccessFile src = new RandomAccessFile(srcFile.getPath(), "r");
                        RandomAccessFile dest = new RandomAccessFile(destFile.getPath(), "rw")) {
                    bytesRead = src.read(buf);
                    while (bytesRead > -1) {
                        dest.write(buf, 0, bytesRead);
                        bytesRead = src.read(buf);
                    }
                }
            }
        }
    }

    /**
     * get the index of a given element in the list of its parents elements.
     *
     * @param elem  the element to get the index number for
     * @return the index of the given element
     */
    public static int getElementIndex(final Element elem) {
        int i = 0;
        final Element parent = elem.getParentElement();
        if (parent != null) {
            final int last = parent.getElementCount() - 1;
            Element anElem = parent.getElement(i);
            if (anElem != elem) {
                while ((i < last) && (anElem != elem)) {
                    anElem = parent.getElement(++i);
                }
            }
        }
        return i;
    }

    /**
     * Get the path of the class file for a given class.
     *
     * <p>This is either a directory of a class file or a directory of
     * a JAR file. Thus, this class must reside in the same place as
     * the application in question, not in a separate library
     * for instance.</p>
     *
     * @param cls  the class to get the path for
     *
     * @return the path of this class file or the path of the JAR file this class
     *      file resides in, whatever applies
     */
    public static String getClassFilePath(final Class cls) {
        int end = 0;
        String urlStr = null;
        String clsName = cls.getName();
        final int clsNameLen = clsName.length() + CLASS_EXT.length();
        int pos = clsName.lastIndexOf(CLASS_SEPARATOR);
        if (pos > -1) {
            clsName = clsName.substring(pos + 1);
        }
        clsName = clsName + CLASS_EXT;
        final URL url = cls.getResource(clsName);
        if (url != null) {
            urlStr = url.toString();
            pos = urlStr.indexOf(JAR_SEPARATOR);
            if (pos > -1) {
                urlStr = urlStr.substring(0, pos);
                end = urlStr.lastIndexOf(URL_SEPARATOR) + 1;
            }
            else {
                end = urlStr.length() - clsNameLen;
            }
            pos = urlStr.lastIndexOf(FILE_PREFIX);
            if (pos > -1) {
                pos += FILE_PREFIX.length() + 1;
            }
            else {
                pos = 0;
            }
            urlStr = urlStr.substring(pos, end);
            urlStr = urlStr.replaceAll("%20", " ");
        }
        return urlStr;
    }

    /**
     * quick hack for getting the point value from an attribute value string
     * (needs to be refined and consolidated with length value)
     *
     * @param valStr  the attribute value string to get the point size for
     *
     * @return the point size from the given attribute value
     */
    public static float getPtValue(String valStr) {
        float len = 0;
        int pos = valStr.indexOf(pt);
        if (pos > -1) {
            unit = pt;
            valStr = valStr.substring(0, pos);
            len = Float.valueOf(valStr).floatValue();
        }
        else {
            pos = valStr.indexOf(px);
            if (pos > -1) {
                unit = px;
                valStr = valStr.substring(0, pos);
                len = Float.valueOf(valStr).floatValue() * 1.3f;
            }
            else {
                pos = valStr.indexOf(pct);
                if (pos > -1) {
                    unit = pct;
                    valStr = valStr.substring(0, pos);
                    //System.out.println("Util.getPtValue valStr=" + valStr);
                    len = Float.valueOf(valStr).floatValue() / 100f;
                    //System.out.println("Util.getPtValue len=" + len);
                }
                else {
                    // assume relative value 1 .. 6
                    try {
                        len = Float.valueOf(valStr).floatValue();
                        unit = pt;
                        /*
                        switch((int) len) {
                          case 1:
                            len = 8;
                            break;
                          case 2:
                            len = 10;
                            break;
                          case 3:
                            len = 12;
                            break;
                          case 4:
                            len = 14;
                            break;
                          case 5:
                            len = 18;
                            break;
                          case 6:
                            len = 24;
                            break;
                          default:
                            len = len;
                            break;
                        }
                        */
                    }
                    catch (final Exception e) {
                        // unsupported number format (em ex, etc.)
                    }
                }
            }
        }
        return len;
    }

    /**
     * get the unit string from the last attribute object which was
     * converted to a numerical value
     *
     * @return the unit string from the last attribute object
     */
    public static String getLastAttrUnit() {
        return unit;
    }

    /**
     * get the numerical value for an attribute object
     *
     * @param attr  the attribute to get the value from
     *
     * @return  the numerical value
     */
    public static float getAttrValue(final Object attr) {
        float val = -1;
        if (attr != null) {
            val = Util.getPtValue(attr.toString());
            //System.out.println("Util.getAttrValue val=" + val);
        }
        return val;
    }

    /**
     * get the absolute value of an attribute
     *
     * @param attr  the attribute to get the value from
     *
     * @return the absolute numerical value
     */
    public static float getAbsoluteAttrVal(final Object attr) {
        String valStr = null;
        if (attr != null) {
            valStr = attr.toString();
            int pos = valStr.indexOf(pt);
            unit = pt;
            if (pos < 0) {
                pos = valStr.indexOf(pct);
                unit = pct;
                if (pos < 0) {
                    pos = valStr.indexOf(px);
                }
            }
            if (pos > -1) {
                valStr = valStr.substring(0, pos);
                return Float.valueOf(valStr).floatValue();
            }
            else {
                unit = "";
            }
        }
        try {
            return Float.valueOf(valStr).floatValue();
        }
        catch (final Exception e) {
            // unsupported number format (em ex, etc.)
            return 0f;
        }
    }

    /**
     * get the row index for a given table cell
     *
     * @param cell  the cell element to get the row index for
     *
     * @return the row index of the given cell element
     */
    public static int getRowIndex(final Element cell) {
        final Element thisRow = cell.getParentElement();
        final Element table = thisRow.getParentElement();
        int index = 0;
        final int count = table.getElementCount();
        Element elem = table.getElement(index);
        while (!elem.equals(thisRow) && index < count) {
            elem = table.getElement(++index);
        }
        return index;
    }

    /**
     * Get an arry of strings from a given string having several entries
     * delimited by blanks.
     *
     * <p>In the resource file of SimplyHTML for instance menu bar and menu
     * definitions are contained as strings having a key for each item.
     * The keys are delimited with blanks.</p>
     *
     * <p>A string "file edit help" from the resource file for instance
     * would be broken into an array of strings looking as follows</p>
     *
     * <p>
     * String[0]="file"<br>
     * String[1]="edit"<br>
     * String[2]="help"
     * </p>
     *
     * @param input  the string to transform into a string array
     * @return the resulting string array
     */
    public static String[] tokenize(final String input, final String delim) {
        final Vector v = new Vector();
        final StringTokenizer t = new StringTokenizer(input, delim);
        String result[];
        while (t.hasMoreTokens()) {
            v.addElement(t.nextToken());
        }
        result = new String[v.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (String) v.elementAt(i);
        }
        return result;
    }

    /**
     * write a message with a time stamp to System.out and remember
     * the time stamp in a LIFO Vector
     */
    public static void msgStart(final String startMsg) {
        final long startTime = System.currentTimeMillis();
        startTimes.addElement(new Long(startTime));
        //System.out.println(startMsg + " startTime=" + startTime);
    }

    /**
     * find the first occurrence of an <code>Element</code> in the
     * element tree above a given <code>Element</code>
     *
     * @param name the name of the <code>Element</code> to search for
     * @param start the <code>Element</code> to start looking
     *
     * @return the found <code>Element</code> or null if none is found
     */
    public static Element findElementUp(final String name, final Element start) {
        Element elem = start;
        while (elem != null && !elem.getName().equalsIgnoreCase(name)) {
            elem = elem.getParentElement();
        }
        return elem;
    }

    /**
     * find the first occurrence of an <code>Element</code> in the
     * element tree above a given <code>Element</code>
     *
     * @param name1 the primary name of the <code>Element</code> to search for
     * @param name2 an alternative name for the <code>Element</code> to search for
     * @param start the <code>Element</code> to start looking
     *
     * @return the found <code>Element</code> or null if none is found
     */
    public static Element findElementUp(final String name1, final String name2, final Element start) {
        Element elem = start;
        while (elem != null && !(elem.getName().equalsIgnoreCase(name1) || elem.getName().equalsIgnoreCase(name2))) {
            elem = elem.getParentElement();
        }
        return elem;
    }

    /**
     * find the first occurrence of an <code>Element</code> in the
     * element tree below a given <code>Element</code>
     *
     * @param name the name of the <code>Element</code> to search for
     * @param parent the <code>Element</code> to start looking
     *
     * @return the found <code>Element</code> or null if none is found
     */
    public static Element findElementDown(final String name, final Element parent) {
        Element foundElement = null;
        final ElementIterator eli = new ElementIterator(parent);
        Element thisElement = eli.first();
        while (thisElement != null && foundElement == null) {
            if (thisElement.getName().equalsIgnoreCase(name)) {
                foundElement = thisElement;
            }
            thisElement = eli.next();
        }
        return foundElement;
    }

    /**
     * convenience method for adding a component to a container
     * layed out by a GridBagLayout
     *
     * @param container  the container to add a component to
     * @param comp  the component to add to container
     * @param g  the GridBagLayout associated with container
     * @param c  the GridBagConstraints to use
     * @param gx  the value to use for GridBagConstraints.gridx
     * @param gy  the value to use for GridBagConstraints.gridy
     * @param a  the value to use for GridBagConstraints.anchor
     */
    public static void addGridBagComponent(final JComponent container, final JComponent comp, final GridBagLayout g,
                                           final GridBagConstraints c, final int gx, final int gy, final int a) {
        /*
        c.gridx = gx;
        c.gridy = gy;
        c.anchor = a;
        c.insets = new Insets(2, 2, 2, 2);
        c.ipadx = 2;
        c.ipady = 2;
        g.setConstraints(comp, c);
        container.add(comp);
        */
        Util.addGridBagComponent(container, comp, g, c, gx, gy, a, 1, 1, GridBagConstraints.NONE, 0, 0);
    }

    /**
     * convenience method for adding a component to a container
     * layed out by a GridBagLayout
     *
     * @param container  the container to add a component to
     * @param comp  the component to add to container
     * @param g  the GridBagLayout associated with container
     * @param c  the GridBagConstraints to use
     * @param gx  the value to use for GridBagConstraints.gridx
     * @param gy  the value to use for GridBagConstraints.gridy
     * @param a  the value to use for GridBagConstraints.anchor
     * @param gw  the value to use for GridBagConstraints.gridwidth
     * @param gh  teh value to use for GridBagConstraints.gridheight
     */
    public static void addGridBagComponent(final JComponent container, final JComponent comp, final GridBagLayout g,
                                           final GridBagConstraints c, final int gx, final int gy, final int a,
                                           final int gw, final int gh) {
        Util.addGridBagComponent(container, comp, g, c, gx, gy, a, gw, gh, GridBagConstraints.NONE, 0, 0);
    }

    /**
     * convenience method for adding a component to a container
     * layed out by a GridBagLayout
     *
     * @param container  the container to add a component to
     * @param comp  the component to add to container
     * @param g  the GridBagLayout associated with container
     * @param c  the GridBagConstraints to use
     * @param gx  the value to use for GridBagConstraints.gridx
     * @param gy  the value to use for GridBagConstraints.gridy
     * @param a  the value to use for GridBagConstraints.anchor
     * @param gw  the value to use for GridBagConstraints.gridwidth
     * @param gh  teh value to use for GridBagConstraints.gridheight
     * @param f  the value to use for GridBagConstraints.fill
     */
    public static void addGridBagComponent(final JComponent container, final JComponent comp, final GridBagLayout g,
                                           final GridBagConstraints c, final int gx, final int gy, final int a,
                                           final int gw, final int gh, final int f) {
        Util.addGridBagComponent(container, comp, g, c, gx, gy, a, gw, gh, GridBagConstraints.NONE, 0, 0);
    }

    /**
     * convenience method for adding a component to a container
     * layed out by a GridBagLayout
     *
     * @param container  the container to add a component to
     * @param comp  the component to add to container
     * @param g  the GridBagLayout associated with container
     * @param c  the GridBagConstraints to use
     * @param gx  the value to use for GridBagConstraints.gridx
     * @param gy  the value to use for GridBagConstraints.gridy
     * @param a  the value to use for GridBagConstraints.anchor
     * @param gw  the value to use for GridBagConstraints.gridwidth
     * @param gh  teh value to use for GridBagConstraints.gridheight
     * @param f  the value to use for GridBagConstraints.fill
     * @param wx  the value to use for GridBagConstraints.weightx
     * @param wy  the value to use for GridBagConstraints.weighty
     */
    public static void addGridBagComponent(final JComponent container, final JComponent comp, final GridBagLayout g,
                                           final GridBagConstraints c, final int gx, final int gy, final int a,
                                           final int gw, final int gh, final int f, final double wx, final double wy) {
        c.gridx = gx;
        c.gridy = gy;
        c.anchor = a;
        c.insets = new Insets(2, 2, 2, 2);
        c.ipadx = 2;
        c.ipady = 2;
        c.gridwidth = gw;
        c.gridheight = gh;
        c.fill = f;
        c.weightx = wx;
        c.weighty = wy;
        g.setConstraints(comp, c);
        container.add(comp);
    }


    /**
     * resolve a relative URL string against an absolute URL string.
     *
     * <p>the absolute URL string is the start point for the
     * relative path.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>
     *   absolute path:  file:/d:/eigene dateien/eigene bilder/
     *   relative path:  ../images/test.jpg
     *   result:         file:/d:/eigene dateien/images/test.jpg
     * </pre>
     * @param base  the absolute URL string to start at
     * @param relPath  the relative URL string to resolve
     *
     * @return the absolute URL string resulting from resolving relPath
     *    against absPath
     */
    public static URL resolveRelativePath(final URL base, final String relPath) {
        try {
            return new URL(base, relPath);
        } catch (MalformedURLException e) {
           throw new UncheckedIOException(e);
        }
    }
    
    /**
     * get the path to a given file relative to a given directory
     *
     * @param fromFile  the directory having the file from which the link refers
     * @param toFile  the file to which a link refers
     *
     * @return the relative path
     */
    public static String getRelativePath(final File fromFile, final File toFile) {
        if(! fromFile.isDirectory())
            return getRelativePath(fromFile.getParentFile(), toFile);
        String fromStr = fromFile.getAbsolutePath();
        if (!fromStr.endsWith(File.separator)) {
            fromStr = fromStr + File.separator;
        }
        final String toStr = toFile.getAbsolutePath();
        int pos = fromStr.indexOf(File.separator);
        final int fromLen = fromStr.length();
        final int toLen = toStr.length();
        int oldPos = pos;
        while ((pos > -1) && (pos < fromLen) && (pos < toLen)
                && (fromStr.substring(0, pos).equalsIgnoreCase(toStr.substring(0, pos)))) {
            oldPos = pos + 1;
            pos = fromStr.indexOf(File.separator, oldPos);
        }
        final int samePos = oldPos;
        int level = 0;
        while (pos > -1) {
            ++level;
            oldPos = pos + 1;
            pos = fromStr.indexOf(File.separator, oldPos);
        }
        final StringBuffer relPath = new StringBuffer();
        if (level > 0) {
            for (int i = 0; i < level; i++) {
                relPath.append("..");
                relPath.append(File.separator);
            }
        }
        relPath.append(toStr.substring(samePos));
        return relPath.toString().replace(File.separatorChar, URL_SEPARATOR_CHAR);
    }

    /**
     * show an error message and print a stack trace to the console if
     * in development mode (DEV_MODE = true)
     *
     * @param owner the owner of the message, or null
     * @param msg the message to display, or null
     * @param e the exception object describing the error, or null
     */
    public static void errMsg(final Component owner, final String msg, final Throwable e) {
        if (e != null) {
            e.printStackTrace();
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(owner, msg, ERR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * center a <code>Component</code> relative to
     * another <code>Component</code>.
     *
     * @param parent  the <code>Component</code> to be used as the
     *                  basis for centering
     * @param comp  the <code>Component</code> to be centered within parent
     *
     */
    public static void center(final Component parent, final Component comp) {
        final Dimension cSize = comp.getPreferredSize();
        final Dimension fSize = parent.getSize();
        final Point loc = parent.getLocation();
        comp.setLocation((fSize.width - cSize.width) / 2 + loc.x, (fSize.height - cSize.height) / 2 + loc.y);
    }

    /**
     * get a StyleSheet object for using its utility methods
     */
    public static StyleSheet styleSheet() {
        return s;
    }

    /**
     * remove all occurrences of a given char from a given string
     *
     * @param src the string to remove from
     * @param c  the char to remove
     *
     * @return a string copy of src with all occurrences of c removed
     */
    public static String removeChar(final String src, final char c) {
        final StringBuffer buf = new StringBuffer();
        int start = 0;
        int pos = src.indexOf(c);
        while ((pos > -1) && (start < src.length())) {
            pos = src.indexOf(c, start);
            if ((pos > -1) && (start < src.length())) {
                buf.append(src.substring(start, pos));
                start = pos + 1;
            }
        }
        if (start < src.length()) {
            buf.append(src.substring(start));
        }
        if (buf.length() == 0) {
            buf.append(src);
        }
        return buf.toString();
    }

    /**
     * get a string from the resources file
     *
     * @param resources  the TextResources to get the string from
     * @param nm  the key of the string
     * @return the string for the given key or null if not found
     */
    static public String getResourceString(final UIResources resources, final String nm) {
        return DynamicResource.getResourceString(resources, nm);
    }

    static public String getResourceString(final String nm) {
        final String resourceString = DynamicResource.getResourceString(SHTMLPanelImpl.getUiResources(), nm);
        return resourceString != null ? resourceString : nm;
    }

    static public <T extends Enum<T>> T getPreference(final String key, final T defaultValue) {
    	@SuppressWarnings("unchecked")
		final Class<T> valueClass = (Class<T>) defaultValue.getClass();
		return Enum.valueOf(valueClass, Util.getPreference(key, defaultValue.name()));
    }
    
    static public String getPreference(final String key, final String defaultValue) {
        String paramValue = DynamicResource.getResourceString(SHTMLPanel.getResources(), key);
        if (paramValue != null) {
            return paramValue;
        }
        paramValue = defaultValue;
        try {
            final Preferences prefs = Preferences.userNodeForPackage(PrefsDialog.class);
            paramValue = prefs.get(key, paramValue);
        }
        catch (final Exception ex) {
        }
        return paramValue;
    }

    static boolean preferenceIsTrue(final String key) {
        return Util.getPreference(key, "false").equalsIgnoreCase("true");
    }

    static boolean preferenceIsTrue(final String key, final String defaultValue) {
        return Util.getPreference(key, defaultValue).equalsIgnoreCase("true");
    }

    static boolean useSteStyleSheet() {
        return Util.getPreference(PrefsDialog.PREFS_USE_STD_STYLE_SHEET, "false").equalsIgnoreCase("true");
    }

    /** Tells whether the user should be able to switch between the WYSIWYG editor pane and the HTML source
     *  editor pane using the UI element of tab, shown below the editing panes. If false, switching
     *  between editor panes is still possible, albeit not using the tabs. */
    static boolean showViewsInTabs() {
        return Util.preferenceIsTrue("showViewsInTabs", "true");
    }
}
