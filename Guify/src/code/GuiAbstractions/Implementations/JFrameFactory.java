package code.GuiAbstractions.Implementations;

import code.GuiAbstractions.Interfaces.IFrameFactory;
import views.*;

/**
 * Frame factory. Factory for JFrame (Java Swing).
 */
public class JFrameFactory implements IFrameFactory {
	
	public static Object createJFrame(int frameType) throws Exception {
		
		switch(frameType) {
		
		case LOGIN:
			throw new Exception("Frame Login requires additional parameters. "
					+ "Call createJFrame(int frameType, Object additionalParameters) instead");
			
		case DESKTOP:
			throw new Exception("Frame Desktop requires additional parameters. "
					+ "Call createJFrame(int frameType, Object additionalParameters) instead");
			
		case QUEUE:
			return new Queue();
			
		case NOTEPAD:
			throw new Exception("Frame Notepad requires additional parameters. "
					+ "Call createJFrame(int frameType, Object additionalParameters) instead");
			
		case FIND_AND_REPLACE:
			throw new Exception("Frame FindAndReplace requires additional parameters. "
					+ "Call createJFrame(int frameType, Object additionalParameters) instead");
			
		default:
			throw new Exception("Invalid frame name");
		}
	}
	
	public static Object createJFrame(int frameType, Object controller) throws Exception{
		
		if(	frameType != NOTEPAD 
			&& frameType != FIND_AND_REPLACE 
			&& frameType != LOGIN 
			&& frameType != DESKTOP) {
				System.err.println("additionalParams ignored for this frame");
				return createJFrame(frameType);
		}
		
		switch(frameType) {
		
		case LOGIN:
			return new Login(controller);
			
		case DESKTOP:
			return new Desktop(controller);
			
		case NOTEPAD:
			return new Notepad(controller);
			
		case FIND_AND_REPLACE:
			return new FindAndReplace(controller);
		
		default:
			throw new Exception("Invalid frame name");
		}
	}
	
}
