package de.b0n.dir;

import javax.swing.*;

/**
 * Created by huluvu424242 on 16.01.17.
 */
public class DupFinderGUI {

    protected TreeView treeView = new TreeView();

    public TreeView getTreeView(){
        return treeView;
    }


    public static void main(String[] args) {

        final DupFinderGUI gui = new DupFinderGUI();
        gui.showView();

    }

    public void showView(){

        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }


    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Couldn't use system look and feel.");
        }

        //Create and set up the window.
        JFrame frame = new JFrame("Duplikat-Finder");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(treeView);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
}
