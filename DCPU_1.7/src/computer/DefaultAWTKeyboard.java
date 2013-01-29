package computer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class DefaultAWTKeyboard extends VirtualKeyboard implements KeyListener {
	public DefaultAWTKeyboard() {
		super(new AWTKeyMapping());
	}

  public void keyPressed(KeyEvent ke) {
    keyPressed(ke.getKeyCode());
  }

  public void keyReleased(KeyEvent ke) {
    keyReleased(ke.getKeyCode());
  }

  public void keyTyped(KeyEvent ke) {
    keyTyped(ke.getKeyChar());
  }
}
