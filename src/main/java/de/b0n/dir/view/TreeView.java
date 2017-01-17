package de.b0n.dir.view;

import de.b0n.dir.TreePopupMenu;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Iterator;
import java.util.Queue;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class TreeView extends JPanel implements MouseListener, AbstractView {

    @Override
    public ViewUpdater createViewUpdater(final Queue<Queue<File>> duplicates) {
        return new Updater(duplicates);
    }


    public class Updater extends ViewUpdater {


        public Updater(final Queue<Queue<File>> duplicates) {
            super(duplicates);
        }

        @Override
        public void doUpdate() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(400L);
                } catch (InterruptedException e) {
                    while (duplicates.peek() != null) {
                        SwingUtilities.invokeLater(new Inserter(duplicates.poll()));
                    }
                    return;
                }

                while (duplicates.peek() != null) {
                    SwingUtilities.invokeLater(new Inserter(duplicates.poll()));
                }
            }
        }
    }

    private class Inserter extends ViewInserter {

        public Inserter(final Queue<File> files) {
            super(files);
        }

        public void doInsert() {
            if (files == null) {
                return;
            }

            DefaultMutableTreeNode master = null;

            Iterator<File> iterator = files.iterator();

            if (iterator.hasNext()) {
                File file = iterator.next();
                master = new DuplicateTreeNode(file.getAbsolutePath(), file.length() / 1024);
                treeModel.insertNodeInto(master, top, top.getChildCount());
            }

            while (iterator.hasNext()) {
                treeModel.insertNodeInto(new DuplicateTreeNode(iterator.next().getAbsolutePath()), master,
                        master.getChildCount());
            }
            tree.scrollPathToVisible(new TreePath(master.getPath()));
        }
    }


    // Create the node
    private DefaultMutableTreeNode top = new DefaultMutableTreeNode("Dubletten");
    private DefaultTreeModel treeModel = new DefaultTreeModel(top);
    private JTree tree = new JTree(treeModel);

    public TreeView() {
        super(new GridLayout(1, 0));

        // Create a tree that allows one selection at a time.

        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addMouseListener(this);

        // Create the scroll pane and add the tree to it.
        JScrollPane treeView = new JScrollPane(tree);
        treeView.setMinimumSize(new Dimension(100, 50));
        treeView.setPreferredSize(new Dimension(500, 300));

        // Add the scroll pane to this panel.
        add(treeView);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (SwingUtilities.isRightMouseButton(e)) {

            int row = tree.getClosestRowForLocation(e.getX(), e.getY());
            TreePath path = tree.getPathForRow(row);
            if (path.getLastPathComponent() instanceof DuplicateTreeNode) {
                DuplicateTreeNode node = (DuplicateTreeNode) path.getLastPathComponent();
                TreePopupMenu menu = new TreePopupMenu(node, tree);
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }
}
