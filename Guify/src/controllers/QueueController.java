package controllers;

import java.util.Observable;
import java.util.Observer;
import code.QueueEventManager;
import code.TransferProgress;
import code.GuiAbstractions.Implementations.JFrameFactory;
import code.GuiAbstractions.Interfaces.IFrameFactory;
import views.interfaces.IQueueFrame;

import java.util.concurrent.ConcurrentHashMap;
import javax.swing.SwingUtilities;

import com.jcraft.jsch.SftpProgressMonitor;

@SuppressWarnings("deprecation") // Observer/Observable objects are okay here
public class QueueController implements Observer {
	
	private IQueueFrame frame;
	// A HashMap containing the Transfer Progress entry and the index of said entry
	// in the table.
	// We need a ConcurrentHashMap instead of a simple HashMap because it
	// is accessed by multiple threads. In particular the threads executing update()
	// and the Event Dispatch Thread.
	// Alternatively, the HashMap could've been managed by the view itself without the need
	// to concern over threading/concurrent problems.
	// TODO Make the HashMap to be handled by the view itself (EDT thread) rather than concurrent threads
	// to enhance readability and understanding
	private ConcurrentHashMap<TransferProgress, Integer> indexAssociationMap = new ConcurrentHashMap<>();
	private final static int HASHMAP_DUMMY_VALUE = -1;
	
	// Executed by the EDT
	public QueueController() {
		try {
			frame = (IQueueFrame) JFrameFactory.createJFrame(IFrameFactory.QUEUE);
		} 
		catch (Exception e) {}
		
		// Register observer of the changes
		QueueEventManager.getInstance().addObserver(this);
		
		// Get previous enqueued elements. Do not place before addObserver(this) or some
		// transfers could go lost
		TransferProgress[] queued = QueueEventManager.getInstance().getQueue();
		for(TransferProgress transferProgress : queued) {
			// It is possible that while iterating on this for, the element
			// has already been inserted into the indexAssociationMap from
			// another thread executing update(), hence, we check if the key is contained 
			// already.
			if (indexAssociationMap.putIfAbsent(transferProgress, HASHMAP_DUMMY_VALUE) == null) {
				int percentage = (int) Math.floor( ((transferProgress.getTransferredBytes() * 100) / transferProgress.getTotalBytes()) );
				int rowIndex = frame.addRow(transferProgress.getSource(), transferProgress.getDestination(), transferProgress.getOperation() == SftpProgressMonitor.GET? "Download" : "Upload", percentage);
		        indexAssociationMap.put(transferProgress, rowIndex);
			}
		}
	}
	
