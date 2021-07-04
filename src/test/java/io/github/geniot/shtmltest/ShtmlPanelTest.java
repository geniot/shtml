package io.github.geniot.shtmltest;


import io.github.geniot.shtml.SHTMLPanelSingleDocImpl;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * No unit tests here. Just an editor test to make sure shtml can be initialized the way that I want it.
 * The package is named shtmltest to make sure I can create shtml panels from outside the shtml package.
 * Changes in shtml code: making some methods and classes public.
 */
public class ShtmlPanelTest {

    public static String HTML_DOC_START = "<html><head><style>body {\n" +
            "background-color: #EEEEEE; \n" +
            "color: #696969; \n" +
            "font-size:16pt; \n" +
            "font-family:verdana; \n" +
            "margin: 15px;\n" +
            "/* http://www.w3schools.com/cssref/css_colors.asp */\n" +
            "}</style></head><body>";
    public static final String HTML_DOC_END = "</body></html>";

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.getContentPane().add(new MyTestPanel(), BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    static class MyTestPanel extends SHTMLPanelSingleDocImpl {
        public MyTestPanel() {
            super(true);
            String text = "<p>Some text</p>";
            getDocumentPane().setDocumentText(HTML_DOC_START + text + HTML_DOC_END);
            getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    onEdit();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    onEdit();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    onEdit();
                }
            });
            getEditorPane().setEditable(true);
            getDocumentPane().getEditor().getCaret().setBlinkRate(0);
        }

        void onEdit() {
            System.out.println("On edit");
        }
    }
}
