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

import java.awt.Cursor;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.MinimalHTMLWriter;
import javax.swing.text.html.StyleSheet;


/**
 * Extensions to <code>HTMLEditorKit</code> for application SimplyHTML.
 *
 * <p>In stage 1 this only re-implements how style sheets are handled by
 * default.</p>
 *
 * <p>Stage 3 adds functionality for usage of the custom HTML document
 * and HTML reader used with SimplyHTML from this stage on.</p>
 *
 * <p>With stage 9 some additional views have been added to
 * the view factory as a workaround for bug id 4765271
 * (see http://developer.java.sun.com/developer/bugParade/bugs/4765271.html).</p>
 *
 * <p>OK, I give up: With release 2 of stage 9 above views are used no longer and
 * bug fixing is not done anymore. The HTML support is almost taken as is in the hope
 * that Sun will enhance it some day. To do compensation inside a single application
 * is not possible with a reasonable effort.</p>
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
public class SHTMLEditorKit extends HTMLEditorKit {
 
	SHTMLEditorKit() {
        super();
        final Cursor textCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
        setDefaultCursor(textCursor);
    }

    /* --------------- SHTMLDocument implementation start ------------ */
    /**
     * Create an uninitialized text storage model
     * that is appropriate for this type of editor.
     *
     * @return the model
     */
    public Document createDefaultDocument() {
        final SHTMLDocument doc = (SHTMLDocument) createEmptyDocument();
        try {
            final String standardContent;
            if (Util.preferenceIsTrue("gray_row_below_end")) {
                standardContent = "<p>\n</p>\n<p style=\"background-color: #808080\">\n" + SHTMLDocument.SUFFIX
                        + "\n</p>\n";
            }
            else {
                standardContent = "<p>\n</p>\n<p>\n" + SHTMLDocument.SUFFIX + "\n</p>\n";
            }
            doc.setOuterHTML(doc.getParagraphElement(doc.getLength()), standardContent);
        }
        catch (final BadLocationException e) {
            e.printStackTrace();
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    Document createEmptyDocument() {
        getStyleSheet();
        final StyleSheet ss = new ScaledStyleSheet();
        try {
            ss.importStyleSheet(Class.forName("javax.swing.text.html.HTMLEditorKit").getResource(DEFAULT_CSS));
        }
        catch (final Exception e) {
        }
        final SHTMLDocument doc = new SHTMLDocument(ss);
        doc.setParser(getParser());
        doc.setAsynchronousLoadPriority(-1);
        doc.setTokenThreshold(1);
        return doc;
    }

	/**
     * Inserts content from the given stream. If <code>doc</code> is
     * an instance of HTMLDocument, this will read
     * HTML 3.2 text. Inserting HTML into a non-empty document must be inside
     * the body Element, if you do not insert into the body an exception will
     * be thrown. When inserting into a non-empty document all tags outside
     * of the body (head, title) will be dropped.
     *
     * @param in  the stream to read from
     * @param doc the destination for the insertion
     * @param pos the location in the document to place the
     *   content
     * @exception IOException on any I/O error
     * @exception BadLocationException if pos represents an invalid
     *   location within the document
     * @exception RuntimeException (will eventually be a BadLocationException)
     *            if pos is invalid
     */
    public void read(final Reader in, final Document doc, final int pos) throws IOException, BadLocationException {
        if (doc instanceof SHTMLDocument) {
            final SHTMLDocument hdoc = (SHTMLDocument) doc;
            final Parser parser = getParser();
            if (parser == null) {
                throw new IOException("Can't load parser");
            }
            if (pos > doc.getLength()) {
                throw new BadLocationException("Invalid location", pos);
            }
            final ParserCallback receiver = hdoc.getReader(pos);
            if (doc.getLength() == 0) {
                final Boolean ignoreCharset = (Boolean) doc.getProperty("IgnoreCharsetDirective");
                parser.parse(in, receiver, (ignoreCharset == null) ? false : ignoreCharset.booleanValue());
            }
            else {
                parser.parse(in, receiver, true);
            }
            receiver.flush();
        }
        else {
            super.read(in, doc, pos);
        }
    }

    /**
     * Write content from a document to the given stream
     * in a format appropriate for this kind of content handler.
     *
     * @param out  the stream to write to
     * @param doc  the source for the write
     * @param pos  the location in the document to fetch the
     *   content
     * @param len  the amount to write out
     * @exception IOException on any I/O error
     * @exception BadLocationException if pos represents an invalid
     *   location within the document
     */
    public void write(final Writer out, final Document doc, final int pos, final int len) throws IOException,
            BadLocationException {
        if (doc instanceof SHTMLDocument) {
            try {
                final SHTMLWriter w = new SHTMLWriter(out, (SHTMLDocument) doc, pos, len);
                w.write();
            }
            catch (final Exception e) {
                e.printStackTrace();
            }
        }
        else if (doc instanceof StyledDocument) {
            final MinimalHTMLWriter w = new MinimalHTMLWriter(out, (StyledDocument) doc, pos, len);
            w.write();
        }
        else {
            super.write(out, doc, pos, len);
        }
    }

    /* --------------- SHTMLDocument implementaion end --------------- */
    void updateInputAttributes(final SHTMLEditorPane e) {
        // EditorKit might not have installed the StyledDocument yet.
        final Document aDoc = e.getDocument();
        if (!(aDoc instanceof StyledDocument)) {
            return;
        }
        final int start = e.getSelectionStart();
        // record current character attributes.
        final StyledDocument doc = (StyledDocument) aDoc;
        // If nothing is selected, get the attributes from the character
        // before the start of the selection, otherwise get the attributes
        // from the character element at the start of the selection.
        Element run;
        final Element currentParagraph = doc.getParagraphElement(start);
        if (currentParagraph.getStartOffset() == start || start != e.getSelectionEnd()) {
            // Get the attributes from the character at the selection
            // if in a different paragrah!
            run = doc.getCharacterElement(start);
        }
        else {
            run = doc.getCharacterElement(Math.max(start - 1, 0));
        }
        createInputAttributes(run, getInputAttributes());
    }

    /* --------------- ViewFactory implementation start -------------- */
    /** Shared factory for creating HTML Views. */
    private static final ViewFactory defaultFactory = new SHTMLFactory();

    /**
     * Fetch a factory that is suitable for producing
     * views of any models that are produced by this
     * kit.
     *
     * @return the factory
     */
    public ViewFactory getViewFactory() {
        return defaultFactory;
    }

    static public void removeCharacterAttributes(final StyledDocument doc, final Object attributeName, final int start,
                                                 final int length) {
        // clear all character attributes in selection
        final int end = start + length;
        SimpleAttributeSet sasText = null;
        for (int i = start; i < end;) {
            final Element characterElement = doc.getCharacterElement(i);
            sasText = new SimpleAttributeSet(characterElement.getAttributes().copyAttributes());
            final int endOffset = characterElement.getEndOffset();
            final ArrayList<?> attributeNames = Collections.list(sasText.getAttributeNames());
            for (final Object entryKey : attributeNames) {
                if (attributeName != null && entryKey.equals(attributeName) || attributeName == null
                        && !entryKey.equals(StyleConstants.NameAttribute)) {
                    sasText.removeAttribute(entryKey);
                }
            }
            final int last = end < endOffset ? end : endOffset;
            try {
                doc.setCharacterAttributes(i, last - i, sasText, true);
            }
            catch (final Exception e) {
            }
            i = i < last ? last : i + 1;
        }
    }

    public static class SHTMLFactory extends HTMLEditorKit.HTMLFactory implements ViewFactory {
        public View create(final Element elem) {
            View view = null;
            final Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
            if (o instanceof HTML.Tag) {
                final HTML.Tag kind = (HTML.Tag) o;
                //System.out.println("SHTMLEditorKit.SHTMLFactory o is HTML.Tag kind=" + kind.toString());
                if (kind == HTML.Tag.TABLE) {
                    view = super.create(elem);
                }
                else if (kind == HTML.Tag.COMMENT) {
                    view = new InvisibleView(elem);
                }
                else if (kind instanceof HTML.UnknownTag) {
                    view = new InvisibleView(elem);
                }
                else {
                    view = super.create(elem);
                }
            }
            else {
                view = new LabelView(elem);
            }
            return view;
        }
    }
    /* --------------- ViewFactory implementation end -------------- */
}
