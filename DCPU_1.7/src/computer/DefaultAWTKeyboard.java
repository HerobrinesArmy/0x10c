package computer;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

public class DefaultAWTKeyboard extends VirtualKeyboard implements KeyEventDispatcher {
	public DefaultAWTKeyboard() {
		super(new AWTKeyMapping());
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			keyPressed(e.getKeyCode());
	  } else if (e.getID() == KeyEvent.KEY_RELEASED) {
	  	keyReleased(e.getKeyCode());
	  } else if (e.getID() == KeyEvent.KEY_TYPED) {
	  	keyTyped(e.getKeyChar());
	  }
	  return false;
	}
}
