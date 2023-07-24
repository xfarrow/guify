package views.interfaces;

import code.TransferProgress;

public interface IQueueFrame {
	public void setVisible(boolean visible);
	public void manageTransferProgress(TransferProgress transferProgress);
}
