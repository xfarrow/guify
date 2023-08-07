package controllers;

import code.GuiAbstractions.Implementations.JFrameFactory;
import code.GuiAbstractions.Interfaces.*;
import views.interfaces.IFindAndReplaceFrame;
import views.interfaces.INotepadFrame;

public class FindAndReplaceController {

	private IGenericTextArea textArea;
	private IFindAndReplaceFrame frame;

	public FindAndReplaceController(IGenericTextArea textArea) {
		this.textArea = textArea;
		try {
			frame = (IFindAndReplaceFrame) JFrameFactory
					.createJFrame(IFrameFactory.FIND_AND_REPLACE, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Show frame centered to parent
	 */
	public void showAtTheCenterOfFrame(INotepadFrame notepadFrame) {
		int childX = notepadFrame.getX()
				+ (notepadFrame.getWidth() - frame.getWidth()) / 2;
		int childY = notepadFrame.getY()
				+ (notepadFrame.getHeight() - frame.getHeight()) / 2;
		frame.setLocation(childX, childY);
		frame.setVisible(true);
	}

	public int findNext(String searchText) {
		String text = textArea.getText();
		int currentIndex = textArea.getCaretPosition();
		int nextIndex = text.indexOf(searchText, currentIndex);

		if (nextIndex != -1) {
			textArea.selectText(nextIndex, nextIndex + searchText.length());
			return nextIndex;
		}
		return -1;
	}

	public int findPrevious(String searchText) {
		String text = textArea.getText();
		int cutAt;

		if (textArea.hasHighlightedText()) {
			cutAt = textArea.getSelectionStart();
		} else {
			cutAt = textArea.getCaretPosition();
		}
		String firstPart = text.substring(0, cutAt);
		int previousIndex = firstPart.lastIndexOf(searchText,
				firstPart.length() - 1);
		if (previousIndex != -1) {
			textArea.selectText(previousIndex,
					previousIndex + searchText.length());
			return previousIndex;
		} else {
			return -1;
		}
	}

	public int replaceNext(String toReplace, String replaceWith) {
		int index = findNext(toReplace);

		if (index != -1) {
			textArea.replaceRange(replaceWith, index,
					index + toReplace.length());
		}

		return index;
	}

	public void replaceAll(String searchText, String replacement) {
		String text = textArea.getText();
		text = text.replaceAll(searchText, replacement);
		textArea.setText(text);
	}

	public void disposeMyFrame() {
		if (frame != null) {
			frame.dispose();
		}
	}
}
