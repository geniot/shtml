/*
 * @(#)ElementTreePanel.java	1.16 04/07/26 slightly modified (line 121)
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */
/*
 * @(#)ElementTreePanel.java	1.16 04/07/26
 */
/* for use in SimplyHTML */
package io.github.geniot.shtml;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Displays a tree showing all the elements in a text Document. Selecting
 * a node will result in reseting the selection of the JTextComponent.
 * This also becomes a CaretListener to know when the selection has changed
 * in the text to update the selected item in the tree.
 *
 * @author Scott Violet
 * @version 1.16 07/26/04
 */
public class ElementTreePanel extends JPanel implements CaretListener, DocumentListener, PropertyChangeListener,
        TreeSelectionListener {
    /** Tree showing the documents element structure. */
    protected JTree tree;
    /** Text component showing elemenst for. */
    protected JTextComponent editor;
    /** Model for the tree. */
    protected ElementTreeModel treeModel;
    /** Set to true when updatin the selection. */
    protected boolean updatingSelection;

    public ElementTreePanel(final JTextComponent editor) {
        this.editor = editor;
        final Document document = editor.getDocument();
        // Create the tree.
        treeModel = new ElementTreeModel(document);
        tree = new JTree(treeModel) {
            public String convertValueToText(final Object value, final boolean selected, final boolean expanded,
                                             final boolean leaf, final int row, final boolean hasFocus) {
                // Should only happen for the root
                if (!(value instanceof Element)) {
                    return value.toString();
                }
                final Element e = (Element) value;
                final AttributeSet as = e.getAttributes().copyAttributes();
                String asString;
                if (as != null) {
                    final StringBuffer retBuffer = new StringBuffer("[");
                    final Enumeration names = as.getAttributeNames();
                    while (names.hasMoreElements()) {
                        final Object nextName = names.nextElement();
                        if (nextName != StyleConstants.ResolveAttribute) {
                            retBuffer.append(" ");
                            retBuffer.append(nextName);
                            retBuffer.append("=");
                            retBuffer.append(as.getAttribute(nextName));
                        }
                    }
                    retBuffer.append(" ]");
                    asString = retBuffer.toString();
                }
                else {
                    asString = "[ ]";
                }
                if (e.isLeaf()) {
                    return e.getName() + " [" + e.getStartOffset() + ", " + e.getEndOffset() + "] Attributes: "
                            + asString;
                }
                return e.getName() + " [" + e.getStartOffset() + ", " + e.getEndOffset() + "] Attributes: " + asString;
            }
        };
        tree.addTreeSelectionListener(this);
        /* commented out for use in SimplyHTML */// tree.setDragEnabled(true);
        // Don't show the root, it is fake.
        tree.setRootVisible(false);
        // Since the display value of every node after the insertion point
        // changes every time the text changes and we don't generate a change
        // event for all those nodes the display value can become off.
        // This can be seen as '...' instead of the complete string value.
        // This is a temporary workaround, increase the needed size by 15,
        // hoping that will be enough.
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            public Dimension getPreferredSize() {
                final Dimension retValue = super.getPreferredSize();
                if (retValue != null) {
                    retValue.width += 15;
                }
                return retValue;
            }
        });
        // become a listener on the document to update the tree.
        document.addDocumentListener(this);
        // become a PropertyChangeListener to know when the Document has
        // changed.
        editor.addPropertyChangeListener(this);
        // Become a CaretListener
        editor.addCaretListener(this);
        // configure the panel and frame containing it.
        setLayout(new BorderLayout());
        add(new JScrollPane(tree), BorderLayout.CENTER);
        // Add a label above tree to describe what is being shown
        final JLabel label = new JLabel("Elements that make up the current document", SwingConstants.CENTER);
        label.setFont(new Font("Dialog", Font.BOLD, 14));
        add(label, BorderLayout.NORTH);
        setPreferredSize(new Dimension(400, 400));
    }

    /**
     * Resets the JTextComponent to <code>editor</code>. This will update
     * the tree accordingly.
     */
    public void setEditor(final JTextComponent editor) {
        if (this.editor == editor) {
            return;
        }
        if (this.editor != null) {
            final Document oldDoc = this.editor.getDocument();
            oldDoc.removeDocumentListener(this);
            this.editor.removePropertyChangeListener(this);
            this.editor.removeCaretListener(this);
        }
        this.editor = editor;
        if (editor == null) {
            treeModel = null;
            tree.setModel(null);
        }
        else {
            final Document newDoc = editor.getDocument();
            newDoc.addDocumentListener(this);
            editor.addPropertyChangeListener(this);
            editor.addCaretListener(this);
            treeModel = new ElementTreeModel(newDoc);
            tree.setModel(treeModel);
        }
    }

    // PropertyChangeListener
    /**
     * Invoked when a property changes. We are only interested in when the
     * Document changes to reset the DocumentListener.
     */
    public void propertyChange(final PropertyChangeEvent e) {
        if (e.getSource() == getEditor() && e.getPropertyName().equals("document")) {
            getEditor();
            final Document oldDoc = (Document) e.getOldValue();
            final Document newDoc = (Document) e.getNewValue();
            // Reset the DocumentListener
            oldDoc.removeDocumentListener(this);
            newDoc.addDocumentListener(this);
            // Recreate the TreeModel.
            treeModel = new ElementTreeModel(newDoc);
            tree.setModel(treeModel);
        }
    }

    // DocumentListener
    /**
     * Gives notification that there was an insert into the document.  The
     * given range bounds the freshly inserted region.
     *
     * @param e the document event
     */
    public void insertUpdate(final DocumentEvent e) {
        updateTree(e);
    }

    /**
     * Gives notification that a portion of the document has been
     * removed.  The range is given in terms of what the view last
     * saw (that is, before updating sticky positions).
     *
     * @param e the document event
     */
    public void removeUpdate(final DocumentEvent e) {
        updateTree(e);
    }

    /**
     * Gives notification that an attribute or set of attributes changed.
     *
     * @param e the document event
     */
    public void changedUpdate(final DocumentEvent e) {
        updateTree(e);
    }

    // CaretListener
    /**
     * Messaged when the selection in the editor has changed. Will update
     * the selection in the tree.
     */
    public void caretUpdate(final CaretEvent e) {
        if (!updatingSelection) {
            getEditor();
            final int selBegin = Math.min(e.getDot(), e.getMark());
            final int end = Math.max(e.getDot(), e.getMark());
            final Vector paths = new Vector();
            final TreeModel model = getTreeModel();
            final Object root = model.getRoot();
            final int rootCount = model.getChildCount(root);
            // Build an array of all the paths to all the character elements
            // in the selection.
            for (int counter = 0; counter < rootCount; counter++) {
                int start = selBegin;
                while (start <= end) {
                    final TreePath path = getPathForIndex(start, root, (Element) model.getChild(root, counter));
                    final Element charElement = (Element) path.getLastPathComponent();
                    paths.addElement(path);
                    if (start >= charElement.getEndOffset()) {
                        start++;
                    }
                    else {
                        start = charElement.getEndOffset();
                    }
                }
            }
            // If a path was found, select it (them).
            final int numPaths = paths.size();
            if (numPaths > 0) {
                final TreePath[] pathArray = new TreePath[numPaths];
                paths.copyInto(pathArray);
                updatingSelection = true;
                try {
                    getTree().setSelectionPaths(pathArray);
                    getTree().scrollPathToVisible(pathArray[0]);
                }
                finally {
                    updatingSelection = false;
                }
            }
        }
    }

    // TreeSelectionListener
    /**
      * Called whenever the value of the selection changes.
      * @param e the event that characterizes the change.
      */
    public void valueChanged(final TreeSelectionEvent e) {
        final JTree tree = getTree();
        if (!updatingSelection && tree.getSelectionCount() == 1) {
            final TreePath selPath = tree.getSelectionPath();
            final Object lastPathComponent = selPath.getLastPathComponent();
            if (!(lastPathComponent instanceof DefaultMutableTreeNode)) {
                final Element selElement = (Element) lastPathComponent;
                updatingSelection = true;
                try {
                    getEditor().select(selElement.getStartOffset(), selElement.getEndOffset());
                }
                finally {
                    updatingSelection = false;
                }
            }
        }
    }

    // Local methods
    /**
     * @return tree showing elements.
     */
    protected JTree getTree() {
        return tree;
    }

    /**
     * @return JTextComponent showing elements for.
     */
    protected JTextComponent getEditor() {
        return editor;
    }

    /**
     * @return TreeModel implementation used to represent the elements.
     */
    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    /**
     * Updates the tree based on the event type. This will invoke either
     * updateTree with the root element, or handleChange.
     */
    protected void updateTree(final DocumentEvent event) {
        updatingSelection = true;
        try {
            final TreeModel model = getTreeModel();
            final Object root = model.getRoot();
            for (int counter = model.getChildCount(root) - 1; counter >= 0; counter--) {
                updateTree(event, (Element) model.getChild(root, counter));
            }
        }
        finally {
            updatingSelection = false;
        }
    }

    /**
     * Creates TreeModelEvents based on the DocumentEvent and messages
     * the treemodel. This recursively invokes this method with children
     * elements.
     * @param event indicates what elements in the tree hierarchy have
     * changed.
     * @param element Current element to check for changes against.
     */
    protected void updateTree(final DocumentEvent event, final Element element) {
        final DocumentEvent.ElementChange ec = event.getChange(element);
        if (ec != null) {
            final Element[] removed = ec.getChildrenRemoved();
            final Element[] added = ec.getChildrenAdded();
            final int startIndex = ec.getIndex();
            // Check for removed.
            if (removed != null && removed.length > 0) {
                final int[] indices = new int[removed.length];
                for (int counter = 0; counter < removed.length; counter++) {
                    indices[counter] = startIndex + counter;
                }
                getTreeModel().nodesWereRemoved((TreeNode) element, indices, removed);
            }
            // check for added
            if (added != null && added.length > 0) {
                final int[] indices = new int[added.length];
                for (int counter = 0; counter < added.length; counter++) {
                    indices[counter] = startIndex + counter;
                }
                getTreeModel().nodesWereInserted((TreeNode) element, indices);
            }
        }
        if (!element.isLeaf()) {
            int startIndex = element.getElementIndex(event.getOffset());
            final int elementCount = element.getElementCount();
            final int endIndex = Math.min(elementCount - 1,
                element.getElementIndex(event.getOffset() + event.getLength()));
            if (startIndex > 0 && startIndex < elementCount
                    && element.getElement(startIndex).getStartOffset() == event.getOffset()) {
                // Force checking the previous element.
                startIndex--;
            }
            if (startIndex != -1 && endIndex != -1) {
                for (int counter = startIndex; counter <= endIndex; counter++) {
                    updateTree(event, element.getElement(counter));
                }
            }
        }
        else {
            // Element is a leaf, assume it changed
            getTreeModel().nodeChanged((TreeNode) element);
        }
    }

    /**
     * Returns a TreePath to the element at <code>position</code>.
     */
    protected TreePath getPathForIndex(final int position, final Object root, final Element rootElement) {
        TreePath path = new TreePath(root);
        Element child = rootElement.getElement(rootElement.getElementIndex(position));
        path = path.pathByAddingChild(rootElement);
        path = path.pathByAddingChild(child);
        while (!child.isLeaf()) {
            child = child.getElement(child.getElementIndex(position));
            path = path.pathByAddingChild(child);
        }
        return path;
    }

    /**
     * ElementTreeModel is an implementation of TreeModel to handle displaying
     * the Elements from a Document. AbstractDocument.AbstractElement is
     * the default implementation used by the swing text package to implement
     * Element, and it implements TreeNode. This makes it trivial to create
     * a DefaultTreeModel rooted at a particular Element from the Document.
     * Unfortunately each Document can have more than one root Element.
     * Implying that to display all the root elements as a child of another
     * root a fake node has be created. This class creates a fake node as
     * the root with the children being the root elements of the Document
     * (getRootElements).
     * <p>This subclasses DefaultTreeModel. The majority of the TreeModel
     * methods have been subclassed, primarily to special case the root.
     */
    public static class ElementTreeModel extends DefaultTreeModel {
        protected Element[] rootElements;

        public ElementTreeModel(final Document document) {
            super(new DefaultMutableTreeNode("root"), false);
            rootElements = document.getRootElements();
        }

        /**
         * Returns the child of <I>parent</I> at index <I>index</I> in
         * the parent's child array.  <I>parent</I> must be a node
         * previously obtained from this data source. This should
         * not return null if <i>index</i> is a valid index for
         * <i>parent</i> (that is <i>index</i> &gt;= 0 &amp;&amp; <i>index</i>
         * &lt; getChildCount(<i>parent</i>)).
         *
         * @param   parent  a node in the tree, obtained from this data source
         * @return  the child of <I>parent</I> at index <I>index</I>
         */
        public Object getChild(final Object parent, final int index) {
            if (parent == root) {
                return rootElements[index];
            }
            return super.getChild(parent, index);
        }

        /**
         * Returns the number of children of <I>parent</I>.  Returns 0
         * if the node is a leaf or if it has no children.
         * <I>parent</I> must be a node previously obtained from this
         * data source.
         *
         * @param   parent  a node in the tree, obtained from this data source
         * @return  the number of children of the node <I>parent</I>
         */
        public int getChildCount(final Object parent) {
            if (parent == root) {
                return rootElements.length;
            }
            return super.getChildCount(parent);
        }

        /**
         * Returns true if <I>node</I> is a leaf.  It is possible for
         * this method to return false even if <I>node</I> has no
         * children.  A directory in a filesystem, for example, may
         * contain no files; the node representing the directory is
         * not a leaf, but it also has no children.
         *
         * @param   node    a node in the tree, obtained from this data source
         * @return  true if <I>node</I> is a leaf
         */
        public boolean isLeaf(final Object node) {
            if (node == root) {
                return false;
            }
            return super.isLeaf(node);
        }

        /**
         * Returns the index of child in parent.
         */
        public int getIndexOfChild(final Object parent, final Object child) {
            if (parent == root) {
                for (int counter = rootElements.length - 1; counter >= 0; counter--) {
                    if (rootElements[counter] == child) {
                        return counter;
                    }
                }
                return -1;
            }
            return super.getIndexOfChild(parent, child);
        }

        /**
         * Invoke this method after you've changed how node is to be
         * represented in the tree.
         */
        public void nodeChanged(final TreeNode node) {
            if (listenerList != null && node != null) {
                TreeNode parent = node.getParent();
                if (parent == null && node != root) {
                    parent = root;
                }
                if (parent != null) {
                    final int anIndex = getIndexOfChild(parent, node);
                    if (anIndex != -1) {
                        final int[] cIndexs = new int[1];
                        cIndexs[0] = anIndex;
                        nodesChanged(parent, cIndexs);
                    }
                }
            }
        }

        /**
         * Returns the path to a particluar node. This is recursive.
         */
        protected TreeNode[] getPathToRoot(final TreeNode aNode, int depth) {
            TreeNode[] retNodes;
            /* Check for null, in case someone passed in a null node, or
               they passed in an element that isn't rooted at root. */
            if (aNode == null) {
                if (depth == 0) {
                    return null;
                }
                else {
                    retNodes = new TreeNode[depth];
                }
            }
            else {
                depth++;
                if (aNode == root) {
                    retNodes = new TreeNode[depth];
                }
                else {
                    TreeNode parent = aNode.getParent();
                    if (parent == null) {
                        parent = root;
                    }
                    retNodes = getPathToRoot(parent, depth);
                }
                retNodes[retNodes.length - depth] = aNode;
            }
            return retNodes;
        }
    }
}
