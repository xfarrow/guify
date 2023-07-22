package views.interfaces;

public interface IQueueFrame {
	public void setVisible(boolean visible);
	int addRow(String source, String destination, String operation, int percentage);
	void updateRow(int rowIndex, int percentage);
}
