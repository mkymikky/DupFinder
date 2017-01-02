package de.b0n.dir;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import java.io.File;
import java.util.Iterator;
import java.util.Queue;
import java.awt.Dimension;
import java.awt.GridLayout;
 
public class TreeView extends JPanel {
	private static final long serialVersionUID = -4525932945955514394L;

	public class Updater implements Runnable {

		private final Queue<Queue<File>> duplicates;

		public Updater(final Queue<Queue<File>> duplicates) {
			this.duplicates = duplicates;
		}

		@Override
		public void run() {
			while(!Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(400L);
				} catch (InterruptedException e) {
					while (duplicates.peek() != null) {
						javax.swing.SwingUtilities.invokeLater(new Inserter(duplicates.poll()));
					}
					return;
				}
				
				while (duplicates.peek() != null) {
					javax.swing.SwingUtilities.invokeLater(new Inserter(duplicates.poll()));
				}
			}
		}
	}
	
	private class Inserter implements Runnable {
		private final Queue<File> files;

		public Inserter(final Queue<File> files) {
			this.files = files;
		}

		public void run() {
			if (files == null) {
				return;
			}

			DefaultMutableTreeNode master = null;
	
			Iterator<File> iterator = files.iterator();
			
			if (iterator.hasNext()) {
				File file = iterator.next();
				master = new DefaultMutableTreeNode(file.getAbsolutePath() + " Größe: " + (file.length()/1024) + "kB");
				treeModel.insertNodeInto(master, top, top.getChildCount());
			}
	
			while (iterator.hasNext()) {
				treeModel.insertNodeInto(new DefaultMutableTreeNode(iterator.next().getAbsolutePath()), master, master.getChildCount());
			}
			tree.scrollPathToVisible(new TreePath(master.getPath()));
		}
	}

	//Create the node
	private DefaultMutableTreeNode top = new DefaultMutableTreeNode("Dubletten");
	private DefaultTreeModel treeModel = new DefaultTreeModel(top);
	private JTree tree = new JTree(treeModel);
	
	public TreeView() {
		super(new GridLayout(1,0));
 
		//Create a tree that allows one selection at a time.
		
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 
		//Create the scroll pane and add the tree to it.
		JScrollPane treeView = new JScrollPane(tree);
		treeView.setMinimumSize(new Dimension(100, 50));
		treeView.setPreferredSize(new Dimension(500, 300));
 
		//Add the scroll pane to this panel.
		add(treeView);
	}
}
