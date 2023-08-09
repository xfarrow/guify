package views;

import javax.swing.JFrame;
import controllers.QueueController;
import views.interfaces.IDesktopFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpException;
import code.Constants;
import code.Constants.GuifyColors;
import code.TreeNode;
import code.Helper;
import code.IDirectoryNodeButton;
import code.JDirectoryNodeButton;
import code.WrapLayout;
import controllers.DesktopController;
import javax.swing.JTree;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Vector;
import javax.swing.JScrollPane;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import java.awt.*;
import javax.swing.JToolBar;

public class Desktop extends JFrame implements IDesktopFrame {

	/*
	 * ========== BEGIN Attributes ==========
	 */

	private static final long serialVersionUID = 1L;
	private DesktopController controller;
	private JTree tree;
	private JScrollPane treePanel;
	private JPanel desktopPanel;
	private JToolBar toolBar;
	private JButton cutBtn;
	private JButton copyBtn;
	private JButton pasteBtn;
	private JButton renameBtn;
	private JButton deleteBtn;
	private JButton downloadBtn;
	private JTextField pathTextBox;

	/*
	 * ========== END Attributes ==========
	 */

	/*
	 * ========== BEGIN Constructors ==========
	 */

	public Desktop(Object controller) {
		this.controller = (DesktopController) controller;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle(this.controller.getTitle());
		getContentPane().setLayout(new BorderLayout());

		treePanel = new JScrollPane();
		// You can adjust the preferred size as needed
		treePanel.setPreferredSize(new Dimension(150, 634));
		getContentPane().add(treePanel, BorderLayout.WEST);

		desktopPanel = new JPanel();
		desktopPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 5, 5));
		desktopPanel.setBackground(Color.WHITE);
		desktopPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				unselectAllNodes();
				desktopPanel.requestFocus();
			}
		});

		JScrollPane scrollPane = new JScrollPane(desktopPanel);
		scrollPane.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(25);
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setBackground(GuifyColors.GRAY);
		createJToolBar();
		getContentPane().add(toolBar, BorderLayout.NORTH);
		setSize(1280, 720);
		setLocationRelativeTo(null);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				((DesktopController) controller).disposeResources();
			}
		});

		// Create drag and drop handler
		new DropTarget(desktopPanel, new DropTargetListener() {

			@Override
			public void dragEnter(DropTargetDragEvent dtde) {
			}

			@Override
			public void dragOver(DropTargetDragEvent dtde) {
				desktopPanel.setBorder(new LineBorder(GuifyColors.BLUE, 2));
			}

			@Override
			public void dropActionChanged(DropTargetDragEvent dtde) {
			}

			@Override
			public void dragExit(DropTargetEvent dte) {
				desktopPanel.setBorder(null);

			}

			@Override
			public void drop(DropTargetDropEvent dtde) {
				dtde.acceptDrop(dtde.getDropAction());
				Transferable data = dtde.getTransferable();
				if (data.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					try {
						File[] droppedFileArray = ((Collection<?>) (data
								.getTransferData(
										DataFlavor.javaFileListFlavor)))
								.toArray(new File[((Collection<?>) (data
										.getTransferData(
												DataFlavor.javaFileListFlavor)))
										.size()]);

						((DesktopController) controller)
								.uploadToRemoteServer(droppedFileArray);
						drawComponentsForDirectory(
								((DesktopController) controller)
										.getCurrentWorkingDirectory());

					} catch (UnsupportedFlavorException | IOException e) {
						e.printStackTrace();
					}
				}
				dtde.dropComplete(true);
				desktopPanel.setBorder(null);
			}
		});
	}

	/*
	 * ========== END Constructors ==========
	 */

	/*
	 * ========== BEGIN Frame Drawing ==========
	 */

	/**
	 * Draws all the components which need to be drew for a specific directory
	 * 
	 * @param directory
	 */
	public void drawComponentsForDirectory(String directory) {
		unselectAllNodes();
		controller.setCurrentWorkingDirectory(directory);
		// Only loadDesktop() can tell whether we have the permission to view
		// the content of the directory
		try {
			loadDesktop();
		} catch (Exception ex) {
			if (ex.getMessage().contains("Permission denied")) {
				JOptionPane.showMessageDialog(new JFrame(), "Permission denied",
						"Permission denied", JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(new JFrame(),
						"An unknown error has occurred: " + ex.getMessage(),
						"Error", JOptionPane.ERROR_MESSAGE);
			}
			controller.setCurrentWorkingDirectory(
					controller.getLastSafeDirectory());
			return;
		}
		loadTree();
		pathTextBox.setText(controller.getCurrentWorkingDirectory());
		controller.setLastSafeDirectory(directory);
		repaint();
		revalidate();
	}

	/**
	 * Loads the desktop view
	 * 
	 * @throws SftpException
	 *             if the content of the directory cannot be displayed
	 */
	private void loadDesktop() throws SftpException {
		Image folderIcon = null;
		Image fileIcon = null;
		try {
			folderIcon = ImageIO
					.read(getClass().getClassLoader()
							.getResource("folder_icon.png"))
					.getScaledInstance(32, 32, Image.SCALE_DEFAULT);
			fileIcon = ImageIO
					.read(getClass().getClassLoader()
							.getResource("file_icon.png"))
					.getScaledInstance(32, 32, Image.SCALE_DEFAULT);
		} catch (IOException e1) {
		}

		Vector<LsEntry> elementsToDisplay = null;
		elementsToDisplay = controller.getDirectoryElements();
		desktopPanel.removeAll();
		// No content
		if (elementsToDisplay == null) {
			return;
		}
		for (LsEntry node : elementsToDisplay) {
			ImageIcon icon = new ImageIcon(
					node.getAttrs().isDir() ? folderIcon : fileIcon);
			JDirectoryNodeButton element = new JDirectoryNodeButton(node);
			JLabel iconLabel = new JLabel(icon);
			iconLabel.setVerticalTextPosition(JLabel.BOTTOM);
			iconLabel.setHorizontalTextPosition(JLabel.CENTER);
			iconLabel.setText(node.getFilename());
			element.add(iconLabel);
			element.setBackground(new Color(255, 255, 255));
			element.setToolTipText(node.getFilename());
			int buttonWidth = 75;
			int buttonHeight = element.getPreferredSize().height;
			Dimension buttonSize = new Dimension(buttonWidth, buttonHeight);
			element.setPreferredSize(buttonSize);
			element.setMaximumSize(buttonSize);
			element.setMinimumSize(buttonSize);
			element.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					desktopElementClick(e);
				}
			});
			desktopPanel.add(element);
		}
	}

	/**
	 * Specifies the action to take upon a click on the element of a desktop
	 * 
	 * @param e
	 *            A MouseEvent
	 */
	private void desktopElementClick(MouseEvent e) {
		JDirectoryNodeButton sender = (JDirectoryNodeButton) e.getSource();

		// Move into directory
		if (e.getClickCount() == 2) {
			ImageIcon questionMarkIcon = null;
			try {
				questionMarkIcon = new ImageIcon(ImageIO
						.read(getClass().getClassLoader()
								.getResource("question_mark.png"))
						.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
			} catch (IOException e1) {
			}

			// Double click on a directory
			if (sender.node.getAttrs().isDir()) {
				String newDirectory = Helper.combinePath(
						controller.getCurrentWorkingDirectory(),
						sender.node.getFilename());
				drawComponentsForDirectory(newDirectory);
			}

			// Double click on a file
			else {
				String filePath = Helper.combinePath(
						controller.getCurrentWorkingDirectory(),
						sender.node.getFilename());
				Object[] options = {"Download", "View", "Cancel"};

				int choice = JOptionPane.showOptionDialog(null,
						"What would you like to do with this file?",
						Constants.APP_NAME, JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE, questionMarkIcon, options,
						options[0]);

				switch (choice) {

					// Download
					case 0 :

						JFileChooser fileChooser = new JFileChooser();
						fileChooser.setFileSelectionMode(
								JFileChooser.DIRECTORIES_ONLY);
						int choiceFileChooser = fileChooser.showDialog(this,
								"Save here");

						if (choiceFileChooser == JFileChooser.APPROVE_OPTION) {
							controller.downloadFile(filePath,
									fileChooser.getSelectedFile().toString());
						}
						break;

					// View
					case 1 :
						controller.openNotepadForFile(filePath);
						break;

					// Cancel
					case 2 :
						return;
				}

			}
		}

		// Select a node
		else if (e.getClickCount() == 1) {

			boolean isControlDown = (e.getModifiersEx()
					& InputEvent.CTRL_DOWN_MASK) != 0;

			// If already selected, unselect
			if (sender.getSelected()) {
				if (!isControlDown) {
					unselectNode(sender);
				}
			}

			// If not selected, select
			else {
				if (!isControlDown) {
					// Unselect all the other components
					unselectAllNodes();
				}

				// Select the current component
				selectNode(sender);
			}
		}
	}

	/**
	 * Loads the file system tree seen on the left
	 */
	private void loadTree() {
		TreeNode root = this.controller.getTree();
		DefaultTreeModel model = new DefaultTreeModel(loadTreeAux(root), true);
		tree = new JTree(model);

		// Click on the tree. Open directory if directory is clicked,
		// do nothing otherwise.
		tree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					TreePath path = tree.getPathForLocation(e.getX(), e.getY());
					if (path != null) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
								.getLastPathComponent();
						if (node.getAllowsChildren()) { // is a directory
							String fullPath = getFullPathFromTreeNode(node);
							drawComponentsForDirectory(fullPath);
						} else {
							// Do nothing. Probably a "What to do?" prompt would
							// be nice
						}
					}
				}
			}
		});
		treePanel.setViewportView(tree);
	}

	/**
	 * Auxiliary function which recursively creates a tree given its root
	 * 
	 * @param node
	 *            A TreeNode element
	 * @return A DefaultMutableTreeNode element representing the root of the
	 *         created tree
	 */
	private DefaultMutableTreeNode loadTreeAux(TreeNode node) {

		if (node == null)
			return null;

		if (node.error != null)
			return null;

		DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node.name,
				node.type.equals("directory"));

		if (node.contents != null) {
			for (TreeNode child : node.contents) {
				DefaultMutableTreeNode descendants = loadTreeAux(child);
				if (descendants != null) {
					treeNode.add(descendants);
				}
			}
		}

		return treeNode;
	}

	/**
	 * Retrieves the full path in the file system of a specified tree node
	 * 
	 * @param node
	 *            A DefaultMutableTreeNode element
	 * @return Full path of the node
	 */
	private String getFullPathFromTreeNode(DefaultMutableTreeNode node) {
		javax.swing.tree.TreeNode[] path = node.getPath();
		StringBuilder fullPath = new StringBuilder();
		for (int i = 0; i < path.length; i++) {
			Object userObject = ((DefaultMutableTreeNode) path[i])
					.getUserObject();
			fullPath.append(userObject.toString());
			if (i != path.length - 1) {
				fullPath.append("/");
			}
		}
		return fullPath.toString();
	}

	/**
	 * Creates the tool bar
	 */
	private void createJToolBar() {
		JButton backBtn = new JButton();

		backBtn.setBorderPainted(false);
		backBtn.setBorder(new EmptyBorder(0, 0, 0, 0)); // Set empty border;
		backBtn.setToolTipText("Back");
		try {
			backBtn.setIcon(new ImageIcon(ImageIO
					.read(getClass().getClassLoader()
							.getResource("back_icon.png"))
					.getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
			backBtn.setBorderPainted(false);
		} catch (IOException ex) {
			backBtn.setText("Back");
		}
		backBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (controller.getCurrentWorkingDirectory().equals("/")) {
					return;
				}
				String parentPath = Helper
						.getParentPath(controller.getCurrentWorkingDirectory());
				drawComponentsForDirectory(parentPath);
			}

			// Hover on
			@Override
			public void mouseEntered(MouseEvent e) {
				if (backBtn.isEnabled())
					backBtn.setBackground(GuifyColors.GRAY_HOVER);
			}

			// Hover off
			@Override
			public void mouseExited(MouseEvent e) {
				backBtn.setBackground(GuifyColors.GRAY);
			}
		});

		cutBtn = new JButton();
		cutBtn.setBorderPainted(false);
		cutBtn.setBorder(new EmptyBorder(0, 0, 0, 0)); // Set empty border;
		cutBtn.setToolTipText("Cut");
		try {
			cutBtn.setIcon(new ImageIcon(ImageIO
					.read(getClass().getClassLoader()
							.getResource("cut_icon.png"))
					.getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
			cutBtn.setBackground(GuifyColors.GRAY);
			cutBtn.setBorderPainted(false);
			cutBtn.setEnabled(false);
		} catch (IOException ex) {
			cutBtn.setText("Cut");
		}
		cutBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (cutBtn.isEnabled()) {
					controller.cutCopyPasteController.startCuttying(
							controller.getSelectedNodes(),
							controller.getCurrentWorkingDirectory());
					pasteBtn.setEnabled(true);
				}
			}

			// Hover on
			@Override
			public void mouseEntered(MouseEvent e) {
				if (cutBtn.isEnabled())
					cutBtn.setBackground(GuifyColors.GRAY_HOVER);
			}

			// Hover off
			@Override
			public void mouseExited(MouseEvent e) {
				cutBtn.setBackground(GuifyColors.GRAY);
			}
		});

		copyBtn = new JButton();
		copyBtn.setBorderPainted(false);
		copyBtn.setBorder(new EmptyBorder(0, 0, 0, 0)); // Set empty border;
		copyBtn.setToolTipText("Copy");
		try {
			copyBtn.setIcon(new ImageIcon(ImageIO
					.read(getClass().getClassLoader()
							.getResource("copy_icon.png"))
					.getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
			copyBtn.setBackground(GuifyColors.GRAY);
			copyBtn.setBorderPainted(false);
			copyBtn.setEnabled(false);
		} catch (IOException ex) {
			copyBtn.setText("Copy");
		}
		copyBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				controller.cutCopyPasteController.startCopying(
						controller.getSelectedNodes(),
						controller.getCurrentWorkingDirectory());
				pasteBtn.setEnabled(true);
			}

			// Hover on
			@Override
			public void mouseEntered(MouseEvent e) {
				if (copyBtn.isEnabled())
					copyBtn.setBackground(GuifyColors.GRAY_HOVER);
			}

			// Hover off
			@Override
			public void mouseExited(MouseEvent e) {
				copyBtn.setBackground(GuifyColors.GRAY);
			}
		});

		pasteBtn = new JButton();
		pasteBtn.setBorderPainted(false);
		pasteBtn.setBorder(new EmptyBorder(0, 0, 0, 0)); // Set empty border;
		pasteBtn.setToolTipText("Paste");
		try {
			pasteBtn.setIcon(new ImageIcon(ImageIO
					.read(getClass().getClassLoader()
							.getResource("paste_icon.png"))
					.getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
			pasteBtn.setBorderPainted(false);
			pasteBtn.setEnabled(false);
		} catch (IOException ex) {
			pasteBtn.setText("Paste");
		}
		pasteBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				controller.cutCopyPasteController
						.paste(controller.getCurrentWorkingDirectory());
				drawComponentsForDirectory(
						controller.getCurrentWorkingDirectory());
			}

			// Hover on
			@Override
			public void mouseEntered(MouseEvent e) {
				if (pasteBtn.isEnabled())
					pasteBtn.setBackground(GuifyColors.GRAY_HOVER);
			}

			// Hover off
			@Override
			public void mouseExited(MouseEvent e) {
				pasteBtn.setBackground(GuifyColors.GRAY);
			}
		});

		renameBtn = new JButton();
		renameBtn.setBorderPainted(false);
		renameBtn.setBorder(new EmptyBorder(0, 0, 0, 0)); // Set empty border;
		renameBtn.setToolTipText("Rename");
		try {
			renameBtn.setIcon(new ImageIcon(ImageIO
					.read(getClass().getClassLoader()
							.getResource("rename_icon.png"))
					.getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
			renameBtn.setBackground(GuifyColors.GRAY);
			renameBtn.setBorderPainted(false);
			renameBtn.setEnabled(false);
		} catch (IOException ex) {
			renameBtn.setText("Rename");
		}
		renameBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				// Something's off and the rename button shouldn't have been
				// active in the first place
				if (controller.getSelectedNodes().size() != 1) {
					return;
				}
				String oldPath = Helper.combinePath(
						controller.getCurrentWorkingDirectory(),
						controller.getSelectedNodes().get(0).getNode()
								.getFilename());

				String newName = (String) JOptionPane.showInputDialog(
						Desktop.this,
						"Rename \"" + controller.getSelectedNodes().get(0)
								.getNode().getFilename() + "\"",
						"Rename", JOptionPane.PLAIN_MESSAGE, null, null, null);

				// has closed or canceled
				if (newName == null) {
					return;
				}

				String newPath = Helper.combinePath(
						controller.getCurrentWorkingDirectory(), newName);
				try {
					controller.rename(oldPath, newPath);
				} catch (SftpException e1) {
					if (e1.getMessage().contains("Permission denied")) {
						JOptionPane.showMessageDialog(new JFrame(),
								"Not enough permissions to rename this element",
								"Permission denied", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				drawComponentsForDirectory(
						controller.getCurrentWorkingDirectory()); // TODO
																	// optimize
																	// this
			}

			// Hover on
			@Override
			public void mouseEntered(MouseEvent e) {
				if (renameBtn.isEnabled())
					renameBtn.setBackground(GuifyColors.GRAY_HOVER);
			}

			// Hover off
			@Override
			public void mouseExited(MouseEvent e) {
				renameBtn.setBackground(GuifyColors.GRAY);
			}
		});

		JButton newBtn = new JButton();
		newBtn.setBorderPainted(false);
		newBtn.setBorder(new EmptyBorder(0, 0, 0, 0)); // Set empty border);
		newBtn.setToolTipText("New");
		try {
			newBtn.setIcon(new ImageIcon(ImageIO
					.read(getClass().getClassLoader()
							.getResource("plus_icon.png"))
					.getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
			newBtn.setBackground(GuifyColors.GRAY);
			newBtn.setBorderPainted(false);
		} catch (IOException ex) {
			newBtn.setText("New");
		}
		newBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPopupMenu menu = new JPopupMenu();

				JMenuItem newFileMenuItem = new JMenuItem("New file");
				try {
					newFileMenuItem.setIcon(new ImageIcon(ImageIO
							.read(getClass().getClassLoader()
									.getResource("file_icon.png"))
							.getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
				} catch (IOException e1) {

				}
				newFileMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {

						String newFileName = (String) JOptionPane
								.showInputDialog(Desktop.this, "Name:",
										"New file", JOptionPane.PLAIN_MESSAGE,
										null, null, null);

						String newFilePath = Helper.combinePath(
								controller.getCurrentWorkingDirectory(),
								newFileName);
						try {
							controller.touch(newFilePath);
						} catch (SftpException e1) {
							if (e1.getMessage().contains("Permission denied")) {
								JOptionPane.showMessageDialog(new JFrame(),
										"Not enough permissions to create a file here",
										"Permission denied",
										JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
						drawComponentsForDirectory(
								controller.getCurrentWorkingDirectory());
					}
				});

				JMenuItem newFolderMenuItem = new JMenuItem("New folder");
				try {
					newFolderMenuItem.setIcon(new ImageIcon(ImageIO
							.read(getClass().getClassLoader()
									.getResource("folder_icon.png"))
							.getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
				} catch (IOException e1) {

				}
				newFolderMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {

						String newFolderName = (String) JOptionPane
								.showInputDialog(Desktop.this, "Name:",
										"New folder", JOptionPane.PLAIN_MESSAGE,
										null, null, null);

						// User has canceled
						if (newFolderName == null) {
							return;
						}

						String newFolderPath = Helper.combinePath(
								controller.getCurrentWorkingDirectory(),
								newFolderName);
						try {
							controller.mkdir(newFolderPath);
						} catch (SftpException e1) {
							if (e1.getMessage().contains("Permission denied")) {
								JOptionPane.showMessageDialog(new JFrame(),
										"Not enough permissions to create a folder here",
										"Permission denied",
										JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
						drawComponentsForDirectory(
								controller.getCurrentWorkingDirectory()); // TODO:
																			// avoid
																			// a
																			// complete
																			// desktop
																			// reload
					}
				});

				menu.add(newFileMenuItem);
				menu.add(newFolderMenuItem);
				menu.show(newBtn, 0, newBtn.getHeight());
			}
		});
		newBtn.addMouseListener(new MouseAdapter() {
			// Hover on
			@Override
			public void mouseEntered(MouseEvent e) {
				if (newBtn.isEnabled())
					newBtn.setBackground(GuifyColors.GRAY_HOVER);
			}

			// Hover off
			@Override
			public void mouseExited(MouseEvent e) {
				newBtn.setBackground(GuifyColors.GRAY);
			}
		});

		deleteBtn = new JButton();
		deleteBtn.setBorderPainted(false);
		deleteBtn.setBorder(new EmptyBorder(0, 0, 0, 0)); // Set empty border);
		deleteBtn.setToolTipText("Delete");
		try {
			deleteBtn.setIcon(new ImageIcon(ImageIO
					.read(getClass().getClassLoader()
							.getResource("delete_icon.png"))
					.getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
			deleteBtn.setBackground(GuifyColors.GRAY);
			deleteBtn.setBorderPainted(false);
			deleteBtn.setEnabled(false);
		} catch (IOException ex) {
			deleteBtn.setText("Delete");
		}
		deleteBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ImageIcon deleteIcon = null;
				try {
					deleteIcon = new ImageIcon(ImageIO
							.read(getClass().getClassLoader()
									.getResource("delete_icon.png"))
							.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
				} catch (IOException e1) {
				}
				int choice = JOptionPane.showOptionDialog(null,
						"Do you really want to delete the selected items?",
						Constants.APP_NAME, JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, deleteIcon, null, null);

				if (choice == 0) { // yes
					try {
						controller.deleteSelectedNodes();
					} catch (SftpException e1) {
						if (e1.getMessage().contains("Permission denied")) {
							JOptionPane.showMessageDialog(new JFrame(),
									"Deletion process has encountered an item which cannot be deleted",
									"Permission denied",
									JOptionPane.ERROR_MESSAGE);
						}
					}
					drawComponentsForDirectory(
							controller.getCurrentWorkingDirectory());
				}
			}

			// Hover on
			@Override
			public void mouseEntered(MouseEvent e) {
				if (deleteBtn.isEnabled())
					deleteBtn.setBackground(GuifyColors.GRAY_HOVER);
			}

			// Hover off
			@Override
			public void mouseExited(MouseEvent e) {
				deleteBtn.setBackground(GuifyColors.GRAY);
			}
		});

		downloadBtn = new JButton();
		downloadBtn.setBorderPainted(false);
		downloadBtn.setBorder(new EmptyBorder(0, 0, 0, 0)); // Set empty
															// border);
		downloadBtn.setToolTipText("Download");
		try {
			downloadBtn.setIcon(new ImageIcon(ImageIO
					.read(getClass().getClassLoader()
							.getResource("download_icon.png"))
					.getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
			downloadBtn.setBorderPainted(false);
			downloadBtn.setEnabled(false);
		} catch (IOException ex) {
			downloadBtn.setText("Download");
		}
		downloadBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int choiceFileChooser = fileChooser.showDialog(Desktop.this,
						"Save here");
				if (choiceFileChooser == JFileChooser.APPROVE_OPTION) {
					controller.downloadSelectedNodes(
							fileChooser.getSelectedFile().toString());
				}
			}

			// Hover on
			@Override
			public void mouseEntered(MouseEvent e) {
				if (downloadBtn.isEnabled())
					downloadBtn.setBackground(GuifyColors.GRAY_HOVER);
			}

			// Hover off
			@Override
			public void mouseExited(MouseEvent e) {
				downloadBtn.setBackground(GuifyColors.GRAY);
			}
		});

		JButton uploadBtn = new JButton();
		uploadBtn.setBorderPainted(false);
		uploadBtn.setBorder(new EmptyBorder(0, 0, 0, 0)); // Set empty border);
		uploadBtn.setToolTipText("Upload here");
		try {
			uploadBtn.setIcon(new ImageIcon(ImageIO
					.read(getClass().getClassLoader()
							.getResource("upload_icon.png"))
					.getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
			uploadBtn.setBackground(GuifyColors.GRAY);
			uploadBtn.setBorderPainted(false);
			uploadBtn.setEnabled(true);
		} catch (IOException ex) {
			uploadBtn.setText("Upload here");
		}
		uploadBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(
						JFileChooser.FILES_AND_DIRECTORIES);
				fileChooser.setMultiSelectionEnabled(true);
				int choiceFileChooser = fileChooser.showDialog(Desktop.this,
						"Upload");
				if (choiceFileChooser == JFileChooser.APPROVE_OPTION
						&& fileChooser.getSelectedFiles().length > 0) {
					controller.uploadToRemoteServer(
							fileChooser.getSelectedFiles());
					drawComponentsForDirectory(
							controller.getCurrentWorkingDirectory());
				}
			}

			// Hover on
			@Override
			public void mouseEntered(MouseEvent e) {
				if (uploadBtn.isEnabled())
					uploadBtn.setBackground(GuifyColors.GRAY_HOVER);
			}

			// Hover off
			@Override
			public void mouseExited(MouseEvent e) {
				uploadBtn.setBackground(GuifyColors.GRAY);
			}
		});

		JButton queueBtn = new JButton();
		queueBtn.setBorderPainted(false);
		queueBtn.setBorder(new EmptyBorder(0, 0, 0, 0)); // Set empty border);
		queueBtn.setToolTipText("Queue");
		try {
			queueBtn.setIcon(new ImageIcon(ImageIO
					.read(getClass().getClassLoader()
							.getResource("queue_icon.png"))
					.getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
			queueBtn.setBackground(GuifyColors.GRAY);
			queueBtn.setBorderPainted(false);
			queueBtn.setEnabled(true);
		} catch (IOException ex) {
			queueBtn.setText("Queue");
		}
		queueBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				QueueController queueController = new QueueController();
				queueController.showFrame(true);
			}

			// Hover on
			@Override
			public void mouseEntered(MouseEvent e) {
				if (queueBtn.isEnabled())
					queueBtn.setBackground(GuifyColors.GRAY_HOVER);
			}

			// Hover off
			@Override
			public void mouseExited(MouseEvent e) {
				queueBtn.setBackground(GuifyColors.GRAY);
			}
		});

		pathTextBox = new JTextField();
		pathTextBox.setMaximumSize(new Dimension(200, 35));
		pathTextBox.setMinimumSize(new Dimension(50, 35));
		pathTextBox.setPreferredSize(new Dimension(200, 35));
		Font font = pathTextBox.getFont();
		Font biggerFont = font.deriveFont(font.getSize() + 4f); // Increase font
																// size by 4
		pathTextBox.setFont(biggerFont);
		pathTextBox.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				if (pathTextBox != null && !pathTextBox.getText()
						.equals(controller.getCurrentWorkingDirectory())) {
					drawComponentsForDirectory(pathTextBox.getText());
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});

		JButton goToBtn = new JButton();
		goToBtn.setBorderPainted(false);
		goToBtn.setBorder(new EmptyBorder(0, 0, 0, 0)); // Set empty border);
		goToBtn.setToolTipText("Go");
		try {
			goToBtn.setIcon(new ImageIcon(ImageIO
					.read(getClass().getClassLoader()
							.getResource("go_to_icon.png"))
					.getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
			goToBtn.setBackground(GuifyColors.GRAY);
			goToBtn.setBorderPainted(false);
			goToBtn.setEnabled(true);
		} catch (IOException ex) {
			goToBtn.setText("Go");
		}
		goToBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (pathTextBox != null && !pathTextBox.getText()
						.equals(controller.getCurrentWorkingDirectory())) {
					drawComponentsForDirectory(pathTextBox.getText());
				}
			}

			// Hover on
			@Override
			public void mouseEntered(MouseEvent e) {
				if (uploadBtn.isEnabled())
					goToBtn.setBackground(GuifyColors.GRAY_HOVER);
			}

			// Hover off
			@Override
			public void mouseExited(MouseEvent e) {
				goToBtn.setBackground(GuifyColors.GRAY);
			}
		});

		toolBar.add(backBtn);
		toolBar.add(Box.createHorizontalStrut(15));
		toolBar.add(cutBtn);
		toolBar.add(Box.createHorizontalStrut(15));
		toolBar.add(copyBtn);
		toolBar.add(Box.createHorizontalStrut(15));
		toolBar.add(pasteBtn);
		toolBar.add(Box.createHorizontalStrut(15));
		toolBar.add(renameBtn);
		toolBar.add(Box.createHorizontalStrut(15));
		toolBar.add(newBtn);
		toolBar.add(Box.createHorizontalStrut(15));
		toolBar.add(deleteBtn);
		toolBar.add(Box.createHorizontalStrut(15));
		toolBar.add(downloadBtn);
		toolBar.add(Box.createHorizontalStrut(15));
		toolBar.add(uploadBtn);
		toolBar.add(Box.createHorizontalStrut(15));
		toolBar.add(queueBtn);
		toolBar.add(Box.createHorizontalStrut(15));
		toolBar.add(pathTextBox);
		toolBar.add(Box.createHorizontalStrut(5));
		toolBar.add(goToBtn);

	}

	/**
	 * Enables or disables tool bar buttons according to these propositions:
	 * 
	 * 1. At least a selected node is selected <--> cut, copy, delete, download
	 * ENABLED 2. Only one selected node is selected <--> rename ENABLED 3.
	 * selectedToolBarOperation is not none <--> paste ENABLED
	 */
	private void updateToolBarItems() {

		int selectedNodes = controller.countSelectedNodes();
		int selectedToolBarOperation = controller.cutCopyPasteController
				.getSelectedOperation();

		if (selectedToolBarOperation != Constants.Constants_FSOperations.NONE) {
			pasteBtn.setEnabled(true);
		} else {
			pasteBtn.setEnabled(false);
		}

		if (selectedNodes == 1) {
			renameBtn.setEnabled(true);
		} else {
			renameBtn.setEnabled(false);
		}

		if (selectedNodes > 0) {
			cutBtn.setEnabled(true);
			copyBtn.setEnabled(true);
			deleteBtn.setEnabled(true);
			downloadBtn.setEnabled(true);
		} else {
			cutBtn.setEnabled(false);
			copyBtn.setEnabled(false);
			deleteBtn.setEnabled(false);
			downloadBtn.setEnabled(false);
		}
	}

	/*
	 * ========== END Frame Drawing ==========
	 */

	/*
	 * ========== BEGIN Node selection/deselection ==========
	 */

	private void selectNode(JDirectoryNodeButton node) {
		controller.addSelectedNode(node);
		node.setBackground(new Color(204, 238, 255));
		updateToolBarItems();
	}

	private void unselectAllNodes() {
		for (IDirectoryNodeButton directoryNode : controller
				.getSelectedNodes()) {
			((JDirectoryNodeButton) directoryNode)
					.setBackground(new Color(255, 255, 255));
		}
		controller.clearSelectedNodes();
		updateToolBarItems();
	}

	private void unselectNode(JDirectoryNodeButton sender) {
		controller.removeSelectedNode(sender);
		sender.setBackground(new Color(255, 255, 255));
		updateToolBarItems();
	}

	/*
	 * ========== END Node selection/deselection ==========
	 */

}
