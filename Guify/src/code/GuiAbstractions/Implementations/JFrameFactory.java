package code.GuiAbstractions.Implementations;

import code.GuiAbstractions.Interfaces.IFrameFactory;
import views.*;

/**
 * Frame factory. Factory for JFrame (Java Swing).
 */
public class JFrameFactory implements IFrameFactory {
	
	public static Object createJFrame(int frameType, Object controller) throws Exception{
		
		switch(frameType) {
		
		case LOGIN:
			return new Login(controller);
			
		case DESKTOP:
			return new Desktop(controller);
			
		case NOTEPAD:
			return new Notepad(controller);
			
		case FIND_AND_REPLACE:
			return new FindAndReplace(controller);
			
		case QUEUE:
			return new Queue(controller);
		
		default:
			throw new Exception("Invalid frame name");
		}
	}
	
}
