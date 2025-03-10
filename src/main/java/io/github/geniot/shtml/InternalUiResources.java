/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Created on 23.11.2006
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

import java.net.URL;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Default implementation of TextResources based on java.util.ResourceBundle
 * 
 * @author Dimitri Polivaev
 * 14.01.2007
 */
public class InternalUiResources implements UIResources {
    private final Properties properties;
    private final ResourceBundle resources;

    public InternalUiResources(final ResourceBundle languageResources) {
        this(languageResources, null);
    }

    public InternalUiResources(final ResourceBundle languageResources, final Properties properties) {
        super();
        resources = languageResources;
        this.properties = properties;
    }

    public String getString(final String key) {
        try {
            return resources.getString(key);
        }
        catch (final MissingResourceException ex) {
            if (properties != null) {
                return properties.getProperty(key);
            }
            System.err.println("SimplyHTML : Warning : resource is missing: " + key);
            return key;
        }
    }

	@Override
	public Icon getIcon(String name) {
        final URL url = DynamicResource.getResource(this, name);
        if (url != null) {
            return new ImageIcon(url);
        }
        else
        	return null;
	}
}
