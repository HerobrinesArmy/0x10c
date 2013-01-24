package computer;

public class VirtualClock extends DCPUHardware
{
  private int interval;
  private int intCount;
  private char ticks;
  private char interruptMessage;

  public VirtualClock()
  {
    super(315667458, 32776, 515079825);
  }

  public void interrupt() {
    int a = this.dcpu.registers[0];
    if (a == 0)
      this.interval = this.dcpu.registers[1];
    else if (a == 1)
      this.dcpu.registers[2] = this.ticks;
    else if (a == 2)
      this.interruptMessage = this.dcpu.registers[1];
  }

  public void tick60hz()
  {
    if (this.interval == 0) return;
    if (++this.intCount >= this.interval) {
      if (this.interruptMessage != 0) this.dcpu.interrupt(this.interruptMessage);
      this.intCount = 0;
      this.ticks++;
    }
  }
}