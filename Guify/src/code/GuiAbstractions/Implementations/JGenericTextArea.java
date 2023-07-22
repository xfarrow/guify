package code.GuiAbstractions.Implementations;

import javax.swing.JTextArea;
import code.GuiAbstractions.Interfaces.IGenericTextArea;

/**
 * 
 * A class implementing an interface for a generic text
 * area. It is currently using JTextArea (Java Swing)
 *
 */
public class JGenericTextArea implements IGenericTextArea {
	
	private JTextArea textArea;
	
	public JGenericTextArea(JTextArea textArea) {
		this.textArea = textArea;
	}
	
	@Override
	public void selectText(int start, int end) {
		if(textArea == null)
			return;
	
	textArea.requestFocus(); // enforce focus or it will not be selected
	textArea.select(start, end);
	}
	
	public String getText() {
		if(textArea == null) {
			throw new NullPointerException("TextArea is null");
		}
		
		return textArea.getText();
	}
	
	public void setText(String text) {
		if(textArea == null) {
			throw new NullPointerException("TextArea is null");
		}
		
		textArea.setText(text);
	}
	
	public void replaceRange(String s, int start, int end) {
		if(textArea == null) {
			throw new NullPointerException("TextArea is null");
		}
		
		textArea.replaceRange(s, start, end);
	}

	@Override
	public void setCaretPosition(int position) {
		if(textArea == null) {
			throw new NullPointerException("TextArea is null");
		}
		
		textArea.setCaretPosition(position);
	}

	@Override
	public int getCaretPosition() {
		if(textArea == null) {
			throw new NullPointerException("TextArea is null");
		}
		
		return textArea.getCaretPosition();
	}
	
	public int getSelectionStart(){
		if(textArea == null) {
			throw new NullPointerException("TextArea is null");
		}
		return textArea.getSelectionStart();
	}

	@Override
	public boolean hasHighlightedText() {
		if(textArea == null) {
			throw new NullPointerException("TextArea is null");
		}
		return textArea.getSelectionStart() != textArea.getCaretPosition();
	}
	
}
