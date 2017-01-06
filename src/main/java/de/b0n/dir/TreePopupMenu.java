package de.b0n.dir;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * JPopupMenu for deleting duplicates. Handles MouseClicks and renames file x to
 * x.dup
 * 
 * @author niklas.polke
 */
public class TreePopupMenu extends JPopupMenu implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static final String ACTION_DELETE = "Delete Duplicate";

	public static final String FILE_ENDING_2DELETE = ".dup";

	final DuplicateTreeNode node;

	final JTree tree;

	public TreePopupMenu(final DuplicateTreeNode node, final JTree tree) {
		this.node = node;
		this.tree = tree;
		JMenuItem deleteItem = new JMenuItem(ACTION_DELETE);
		deleteItem.addActionListener(this);
		add(deleteItem);
	}

	public DuplicateTreeNode getNode() {
		return this.node;
	}

	public DefaultTreeModel getTreeModel() {
		return (DefaultTreeModel) this.tree.getModel();
	}

	public JTree getTree() {
		return this.tree;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		/*
		 * Zur Sicherheit nicht gleich löschen, sondern als zu löschen durch
		 * Umbenennung markieren. Datei bekommt zusätzliche Endung ".dup"
		 */
		if (ACTION_DELETE.equals(e.getActionCommand())) {
			boolean fileRenamed = renameFile();
			if (fileRenamed) {
				removeNodeFromTree();
			}
		}
	}

	private void removeNodeFromTree() {
		if (getNode().isLeaf()) {
			DuplicateTreeNode dupParent = (DuplicateTreeNode) getNode().getParent();
			if (dupParent.getChildCount() > 1) {
				// we only delete the leaf, because there are still at least two
				// other duplicates
				getTreeModel().removeNodeFromParent(getNode());
			} else {
				// we delete the leaf and the parent, because there are no other
				// duplicates any more
				getTreeModel().removeNodeFromParent(dupParent);
			}
		} else {
			if (getNode().getChildCount() > 1) {
				// we delete the parent, create the first leaf/children as new
				// parent and add the other leaf/children as children of the new
				// parent
				// finde new parentDuplicate
				@SuppressWarnings("unchecked")
				Enumeration<DuplicateTreeNode> children = getNode().children();
				DuplicateTreeNode newDupParent = children.nextElement();
				// create list of old and new children nodes
				List<DuplicateTreeNode> newChildren = new ArrayList<DuplicateTreeNode>();
				while (children.hasMoreElements()) {
					DuplicateTreeNode newChildrenNode = children.nextElement();
					newChildren.add(newChildrenNode);
				}
				// save index of old parent duplicate node
				final int parentIndex = getTreeModel().getIndexOfChild(getNode().getParent(), getNode());
				// save parent of old parent duplicate node
				final MutableTreeNode oldParent = (MutableTreeNode) getNode().getParent();
				// remove old parent duplicate node inclusive it's children from
				// the tree model
				getTreeModel().removeNodeFromParent(getNode());
				// create new parent node
				getTreeModel().insertNodeInto(newDupParent, oldParent, parentIndex);
				// insert new children
				for (DuplicateTreeNode newChildrenNode : newChildren) {
					newDupParent.add(newChildrenNode);
				}
				// expand path
				getTree().expandPath(new TreePath(((DuplicateTreeNode) newDupParent).getPath()));
			} else {
				// we delete the parent and the single leaf, because there are
				// no other duplicates any more
				getTreeModel().removeNodeFromParent(getNode());
			}
		}

	}

	private boolean renameFile() {
		try {
			File fileToRename = new File(getNode().getFileAbsolutePath());
			fileToRename.renameTo(new File(getNode().getFileAbsolutePath() + FILE_ENDING_2DELETE));
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
