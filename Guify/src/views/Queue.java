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
	
	public void updateRow(int rowIndex, int percentage) {
		if(rowIndex < tableModel.getRowCount()) {
			tableModel.setValueAt(percentage, rowIndex, 3);
		}
	}

	@Override
	public void manageTransferProgress(TransferProgress transferProgress) {
		
		if(transferProgress.getTransferStatus() == TransferProgress.INIT) {
			if(!controller.isTransferProgressInHashMap(transferProgress)) {
				controller.putTableIndex(transferProgress, 
								addRow(transferProgress.getSource(), 
								transferProgress.getDestination(), 
								transferProgress.getOperation() == SftpProgressMonitor.GET? "Download" : "Upload", 
								0));
			}
		}
		
		else if(transferProgress.getTransferStatus() == TransferProgress.UPDATING) {
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
		
		else if(transferProgress.getTransferStatus() == TransferProgress.END) {
			if(!controller.isTransferProgressInHashMap(transferProgress)) {
				controller.putTableIndex(transferProgress, 
								addRow(transferProgress.getSource(), 
								transferProgress.getDestination(), 
								transferProgress.getOperation() == SftpProgressMonitor.GET? "Download" : "Upload", 
								100));
			}
			else {
				updateRow(controller.getTableIndex(transferProgress), 100);
			}
		}
		
	}
}
