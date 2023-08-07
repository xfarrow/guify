package controllers;

import java.util.Observable;
import java.util.Observer;
import javax.swing.SwingUtilities;
import code.QueueEventManager;
import code.TransferProgress;
import code.GuiAbstractions.Implementations.JFrameFactory;
import code.GuiAbstractions.Interfaces.IFrameFactory;
import views.interfaces.IQueueFrame;
import java.util.HashMap;

@SuppressWarnings("deprecation") // Observer - Observable are okay here
public class QueueController implements Observer {

	/*
	 * ========== BEGIN Attributes ==========
	 */

	private IQueueFrame frame;
	// Accessed only through the EDT, so no need to use ConcurrentHashMap
	private HashMap<TransferProgress, Integer> indexHashMap = new HashMap<>();
	private long lastGuiExecutionTime = 0;
	private static final long THROTTLE_TIME_MS = 10; // 10ms = 1/100th of second

	/*
	 * ========== END Attributes ==========
	 */

	// Executed by the EDT
	public QueueController() {
		try {
			frame = (IQueueFrame) JFrameFactory
					.createJFrame(IFrameFactory.QUEUE, this);
		} catch (Exception e) {
		}

		// Register observer of the changes
		QueueEventManager.getInstance().addObserver(this);

		// Get previous enqueued elements. Do not place before addObserver(this)
		// or some
		// transfers could go lost.
		//
		// A necessary but not sufficient condition in order to claim that the
		// Queue
		// works well, is that when it gets opened at time x, all the
		// non-finished
		// transfers initiated in a time t such that t < x must be shown
		// in the table.
		// More specifically, x represents the time of completion of the
		// instruction
		// QueueEventManager.getInstance().addObserver(this); as
		// any subsequent transfer initiated after this point will be guaranteed
		// to
		// be shown in the table (handled by the "update()" method).
		//
		// Having understood this, we may now suppose t1 to be the time of
		// completion of the
		// instruction QueueEventManager.getInstance().getQueue() and t2 to be
		// the time of completion of the instruction
		// QueueEventManager.getInstance().addObserver(this) where t1 < t2.
		// This would've meant that any transfer initiated in a time t such that
		// t1 < t < t2 would not have been adequately processed as "getQueue()"
		// was
		// already executed, and "addObserver(this)" would not have been
		// performed yet,
		// making the transfer not visible in the queue when the Queue frame
		// would've
		// been opened.
		//
		// One could argue that when any chunk of data is transferred at any
		// time t
		// where t > t2, the update() method will be called, showing the
		// transfer in
		// the queue. It's not guaranteed that this happens (as it may encounter
		// an
		// error before t2 and after t1 or simply in may complete in this time
		// frame)

		TransferProgress[] queued = QueueEventManager.getInstance().getQueue();
		for (TransferProgress transferProgress : queued) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					frame.manageTransferProgress(transferProgress);
				}
			});
		}
	}

	// Executed on different threads. Called by notifyObservers in
	// QueueEventManager in turn called
	// by the threads created in SshEngine.
	//
	// Keep in mind that:
	// For all distinct pairs of calls on the method update(o, arg)
	// denoted as (update(o, arg)_1 , update(o, arg)_2) having the same
	// object "arg", it holds that
	// ((TransferProgress)arg).getTransferredBytes() evaluated during
	// update(o, arg)_1 is less or equal than
	// ((TransferProgress)arg).getTransferredBytes()
	// evaluated during update(o, arg)_2.
	// In simple words, if update() is called at time t1 and at time t2,
	// where arg is the same TransferProgress object, then the second time
	// the value of
	// transferProgress.getTransferredBytes() will be greater or equal
	// than the value of
	// transferProgress.getTransferredBytes() evaluated the first time.
	// This happens as the transfer of a single object is an operation executed
	// on a single thread, hence any update will have a getTransferredBytes()
	// greater
	// or equal than the previous one.
	//
	// Keep also in mind that SwingUtilities.invokeLater() called at time t1
	// will always run before SwingUtilities.invokeLater() called at time t2
	// where t2 > t1. In other words, the order of execution is kept.
	//
	// This observation may be sufficient to prove that at any
	// time t1, the value of the percentage of a specific TransferProgress
	// will always be equal or greater than the value it was at any time t
	// where t < t1. In other words it's impossible that the progress bar
	// will go down (e.g. from 50% to 49%), but this would be true even if
	// the concept described above would be false. This happens because
	// the TransferProgress
	// handled by manageTransferProgress will always have the latest updated
	// values
	// for that specific object, as it is shared with the thread
	// which continuously updates it.
	// We do not need any locks as no race conditions
	// can happen because in the EDT thread we only read said object, nor any
	// inconsistencies can arise.
	// For more information you can view the comments at
	// Queue@manageTransferProgress
	@Override
	public void update(Observable o, Object arg) {
		TransferProgress transferProgress = (TransferProgress) arg;

		// Do not invoke the Event Dispatch Thread too frequently as
		// it may impact the performance of low-end computer systems.
		if (System.currentTimeMillis()
				- lastGuiExecutionTime >= THROTTLE_TIME_MS
				|| transferProgress
						.getTransferStatus() == TransferProgress.END) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					frame.manageTransferProgress(transferProgress);
					lastGuiExecutionTime = System.currentTimeMillis();
				}
			});
		}
	}

	/**
	 * Computes the transfer completion percentage
	 * 
	 * @param transferProgress
	 *            A transferProgress object
	 * @return A value in the range [0, 100] indicating the transfer completion
	 *         percentage
	 */
	public int computePercentage(TransferProgress transferProgress) {
		// Avoid division by zero
		if (transferProgress.getTotalBytes() == 0) {
			// The percentage is 100% if ∀ byte in the file, byte was
			// transferred.
			// If there are no bytes in the file, this logic proposition holds
			// true (vacuous truth)
			return 100;
		} else {
			return (int) Math
					.round(((transferProgress.getTransferredBytes() * 100F)
							/ transferProgress.getTotalBytes()));
		}
	}

	/**
	 * Given a TransferProgress object, retrieve its index
	 * 
	 * @param transferProgress
	 * @return Its index or null if not present
	 */
	public Integer getTableIndex(TransferProgress transferProgress) {
		return indexHashMap.get(transferProgress);
	}

	/**
	 * Put index in the HashMap
	 * 
	 * @param transferProgress
	 *            TransferProgress object
	 * @param index
	 *            An integer value representing the index
	 */
	public void putTableIndex(TransferProgress transferProgress,
			Integer index) {
		indexHashMap.put(transferProgress, index);
	}

	/**
	 * Checks if a specific TransferProgress is contained in the HashMap
	 * 
	 * @param transferProgress
	 *            A TransferProgress object
	 * @return true if present, false otherwise
	 */
	public boolean isTransferProgressInHashMap(
			TransferProgress transferProgress) {
		return getTableIndex(transferProgress) != null;
	}

	public void showFrame(boolean visible) {
		frame.setVisible(visible);
	}
}
