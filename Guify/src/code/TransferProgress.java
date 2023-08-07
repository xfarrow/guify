package code;

/**
 * 
 * An object representing the transfer progress of a file between the server and
 * the host machine.
 *
 */
public class TransferProgress {

	// Transfer statuses
	public static final int INIT = 0;
	public static final int UPDATING = 1;
	public static final int END = 2;

	private String source;
	private String destination;
	private long totalBytes;
	private long transferredBytes;
	private int operation;
	private int transferStatus;

	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public long getTotalBytes() {
		return totalBytes;
	}
	public void setTotalBytes(long totalBytes) {
		this.totalBytes = totalBytes;
	}
	public long getTransferredBytes() {
		return transferredBytes;
	}
	public void setTransferredBytes(long transferredBytes) {
		this.transferredBytes = transferredBytes;
	}
	public int getOperation() {
		return operation;
	}
	public void setOperation(int operation) {
		this.operation = operation;
	}
	public int getTransferStatus() {
		return transferStatus;
	}
	public void setTransferStatus(int transferStatus) {
		this.transferStatus = transferStatus;
	}
}
