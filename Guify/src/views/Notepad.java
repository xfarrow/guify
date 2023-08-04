package views;

import java.awt.Color;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import code.GuiAbstractions.Implementations.JGenericTextArea;
import controllers.NotepadController;
import views.interfaces.INotepadFrame;

import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.JScrollPane;
public class Notepad extends JFrame implements INotepadFrame {
	
	private static final long serialVersionUID = 1L;
	private NotepadController controller;
	private boolean jtextAreaShouldListenForChanges = true;
	private JTextArea textArea;
	
	/**
	 * Create the application.
	 */
	public Notepad(Object controller) {
		this.controller = (NotepadController) controller;
		setTitle(this.controller.getFilePath());
		setBounds(100, 100, 800, 600);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 38, 764, 512);
		getContentPane().add(scrollPane);
		
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		textArea.setTabSize(4);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
		textArea.setCaretPosition(0);
		textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                // Called when text is inserted into the document
            	handleTextChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                // Called when text is removed from the document
            	handleTextChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Called when attributes of the document change
            	// NO OP
            }

            private void handleTextChange() {
            	if(!((NotepadController)controller).isUnsaved() && jtextAreaShouldListenForChanges) {
            		((NotepadController)controller).setUnsaved(true);
            		setTitle(((NotepadController)controller).getTitle());
            	}
            }
        });
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setBounds(10, 0, 614, 37);
		toolBar.setBackground(new Color(240, 240, 240));
		
		JButton saveBtn = new JButton();
		saveBtn.setBorderPainted(false);
		saveBtn.setBorder(new EmptyBorder(0, 0, 0, 0)); // Set empty border
		saveBtn.setToolTipText("Save");
		try {
			saveBtn.setIcon(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResource("save_icon.png")).getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
			saveBtn.setBackground(new Color(240, 240, 240));
			saveBtn.setBorderPainted(false);
			saveBtn.setEnabled(true);
		}catch(IOException ex) {
			saveBtn.setText("Save");
		}
		saveBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				((NotepadController)controller).writeOnFile(textArea.getText());
				((NotepadController)controller).setUnsaved(false);
				setTitle(((NotepadController)controller).getTitle());
			}
			
			// Hover on
			@Override
            public void mouseEntered(MouseEvent e) {
				if(saveBtn.isEnabled())
					saveBtn.setBackground(new Color(220, 220, 220));
            }

			// Hover off
			@Override
            public void mouseExited(MouseEvent e) {
				saveBtn.setBackground(new Color(240, 240, 240));
            }
		});
		
		JButton searchBtn = new JButton();
		searchBtn.setBorderPainted(false);
		searchBtn.setBorder(new EmptyBorder(0, 0, 0, 0)); // Set empty border;
		searchBtn.setToolTipText("Serch/Replace");
		try {
			searchBtn.setIcon(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResource("search_icon.png")).getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
			searchBtn.setBackground(new Color(240, 240, 240));
			searchBtn.setBorderPainted(false);
			searchBtn.setEnabled(true);
		}catch(IOException ex) {
			searchBtn.setText("Serch/Replace");
		}
		searchBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				((NotepadController)controller).showFindAndReplace(new JGenericTextArea(textArea));
			}
			
			// Hover on
			@Override
            public void mouseEntered(MouseEvent e) {
				if(searchBtn.isEnabled())
					searchBtn.setBackground(new Color(220, 220, 220));
            }

			// Hover off
			@Override
            public void mouseExited(MouseEvent e) {
				searchBtn.setBackground(new Color(240, 240, 240));
            }
		});
		
		toolBar.add(saveBtn);
		toolBar.add(Box.createHorizontalStrut(15));
		toolBar.add(searchBtn);
		
		getContentPane().add(toolBar);

		/**
		 * Close "Find and Replace" if this window
		 * gets closed
		 */
		addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosing(WindowEvent e) {
	        	((NotepadController)controller).disposeFindAndReplaceFrame();
	        }
	    });	
	}
	
	/**
	 * Sets text in the JTextArea without triggering
	 * a text changed action
	 */
	public void displayContent(String content) {
		jtextAreaShouldListenForChanges = false;
		textArea.setText(content);
		jtextAreaShouldListenForChanges = true;
	}
	
}
