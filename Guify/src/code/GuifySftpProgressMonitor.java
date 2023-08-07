package code;

import com.jcraft.jsch.SftpProgressMonitor;

// Documentation: https://epaul.github.io/jsch-documentation/javadoc/com/jcraft/jsch/SftpProgressMonitor.html
public class GuifySftpProgressMonitor implements SftpProgressMonitor {

	TransferProgress transferProgress = null;

	@Override
	public boolean count(long bytes) {

		if (transferProgress != null) {
			transferProgress.setTransferredBytes(
					transferProgress.getTransferredBytes() + bytes);
			transferProgress.setTransferStatus(TransferProgress.UPDATING);
			QueueEventManager.getInstance().notify(transferProgress);
		}

		// true if the transfer should go on
		// false if the transfer should be cancelled
		return true;
	}

	@Override
	public void end() {
		if (transferProgress != null) {
			transferProgress.setTransferStatus(TransferProgress.END);
			QueueEventManager.getInstance().notify(transferProgress);
		}
	}

	@Override
	public void init(int op, String src, String dest, long maxBytes) {
		transferProgress = new TransferProgress();
		transferProgress.setOperation(op);
		transferProgress.setSource(src);
		transferProgress.setDestination(dest);
		transferProgress.setTotalBytes(maxBytes);
		transferProgress.setTransferredBytes(0);
		transferProgress.setTransferStatus(TransferProgress.INIT);
		QueueEventManager.getInstance().notify(transferProgress);
	}
}
