package computer;

public class VirtualKeyboard extends DCPUHardware
{
  public static final int KEY_BACKSPACE = 16;
  public static final int KEY_RETURN = 17;
  public static final int KEY_INSERT = 18;
  public static final int KEY_DELETE = 19;
  public static final int KEY_UP = 128;
  public static final int KEY_DOWN = 129;
  public static final int KEY_LEFT = 130;
  public static final int KEY_RIGHT = 131;
  public static final int KEY_SHIFT = 144;
  public static final int KEY_CONTROL = 145;
  private KeyMapping keyMapping;
  private char[] keyBuffer = new char[64];
  private int krp;
  private int kwp;
  private boolean[] isDown = new boolean[256];
  private char interruptMessage;
  private boolean doInterrupt;

  public VirtualKeyboard(KeyMapping keyMapping)
  {
    super(818902022, 4919, 515079825);
    this.keyMapping = keyMapping;
  }

  public void keyTyped(int i) {
    if ((i <= 20) || (i > 127)) return;
    if (this.keyBuffer[(this.kwp & 0x3F)] == 0) {
      this.keyBuffer[(this.kwp++ & 0x3F)] = (char)i;
      this.doInterrupt = true;
    }
  }

  public void keyPressed(int key) {
    int i = this.keyMapping.getKey(key);
    if (i < 0) return;
    if ((i < 20) && 
      (this.keyBuffer[(this.kwp & 0x3F)] == 0)) {
      this.keyBuffer[(this.kwp++ & 0x3F)] = (char)i;
    }

    this.isDown[i] = true;
    this.doInterrupt = true;
  }

  public void keyReleased(int key) {
    int i = this.keyMapping.getKey(key);
    if (i < 0) return;
    this.isDown[i] = false;
    this.doInterrupt = true;
  }

  public void interrupt() {
    int a = this.dcpu.registers[0];
    if (a == 0) {
      for (int i = 0; i < this.keyBuffer.length; i++) {
        this.keyBuffer[i] = '\000';
      }
      this.krp = 0;
      this.kwp = 0;
    } else if (a == 1) {
      if ((this.dcpu.registers[2] = this.keyBuffer[(this.krp & 0x3F)]) != 0)
        this.keyBuffer[(this.krp++ & 0x3F)] = '\000';
    }
    else if (a == 2) {
      int key = this.dcpu.registers[1];
      if ((key >= 0) && (key < 256))
        this.dcpu.registers[2] = (char)(this.isDown[key] ? 1 : 0);
      else
        this.dcpu.registers[2] = '\000';
    }
    else if (a == 3) {
      this.interruptMessage = this.dcpu.registers[1];
    }
  }

  public void tick60hz() {
    if (this.doInterrupt) {
      if (this.interruptMessage != 0) this.dcpu.interrupt(this.interruptMessage);
      this.doInterrupt = false;
    }
  }
}