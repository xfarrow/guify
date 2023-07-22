package views;

import java.awt.event.*;
import javax.swing.*;

import controllers.FindAndReplaceController;
import views.interfaces.IFindAndReplaceFrame;

// This JFrame has been forked from the project "Simple-Java-Text-Editor" you 
// can find at its link: https://github.com/pH-7/Simple-Java-Text-Editor.
// Its license can be found here https://github.com/pH-7/Simple-Java-Text-Editor/blob/master/license.txt (Apache License 2.0)

public class FindAndReplace extends JFrame implements IFindAndReplaceFrame {

    private static final long serialVersionUID = 1L;
    
    public FindAndReplace(Object controller) {
			JLabel lab1 = new JLabel("Find:");
	        JLabel lab2 = new JLabel("Replace:");
	        JTextField findTextField = new JTextField(30);
	        JTextField replaceTextField = new JTextField(30);
	        JButton findNextBtn = new JButton("Find Next");
	        findNextBtn.addMouseListener(new MouseAdapter() {
			    @Override
			    public void mouseClicked(MouseEvent e){
			    	((FindAndReplaceController) controller).findNext(findTextField.getText());
			    }
			});
	        
	        JButton findPreviousBtn = new JButton("Find Previous");
	        findPreviousBtn.addMouseListener(new MouseAdapter() {
			    @Override
			    public void mouseClicked(MouseEvent e){
			    	((FindAndReplaceController) controller).findPrevious(findTextField.getText());
			    }
			});
	        
	        JButton replaceNextBtn = new JButton("Replace Next");
	        replaceNextBtn.addMouseListener(new MouseAdapter() {
			    @Override
			    public void mouseClicked(MouseEvent e){
			    	((FindAndReplaceController) controller).replaceNext(findTextField.getText(), replaceTextField.getText());
			    }
			});
	        
	        JButton replaceAllBtn = new JButton("Replace All");
	        replaceAllBtn.addMouseListener(new MouseAdapter() {
			    @Override
			    public void mouseClicked(MouseEvent e){
			    	((FindAndReplaceController) controller).replaceAll(findTextField.getText(), replaceTextField.getText());
			    }
			});
	        
	        JButton cancel = new JButton("Cancel");
	        cancel.addMouseListener(new MouseAdapter() {
			    @Override
			    public void mouseClicked(MouseEvent e){
			    	dispose();
			    }
			});

	        setLayout(null);

	        // Set the width and height of the label
	        int labWidth = 80;
	        int labHeight = 20;

	        // Adding labels
	        lab1.setBounds(10,10, labWidth, labHeight);
	        add(lab1);
	        findTextField.setBounds(10+labWidth, 10, 120, 20);
	        add(findTextField);
	        lab2.setBounds(10, 10+labHeight+10, labWidth, labHeight);
	        add(lab2);
	        replaceTextField.setBounds(10+labWidth, 10+labHeight+10, 120, 20);
	        add(replaceTextField);

	        // Adding buttons
	        findNextBtn.setBounds(225, 6, 115, 20);
	        add(findNextBtn);

	        findPreviousBtn.setBounds(225, 28, 115, 20);
	        add(findPreviousBtn);

	        replaceNextBtn.setBounds(225, 50, 115, 20);
	        add(replaceNextBtn);

	        replaceAllBtn.setBounds(225, 72, 115, 20);
	        add(replaceAllBtn);

	        cancel.setBounds(225, 94, 115, 20);
	        add(cancel);

	        // Set the width and height of the window
	        int width = 360;
	        int height = 160;

	        // Set size window
	        setSize(width, height);
	        setResizable(false);
	        setTitle("Find/Replace");
	        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	        setAlwaysOnTop(true);
	}
}
