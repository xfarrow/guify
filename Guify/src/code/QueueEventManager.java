package code;

import java.util.Observable; 
import java.util.concurrent.*;
@SuppressWarnings("deprecation") // Observer is okay here
public class QueueEventManager extends Observable {
	
	private static QueueEventManager instance;
	
    // Cannot instantiate from outside
    private QueueEventManager() {}
    
    // We need this object in order to retrieve old transfers which are not being transferred
    ConcurrentLinkedQueue<TransferProgress> queue = new ConcurrentLinkedQueue<TransferProgress>();
    
    public static synchronized QueueEventManager getInstance() {
        if (instance == null) {
            instance = new QueueEventManager();
        }
        return instance;
    }
    
    public void notify(TransferProgress arg) {
    	updateQueue(arg);
        setChanged();
        notifyObservers(arg);
    }
    
    private void updateQueue(TransferProgress transferProgressObj) {
    	if(transferProgressObj.getTransferStatus() == TransferProgress.INIT) {
    		queue.add(transferProgressObj);
    	}
    	else if(transferProgressObj.getTransferStatus() == TransferProgress.END) {
    		queue.remove();
    	}
    }
    
    public TransferProgress[] getQueue() {
    	return queue.toArray(new TransferProgress[queue.size()]);
    }
}
