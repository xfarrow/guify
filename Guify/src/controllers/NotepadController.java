package controllers;

import code.SshEngine;
import code.GuiAbstractions.Implementations.JFrameFactory;
import code.GuiAbstractions.Interfaces.IFrameFactory;
import code.GuiAbstractions.Interfaces.IGenericTextArea;
import views.interfaces.INotepadFrame;
public class NotepadController {
	
	private String filePath = null;
	private INotepadFrame notepadFrame = null;
	private FindAndReplaceController myFindAndReplaceController;
	private boolean unsaved = false;
	
	public boolean isUnsaved() {
		return unsaved;
	}

	public void setUnsaved(boolean unsaved) {
		this.unsaved = unsaved;
	}

	public String getFilePath() {
		return this.filePath;
	}
	
	public NotepadController(String filePath) {
		this.filePath = filePath;
		String contentToDisplay = SshEngine.readFile(filePath);
		try {
			notepadFrame = (INotepadFrame) JFrameFactory.createJFrame(IFrameFactory.NOTEPAD, this);
			notepadFrame.displayContent(contentToDisplay);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void show() {
		notepadFrame.setVisible(true);
	}
	
	public void writeOnFile(String text) {
		SshEngine.writeFile(text, this.filePath);
	}
	
	public void showFindAndReplace(IGenericTextArea textArea) {
		myFindAndReplaceController = new FindAndReplaceController(textArea);
		myFindAndReplaceController.showAtTheCenterOfFrame(notepadFrame);
	}
	
	public void disposeFindAndReplaceFrame() {
		if(myFindAndReplaceController != null)
			myFindAndReplaceController.disposeMyFrame();
	}
	
	public String getTitle() {
		return filePath + (unsaved ? " - UNSAVED" : "");
	}
}