	// Updated by QueueEventManager. Can run simultaneously
	// on multiple threads.
	@Override
	public void update(Observable o, Object arg) {
		TransferProgress transferProgressObj = (TransferProgress)arg;
		if(transferProgressObj.getTransferStatus() == TransferProgress.INIT) {
			// Since the Runnable in SwingUtilities.invokeLater contained in this if
			// might run *after* a subsequent execution of
			// update() having a TransferProgress whose status is UPDATING, said subsequent update()
			// must know that this specific transferProgressObj was already inserted in the HashMap,
			// otherwise the if (indexAssociationMap.putIfAbsent(transferProgressObj, HASHMAP_DUMMY_VALUE) == null)
			// (contained in the if checking whether the status is UPDATING)
			// would return true and add it again to the table, because indexAssociationMap.put(transferProgressObj, rowIndex);
			// has not been completed yet, being in the Runnable.
			//
			// We call putIfAbsent() instead of just put() because it is possible that this transferProgressObj is also
			// in the initial for present in Queue() if this transfer was initiated in a time x where
			// t1 < x < t2, where t1 is the time of completion of QueueEventManager.getInstance().addObserver(this);
			// and t2 is the time of completion of QueueEventManager.getInstance().getQueue(); 
			//
			// To sum it up:
			// 1) update() receives transferProgressObj whose status is "INIT". Puts "DUMMY_VALUE"
			// and schedules the EDT for a table insertion;
			// 2) update() receives the same transferProgressObj with the status "UPDATING".
			// It successfully sees that indexAssociationMap contains this specific transferProgressObj,
			// whose value is DUMMY_VALUE, so it does not schedule an insert, but rather an update. While
			// updating it will see that the value is not valid (if(rowIndex != HASHMAP_DUMMY_VALUE)) and will
			// not perform an update as well.
			// 3) The EDT runs and puts the correct index in the HashMap.
			//
			// Remember that if an update() receives a transferProgress whose status is INIT, that update() will always run
			// before an update() on the same transferProgress whose status is UPDATING, as the thread handling
			// each transferProgress is one and one only.
			if(indexAssociationMap.putIfAbsent(transferProgressObj, HASHMAP_DUMMY_VALUE) == null) {
				// We need SwingUtilities.invokeLater because we
				// are not on the Event Dispatch Thread (the thread
				// responsible for GUI management), but rather on the
				// thread created in SshEngine
				SwingUtilities.invokeLater(new Runnable() {
				      @Override
				      public void run() {
				    	  int rowIndex = frame.addRow(transferProgressObj.getSource(), transferProgressObj.getDestination(), transferProgressObj.getOperation() == SftpProgressMonitor.GET? "Download" : "Upload", 0);
				    	  indexAssociationMap.put(transferProgressObj, rowIndex);
				      }
				});
			}
		}
		else if(transferProgressObj.getTransferStatus() == TransferProgress.UPDATING) {
			int percentage;
			// Avoid division by zero
			if(transferProgressObj.getTotalBytes() == 0) {
				// The percentage is 100% if ∀ byte in the file, byte was transferred.
				// If there are no bytes in the file, this logic proposition holds true (vacuous truth)
				percentage = 100;
			}
			else {
				percentage = (int) Math.floor( ((transferProgressObj.getTransferredBytes() * 100) / transferProgressObj.getTotalBytes()) );
			}
			// It is possible to receive TransferProgress.UPDATING without receiving
			// a TransferProgress.INIT (when this controller gets created when the transferring
			// was already occurring) and before "for(TransferProgress transferProgress : queued)"
			// gets executed on this element
			if (indexAssociationMap.putIfAbsent(transferProgressObj, HASHMAP_DUMMY_VALUE) == null) {
				// We need SwingUtilities.invokeLater because we
				// are not on the Event Dispatch Thread (the thread
				// responsible for GUI management), but rather on the
				// thread created in SshEngine
				SwingUtilities.invokeLater(new Runnable() {
				      @Override
				      public void run() {
				    	  int rowIndex = frame.addRow(transferProgressObj.getSource(), transferProgressObj.getDestination(), transferProgressObj.getOperation() == SftpProgressMonitor.GET? "Download" : "Upload", percentage);
				    	  indexAssociationMap.put(transferProgressObj, rowIndex);
				      }
				});
			}
			else {
				int rowIndex = indexAssociationMap.get(transferProgressObj);
				// It is possible that rowIndex is a DUMMY_VALUE if the insertion
				// into the table has been scheduled with SwingUtilities.invokeLater
				// but has not ran yet
				if(rowIndex != HASHMAP_DUMMY_VALUE) {
					// We need SwingUtilities.invokeLater because we
					// are not on the Event Dispatch Thread (the thread
					// responsible for GUI management), but rather on the
					// thread created in SshEngine
					SwingUtilities.invokeLater(new Runnable() {
					      @Override
					      public void run() {
					    	  frame.updateRow(rowIndex, percentage);
					      }
					});
				}
				else {
					// TODO If the file is small enough, in all the (few)
					// updates, rowIndex could always be HASHMAP_DUMMY_VALUE
					// because when SwingUtilities tries to insert the row the first time,
					// the EDT task will not execute soon enough before the termination
					// of the transfer, so we will see that the percentage it's fixed
					// to a specific value.
				}
			}
		}
		else if(transferProgressObj.getTransferStatus() == TransferProgress.END) {
			// We choose not to remove the element from the table once it has finished
		}
	}
	
	public void showFrame(boolean visible) {
		frame.setVisible(true);
	}
}
