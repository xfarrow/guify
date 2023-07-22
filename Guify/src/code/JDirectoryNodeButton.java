package code;

import javax.swing.JButton;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;

/**
 * 
 * A JButton representing a Directory Node.
 * Useful when a Directory Node is needed
 * to be drew on screen.
 *
 */
public class JDirectoryNodeButton extends JButton implements IDirectoryNodeButton {
	
	private static final long serialVersionUID = 1L;
	public ChannelSftp.LsEntry node = null;
	private boolean isSelected = false;
	
	public JDirectoryNodeButton() {
		super();
	}
	
	public JDirectoryNodeButton(ChannelSftp.LsEntry node) {
		super();
		setNode(node);
	}
	
	public void setNode(ChannelSftp.LsEntry node) {
		this.node = node;
	}
	
	public void setSelected(boolean selected) {
		this.isSelected = selected;
	}
	
	public boolean getSelected() {
		return this.isSelected;
	}

	@Override
	public LsEntry getNode() {
		return this.node;
	}
}
