package views;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import com.jcraft.jsch.SftpProgressMonitor;
import code.Constants;
import code.TransferProgress;
import controllers.QueueController;
import views.interfaces.IQueueFrame;
public class Queue extends JFrame implements IQueueFrame {

	private static final long serialVersionUID = 1L;
	private QueueController controller;
	
	/**
	 *
	 * Custom cell renderer in order to be able to display
	 * a progress bar in the JTable
	 *
	 */
 	public static class ProgressBarTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		private JProgressBar progressBar;
		
	    public ProgressBarTableCellRenderer() {
	        super();
	        progressBar = new JProgressBar();
	        progressBar.setStringPainted(true);
	    }

	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
	                                                   boolean hasFocus, int row, int column) {
	        if (value instanceof Integer) {
	            int progressValue = (Integer) value;
	            progressBar.setValue(progressValue);
	            progressBar.setString(progressValue + "%");
	        }
	        
	        progressBar.setStringPainted(true);
	        progressBar.setForeground(Constants.GuifyColors.BLUE);
            progressBar.setBackground(Color.WHITE);
            
	        return progressBar;
	    }
	}
	
	public DefaultTableModel tableModel;
	
	public Queue(Object controller) {
		this.controller = (QueueController) controller;
		setTitle("Queue");
		String[] columnNames = {"Source", "Destination", "Operation", "Percentage"};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        table.setEnabled(false); // Prevents user editing
        // Show percentage by using a custom cell renderer
        TableColumn percentageColumn = table.getColumnModel().getColumn(3);
        percentageColumn.setCellRenderer(new ProgressBarTableCellRenderer());
        JScrollPane scrollPane = new JScrollPane(table);
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.pack();
	}
	
	/**
	 * Adds a row in the JTable
	 * @return The index of the inserted row
	 */
	public int addRow(String source, String destination, String operation, int percentage) {
        tableModel.addRow(new Object[]{source, destination, operation, percentage});
        return tableModel.getRowCount() - 1;
	}
	
	/**
	 * Given a value (representing the transfer completion percentage)
	 * and an index representing the 0-based index of the row to update, 
	 * update that row with that value
	 * @param rowIndex 0-base index of the row to update
	 * @param percentage The transfer completion percentage to set
	 */
	public void updateRow(int rowIndex, int percentage) {
		if(rowIndex < tableModel.getRowCount()) {
			tableModel.setValueAt(percentage, rowIndex, 3);
		}
	}

	// This method can receive a transferProgress whose status can be INIT, UPDATING
	// or END.
	// In all of these cases it is possible that transferProgress is or is not in the HashMap,
	// and we'll prove its correctness in all the cases:
	//
	// 1. Init: This method can receive a transferProgress whose status is INIT and
	// 		a. Not present in the HashMap: this can happen either when QueueController receives
	// 		an update() or when it iterates in the constructor over the previously enqueued
	//		elements. In both cases, the transfer gets correctly put in the table.
	//		b. Present in the HashMap: despite it's counterintuitive, this can happen when 
	// 		a transfer gets initialized after QueueEventManager.getInstance().addObserver(this);
	//		and before QueueEventManager.getInstance().getQueue().
	// 		This would lead either update()
	//		or the QueueController's constructor to call this method over a transferProgress
	//		already in the HashMap (because inserted by the other one)
	//		but whose status is INIT. In this case, updateRow() will
	//		be called, but without any side effects as the percentage would be zero regardless.
	//
	// 2. Updating:
	//		a. Not present in the HashMap: This can happen when the Queue UI is opened
	//		while an element is already being transferred. This happens because when
	//		the transfer had a "INIT" status, this object did not exist yet.
	//		If it's not present in the HashMap
	//		then it will be added.
	//		b. Present in the HashMap: then it will be correctly updated.
	//
	// 3. End: Same case for Updating
	//
	// It's important to note that this method will always operate over the 
	// last version of the same TransferProgress object as it is updated 
	// from another thread when any chunk of data is transmitted. 
	// This will not create any inconsistencies, because the only
	// attribute that can be different is getTransferredBytes which will
	// be read just once per call, nor race conditions
	// because this method only performs reads, without modifying.
	//
	// This ensures that at any
	// time t1, the value of the percentage of a specific TransferProgress
	// will always be equal or greater than the value it was at any time t
	// where t < t1. In other words it's impossible that the progress bar
	// will go down (e.g. from 50% to 49%).
	@Override
	public void manageTransferProgress(TransferProgress transferProgress) {
		
		// Remember that when QueueController calls frame.manageTransferProgress(transferProgress),
		// here transferProgress might have different attributes than it was originally called on
		// (as it's updated by a different thread).
		// We do not need a lock as we do not edit it, but just keep it in mind.
		
		if(!controller.isTransferProgressInHashMap(transferProgress)) {
			controller.putTableIndex(transferProgress, 
							addRow(transferProgress.getSource(), 
									transferProgress.getDestination(), 
									transferProgress.getOperation() == SftpProgressMonitor.GET? "Download" : "Upload", 
									controller.computePercentage(transferProgress)));
		}
		else {
			updateRow(controller.getTableIndex(transferProgress), controller.computePercentage(transferProgress));
		}
	}
}
