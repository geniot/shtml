package io.github.geniot.shtml.bugfix;

import javax.swing.text.html.HTMLDocument;

public class MapElementRemovingWorkaround {
    public static void removeAllMapElements(HTMLDocument document) {
        document.putProperty("__MAP__", null);
    }
}
