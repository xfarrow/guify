package code;

import com.jcraft.jsch.ChannelSftp.LsEntry;

/**
 * 
 * Interface describing a DirectoryNodeButton, independently of how a concrete
 * DirectoryNodeButton will be (currently it is concretely a JButton)
 *
 */
public interface IDirectoryNodeButton {
	public void setNode(LsEntry node);
	public LsEntry getNode();
	public void setSelected(boolean selected);
	public boolean getSelected();
}
