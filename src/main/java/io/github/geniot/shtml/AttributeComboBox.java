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

import javax.swing.JComboBox;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;

/**
 * ComboBox to show and manipulate an attribute out
 * of a given set of attribute values.
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
class AttributeComboBox extends JComboBox implements AttributeComponent {
    /** CSS attribute key associated with this component */
    private final Object attributeKey;
    /** HTML attribute key associated with this component */
    private final Object htmlAttributeKey;
    /** attribute names associated with the items of this component */
    private final String[] names;
    /** indicates wether or not a call to setValue is the initial one */
    private int setValCount = 0;
    /** stores the initial value for tracking changes */
    private int originalIndex = -2;

    /**
     * construct an <code>AttributeComboBox</code>
     *
     * @param items  the items to appear in the list of this component
     * @param names  the attributes to associate with items
     *        (in the same order)
     * @param key  the CSS attribute key this component represents
     * @param htmlKey  the HTL attribute key this component represents
     *
     * @see getValue
     */
    public AttributeComboBox(final String[] items, final String[] names, final Object key, final Object htmlKey) {
        super(items);
        this.names = names;
        attributeKey = key;
        htmlAttributeKey = htmlKey;
    }

    /**
     * set the value of this <code>AttributeComponent</code>
     *
     * @param a  the set of attributes possibly having an
     *          attribute this component can display
     *
     * @return true, if the set of attributes had a matching attribute,
     *            false if not
     */
    public boolean setValue(final AttributeSet a) {
        //System.out.println("AttributeComboBox setValue");
        //de.calcom.cclib.html.HTMLDiag hd = new de.calcom.cclib.html.HTMLDiag();
        //hd.listAttributes(a, 2);
        boolean success = false;
        Object valObj;
        if (attributeKey != null) {
            valObj = a.getAttribute(attributeKey);
            if (valObj == null && htmlAttributeKey != null) {
                valObj = a.getAttribute(htmlAttributeKey);
                if (valObj != null) {
                	success = setValue(valObj);
                }
            }
            /*
             correction start: missing list-style-type attribute from style sheet
             */
            else if (valObj == null && attributeKey.equals(CSS.Attribute.LIST_STYLE_TYPE)) {
                final Object name = a.getAttribute(StyleConstants.NameAttribute);
                if (name != null && name.toString().equalsIgnoreCase(HTML.Tag.UL.toString())) {
                	success = setValue("disc");
                }
                else if (name != null && name.toString().equalsIgnoreCase(HTML.Tag.OL.toString())) {
                	success = setValue("decimal");
                }
            }
            if (valObj == null && htmlAttributeKey != null) {
                if (htmlAttributeKey.equals(HTML.Attribute.ALIGN) || htmlAttributeKey.equals(HTML.Attribute.VALIGN)) {
                	success = setValue(names[0]);
                }
            }
            /*
             correction end: missing list-style-type attribute from style sheet
             */
            else {
                //System.out.println("AttributeComboBox setValue value=" + valObj);
            	success = setValue(valObj);
            }
        }
        else {
            if (htmlAttributeKey != null) {
                valObj = a.getAttribute(htmlAttributeKey);
                if (valObj != null) {
                	success = setValue(valObj);
                }
            }
        }
        return success;
    }

    public void reset() {
        setValCount = 0;
        originalIndex = -2;
    }

    private boolean setValue(final Object valObj) {
        if (valObj != null) {
            final String valStr = valObj.toString();
            int i = 0;
            while (!valStr.equalsIgnoreCase(names[i])) {
                i++;
                if(i >= names.length)
                	return false;
            }
            setSelectedIndex(i);
            if (++setValCount < 2) {
                originalIndex = i;
            }
            return true;
        }
        return false;
    }

    /**
     * get the value of this <code>AttributeComponent</code>
     *
     * <p>If one an attribute key is not set, the value will not
     * be returned for that attribute key. If both attribute keys are
     * set, two attributes with identical value are returned. Up
     * to J2SE 1.4, the Java language does not render
     * CSS.Attribute.VERTICAL_ALIGN but it does render HTML.Attribute.VALIGN.
     * Some browsers handle it vice versa, so it is useful to
     * store both attributes.</p>
     *
     * @return the value selected from this component
     */
    public AttributeSet getValue() {
        final SimpleAttributeSet a = new SimpleAttributeSet();
        final int value = getSelectedIndex();
        //System.out.println("AttributeComboBox getValue originalIndex=" + originalIndex + " value=" + value);
        if (originalIndex != value) {
            //System.out.println("changed " + attributeKey + " originalIndex=" + originalIndex + " value=" + value);
            if (attributeKey != null) {
                Util.styleSheet().addCSSAttribute(a, (CSS.Attribute) attributeKey, names[value]);
                //a.addAttribute(attributeKey, names[value]);
            }
            if (htmlAttributeKey != null) {
                a.addAttribute(htmlAttributeKey, names[value]);
            }
        }
        return a;
    }

    public AttributeSet getValue(final boolean includeUnchanged) {
        if (includeUnchanged) {
            final SimpleAttributeSet a = new SimpleAttributeSet();
            final int value = getSelectedIndex();
            if (attributeKey != null) {
                Util.styleSheet().addCSSAttribute(a, (CSS.Attribute) attributeKey, names[value]);
            }
            if (htmlAttributeKey != null) {
                a.addAttribute(htmlAttributeKey, names[value]);
            }
            return a;
        }
        else {
            return getValue();
        }
    }
}
