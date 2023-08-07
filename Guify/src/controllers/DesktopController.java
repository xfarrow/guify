package controllers;

import com.google.gson.Gson;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import code.Constants;
import code.Constants.Constants_FSOperations;
import code.GuiAbstractions.Implementations.JFrameFactory;
import controllers.LoginController.LoginCredentials;
import views.interfaces.IDesktopFrame;
import code.TreeNode;
import code.Helper;
import code.IDirectoryNodeButton;
import code.SshEngine;
import java.io.File;
import java.util.*;
import java.util.List;
public class DesktopController {

	/*
	 * ========== BEGIN Attributes ==========
	 */

	private IDesktopFrame frame;
	private String currentWorkingDirectory = "~";
	private String lastSafeDirectory = null;
	private List<IDirectoryNodeButton> selectedNodes = new ArrayList<IDirectoryNodeButton>();
	public CutCopyPasteController cutCopyPasteController = new CutCopyPasteController();

	/*
	 * ========== END Attributes ==========
	 */

	/*
	 * ========== BEGIN Constructors ==========
	 */

	public DesktopController() {
		try {
			frame = (IDesktopFrame) JFrameFactory
					.createJFrame(JFrameFactory.DESKTOP, this);
			frame.drawComponentsForDirectory("~");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * ========== END Constructors ==========
	 */

	/*
	 * ========== BEGIN Getters and Setters ==========
	 */

	public String getCurrentWorkingDirectory() {
		return currentWorkingDirectory;
	}

	public void setCurrentWorkingDirectory(String directory) {
		if (directory.equals("~")) {
			currentWorkingDirectory = SshEngine.executeCommand("pwd");
		} else {
			currentWorkingDirectory = directory.trim();
		}
	}

	public String getLastSafeDirectory() {
		return lastSafeDirectory;
	}

	public void setLastSafeDirectory(String directory) {

		if (directory == null) {
			lastSafeDirectory = null;
			return;
		}

		if (directory.equals("~")) {
			lastSafeDirectory = SshEngine.executeCommand("pwd");
		} else {
			lastSafeDirectory = directory.trim();
		}
	}

	/*
	 * ========== END Getters and Setters ==========
	 */

	/*
	 * ========== BEGIN Create desktop helper methods ==========
	 */

	public TreeNode getTree() {
		final int maxDepth = 3;
		StringBuilder command = new StringBuilder("tree -Ji -L ");
		command.append(maxDepth);
		command.append(' ');
		command.append(currentWorkingDirectory);
		String jsonTree = SshEngine.executeCommand(command.toString());
		TreeNode[] tree = null;

		try {
			// Might throw Invalid JSON exception because of incorrect
			// JSON output returned from tree:
			// https://gitlab.com/OldManProgrammer/unix-tree/-/issues/11
			// Fixed in tree 2.1.1
			// https://gitlab.com/OldManProgrammer/unix-tree/-/commit/84fa3ddff51b30835a0f9c4a9e4c9225970f9aff
			//
			// For this reason, we temporarily explicitly avoid it to happen
			jsonTree = jsonTree.replace("}{\"error\"", "},{\"error\"");
			tree = new Gson().fromJson(jsonTree, TreeNode[].class);
			return tree[0];
		} catch (Exception ex) {
			return null;
		}
	}

	public Vector<ChannelSftp.LsEntry> getDirectoryElements()
			throws SftpException {
		return SshEngine.ls(currentWorkingDirectory);
	}

	/*
	 * ========== END Create desktop helper methods ==========
	 */

	/*
	 * ========== BEGIN Download and Upload section ==========
	 */

	/**
	 * Downloads a file from the remote server to the local machine
	 * 
	 * @param sourcePath
	 *            Remote file's full path
	 * @param destinationPath
	 *            Local file's full path
	 */
	public void downloadFile(String sourcePath, String destinationPath) {
		SshEngine.downloadFile(sourcePath, destinationPath);
	}

	/**
	 * Uploads files and folders to the remote server
	 * 
	 * @param selectedNodes
	 * @throws SftpException
	 */
	public void uploadToRemoteServer(File[] selectedNodes)
			throws SftpException {
		if (selectedNodes.length > 0) {
			List<File> selectedFiles = new ArrayList<File>();
			List<File> selectedDirectories = new ArrayList<File>();
			for (java.io.File file : selectedNodes) {
				if (file.isFile()) {
					selectedFiles.add(file);
				} else if (file.isDirectory()) {
					selectedDirectories.add(file);
				}
			}

			for (File file : selectedFiles) {
				SshEngine.uploadFile(file, this.getCurrentWorkingDirectory());
			}

			if (selectedDirectories.size() > 0) {
				for (File directory : selectedDirectories) {
					SshEngine.uploadDirectoriesRecursively(directory,
							this.getCurrentWorkingDirectory());
				}
			}
		}
	}

	/*
	 * ========== END Download and Upload section ==========
	 */

	/*
	 * ========== BEGIN Selected Nodes section ==========
	 */

	public void addSelectedNode(IDirectoryNodeButton node) {
		selectedNodes.add(node);
		node.setSelected(true);
	}

	public void removeSelectedNode(IDirectoryNodeButton node) {
		selectedNodes.remove(node);
		node.setSelected(false);
	}

	public void clearSelectedNodes() {
		if (selectedNodes != null) {
			Iterator<IDirectoryNodeButton> iterator = selectedNodes.iterator();
			while (iterator.hasNext()) {
				IDirectoryNodeButton node = iterator.next();
				iterator.remove();
				node.setSelected(false);
			}
		}
	}

	public List<IDirectoryNodeButton> getSelectedNodes() {
		return selectedNodes;
	}

	public int countSelectedNodes() {
		return selectedNodes.size();
	}

	public void deleteSelectedNodes() throws SftpException {

		List<String> filesToDelete = new ArrayList<String>();
		List<String> directoriesToDelete = new ArrayList<String>();

		for (IDirectoryNodeButton node : selectedNodes) {
			if (node.getNode().getAttrs().isDir()) {
				directoriesToDelete.add(Helper
						.combinePath(getCurrentWorkingDirectory(),
								node.getNode().getFilename())
						.replace("\"", "\\\""));
			} else {
				filesToDelete.add(Helper
						.combinePath(getCurrentWorkingDirectory(),
								node.getNode().getFilename())
						.replace("\"", "\\\""));
			}
		}

		SshEngine.rm(filesToDelete);
		SshEngine.rmdir(directoriesToDelete);

		clearSelectedNodes();
	}

	public void downloadSelectedNodes(String destinationPath) {
		List<String> directories = new ArrayList<String>();
		List<String> files = new ArrayList<String>();
		String tmp;
		for (IDirectoryNodeButton node : selectedNodes) {
			tmp = Helper.combinePath(getCurrentWorkingDirectory(),
					node.getNode().getFilename());
			if (node.getNode().getAttrs().isDir()) {
				directories.add(tmp);
			} else {
				files.add(tmp);
			}
		}

		for (String dir : directories) {
			SshEngine.downloadDirectoryRecursively(dir, destinationPath);
		}

		for (String file : files) {
			SshEngine.downloadFile(file, destinationPath);
		}
	}

	/*
	 * ========== END Selected Nodes section ==========
	 */

	/*
	 * ========== BEGIN CutCopyPasteController controller ==========
	 */

	public class CutCopyPasteController {
		private List<String> sources = new ArrayList<String>();
		private int selectedOperation = Constants.Constants_FSOperations.NONE;

		public void startCopying(List<IDirectoryNodeButton> selectedNodes,
				String currentPath) {
			String fullPath = null;
			for (IDirectoryNodeButton nodeBtn : selectedNodes) {
				fullPath = Helper.combinePath(currentPath,
						nodeBtn.getNode().getFilename());
				sources.add(fullPath);
			}
			selectedOperation = Constants.Constants_FSOperations.COPY;
		}

		public void startCuttying(List<IDirectoryNodeButton> selectedNodes,
				String currentPath) {
			String fullPath = null;
			for (IDirectoryNodeButton nodeBtn : selectedNodes) {
				fullPath = Helper.combinePath(currentPath,
						nodeBtn.getNode().getFilename());
				sources.add(fullPath);
			}
			selectedOperation = Constants.Constants_FSOperations.CUT;
		}

		public void paste(String destination) {
			StringBuilder command = null;

			// no source
			if (sources.size() == 0) {
				return;
			}

			// cannot write on destination
			// we keep using isWriteable as
			// the executeCommand() does not fire
			// an exception in case of fail
			if (!isWriteable(destination)) {
				return;
			}

			// copy
			if (selectedOperation == Constants.Constants_FSOperations.COPY) {
				command = new StringBuilder("cp -r");

			}

			// cut
			else if (selectedOperation == Constants.Constants_FSOperations.CUT) {
				command = new StringBuilder("mv");
			}

			// invalid command
			else {
				return;
			}

			// execute
			for (String path : sources) {
				command.append(' ');
				command.append('"');
				command.append(path.replace("\"", "\\\""));
				command.append('"');
			}
			command.append(' ');
			command.append('"');
			command.append(destination.replace("\"", "\\\""));
			command.append('"');
			SshEngine.executeCommand(command.toString());
			selectedOperation = Constants_FSOperations.NONE;
		}

		public int getSelectedOperation() {
			return selectedOperation;
		}
	}

	/*
	 * ========== END CutCopyPasteController controller ==========
	 */

	/*
	 * ========== BEGIN File System Operations ==========
	 */

	/**
	 * Creates a new folder
	 * 
	 * @param newFolderPath
	 *            Folder's path
	 * @throws SftpException
	 */
	public void mkdir(String newFolderPath) throws SftpException {
		SshEngine.mkdir(newFolderPath);
	}

	/**
	 * Creates a file in the remote file path
	 * 
	 * @param remoteFilePath
	 *            remote file path
	 * @throws SftpException
	 */
	public void touch(String remoteFilePath) throws SftpException {
		SshEngine.touch(remoteFilePath);
	}

	/**
	 * Renames a file
	 * 
	 * @param oldNamePath
	 *            Path of the old name
	 * @param newNamePath
	 *            Path of the new name
	 * @throws SftpException
	 */
	public void rename(String oldNamePath, String newNamePath)
			throws SftpException {
		SshEngine.rename(oldNamePath, newNamePath);
	}

	/*
	 * ========== END File System Operations ==========
	 */

	/*
	 * ========== BEGIN Other ==========
	 */

	/**
	 * Given a remote file path, opens a graphical notepad for it
	 * 
	 * @param filePath
	 *            remote file path to display in the notepad
	 */
	public void openNotepadForFile(String filePath) {
		new NotepadController(filePath).show();
	}

	/**
	 * Disposes resources which need to be freed before exiting the application
	 */
	public void disposeResources() {
		SshEngine.disconnectSession();
	}

	/**
	 * @deprecated This method is deprecated. Catch SftpException and look for
	 *             "Permission denied" instead. This prevents unnecessary
	 *             overhead
	 */
	public boolean isReadable(String path) {
		StringBuilder command = new StringBuilder();
		command.append("[ -r \"");
		command.append(path.equals("~")
				? SshEngine.executeCommand("pwd").replace("\"", "\\\"")
				: path.replace("\"", "\\\""));
		command.append("\" ] && echo 1 || echo 0"); // short circuiting
		return SshEngine.executeCommand(command.toString()).trim().equals("1");
	}

	/**
	 * @deprecated This method is deprecated. Catch SftpException and look for
	 *             "Permission denied" instead
	 */
	public boolean isWriteable(String path) {
		StringBuilder command = new StringBuilder();
		command.append("[ -w \"");
		command.append(path.equals("~")
				? SshEngine.executeCommand("pwd").replace("\"", "\\\"")
				: path.replace("\"", "\\\""));
		command.append("\" ] && echo 1 || echo 0"); // short circuiting
		return SshEngine.executeCommand(command.toString()).trim().equals("1");
	}

	public void showFrame(boolean show) {
		frame.setVisible(show);
	}

	public String getTitle() {
		StringBuilder title = new StringBuilder(Constants.APP_NAME);
		title.append(" - ");
		title.append(LoginCredentials.host);
		return title.toString();
	}

	/*
	 * ========== END Other ==========
	 */

}
