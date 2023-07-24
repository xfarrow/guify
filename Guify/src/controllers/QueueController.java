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

@SuppressWarnings("deprecation")
public class QueueController implements Observer{
	
	/*
	 * ========== BEGIN Attributes ==========
	 */
	
	private IQueueFrame frame;
	private HashMap<TransferProgress, Integer> indexHashMap = new HashMap<>();
	
	/*
	 * ========== END Attributes ==========
	 */
	
	// Executed by the EDT
	public QueueController() {
		try {
			frame = (IQueueFrame) JFrameFactory.createJFrame(IFrameFactory.QUEUE, this);
		} 
		catch (Exception e) {}
		
		// Register observer of the changes
		QueueEventManager.getInstance().addObserver(this);
		
		// Get previous enqueued elements. Do not place before addObserver(this) or some
		// transfers could go lost
		TransferProgress[] queued = QueueEventManager.getInstance().getQueue();
		for(TransferProgress transferProgress : queued) {
			SwingUtilities.invokeLater(new Runnable() {
			      @Override
			      public void run() {
			    	  frame.manageTransferProgress(transferProgress);
			      }
			});
		}
	}
	
	// Executed by different threads
	@Override
	public void update(Observable o, Object arg) {
		TransferProgress transferProgress = (TransferProgress)arg;
		SwingUtilities.invokeLater(new Runnable() {
		      @Override
		      public void run() {
		    	  frame.manageTransferProgress(transferProgress);
		      }
		});
	}
	
	public int computePercentage(TransferProgress transferProgress) {
		// Avoid division by zero
		if(transferProgress.getTotalBytes() == 0) {
			// The percentage is 100% if ∀ byte in the file, byte was transferred.
			// If there are no bytes in the file, this logic proposition holds true (vacuous truth)
			return 100;
		}
		
		// Avoid "stuck at 99%" due to precision issues
		else if(transferProgress.getTotalBytes() == transferProgress.getTransferredBytes()) {
			return 100;
		}
		
		else {
			return (int) Math.floor( ((transferProgress.getTransferredBytes() * 100) / transferProgress.getTotalBytes()) );
		}
	}
	
	public Integer getTableIndex(TransferProgress transferProgress) {
		return indexHashMap.get(transferProgress);
	}
	
	public void putTableIndex(TransferProgress transferProgress, Integer index) {
		indexHashMap.put(transferProgress, index);
	}
	
	public void showFrame(boolean visible) {
		frame.setVisible(visible);
	}
	
	public boolean isTransferProgressInHashMap(TransferProgress transferProgress) {
		return getTableIndex(transferProgress) != null;
	}
	
}
