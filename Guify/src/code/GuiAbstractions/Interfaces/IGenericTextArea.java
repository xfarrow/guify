package code.GuiAbstractions.Interfaces;

/**
 * 
 * 
 * Interface for a generic TextArea.
 * It is used to create an abstraction of a
 * TextArea, without using view-specific objects
 * (such as JTextArea).
 * 
 * This increases modularity, flexibility and
 * creates a separation of concerns.
 * 
 * In case of change of the GUI library you
 * do not need to change neither the Controllers nor
 * these interfaces, but only the implementations.
 *
 */
public interface IGenericTextArea {
	void selectText(int start, int end);
	String getText();
	void setText(String text);
	void replaceRange(String s, int start, int end);
	void setCaretPosition(int position);
	int getCaretPosition();
	int getSelectionStart();
	boolean hasHighlightedText();
}
