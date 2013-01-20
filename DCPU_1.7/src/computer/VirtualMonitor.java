package computer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class VirtualMonitor extends DCPUHardware
{
  public static final int WIDTH_CHARS = 32;
  public static final int HEIGHT_CHARS = 12;
  public static final int WIDTH_PIXELS = 128;
  public static final int HEIGHT_PIXELS = 96;
  private static final int START_DURATION = 60;
  private int lightColor;
  private int[] palette = new int[16];
  private char[] font = new char[256];
  public int[] pixels = new int[16384];
  private int screenMemMap;
  private int fontMemMap;
  private int paletteMemMap;
  private int[] loadImage = new int[12288];

  private int borderColor = 0;
  private int startDelay = 0;

  public VirtualMonitor() {
    super(1934226965, 6146, 476875574);

    resetPalette();

    int[] pixels = new int[4096];
    try {
      ImageIO.read(VirtualMonitor.class.getResource("/dcpu/hardware/lem/font.png")).getRGB(0, 0, 128, 32, pixels, 0, 128);
      ImageIO.read(VirtualMonitor.class.getResource("/dcpu/hardware/lem/boot.png")).getRGB(0, 0, 128, 96, this.loadImage, 0, 128);
    } catch (IOException e) {
      e.printStackTrace();
    }

    for (int c = 0; c < 128; c++) {
      int ro = c * 2;
      int xo = c % 32 * 4;
      int yo = c / 32 * 8;
      for (int xx = 0; xx < 4; xx++) {
        int bb = 0;
        for (int yy = 0; yy < 8; yy++)
          if ((pixels[(xo + xx + (yo + yy) * 128)] & 0xFF) > 128)
            bb |= 1 << yy;
        int tmp226_225 = (ro + xx / 2);
        char[] tmp226_217 = this.font; 
        tmp226_217[tmp226_225] = (char)(tmp226_217[tmp226_225] | bb << (xx + 1 & 0x1) * 8);
      }
    }
  }

  private void resetPalette() {
    for (int i = 0; i < 16; i++) {
      int b = (i >> 0 & 0x1) * 170;
      int g = (i >> 1 & 0x1) * 170;
      int r = (i >> 2 & 0x1) * 170;
      if (i == 6) {
        g -= 85;
      } else if (i >= 8) {
        r += 85;
        g += 85;
        b += 85;
      }
      this.palette[i] = (0xFF000000 | r << 16 | g << 8 | b);
    }
  }

  private void loadPalette(char[] ram, int offset) {
    for (int i = 0; i < 16; i++) {
      char ch = ram[(offset + i)];
      int b = (ch >> '\000' & 0xF) * 17;
      int g = (ch >> '\004' & 0xF) * 17;
      int r = (ch >> '\b' & 0xF) * 17;
      this.palette[i] = (0xFF000000 | r << 16 | g << 8 | b);
    }
  }

  public void interrupt() {
    int a = this.dcpu.registers[0];
    if (a == 0) {
      if ((this.screenMemMap == 0) && (this.dcpu.registers[1] != 0)) {
        this.startDelay = 60;
      }
      this.screenMemMap = this.dcpu.registers[1];
    } else if (a == 1) {
      this.fontMemMap = this.dcpu.registers[1];
    } else if (a == 2) {
      this.paletteMemMap = this.dcpu.registers[1];
    } else if (a == 3) {
      this.borderColor = (this.dcpu.registers[1] & 0xF);
    } else if (a == 4) {
      int offs = this.dcpu.registers[1];
      for (int i = 0; i < this.font.length; i++) {
        this.dcpu.ram[(offs + i & 0xFFFF)] = this.font[i];
      }
      this.dcpu.cycles += 256;
    } else if (a == 5) {
      int offs = this.dcpu.registers[1];
      for (int i = 0; i < 16; i++) {
        int b = (i >> 0 & 0x1) * 10;
        int g = (i >> 1 & 0x1) * 10;
        int r = (i >> 2 & 0x1) * 10;
        if (i == 6) {
          g -= 5;
        } else if (i >= 8) {
          r += 5;
          g += 5;
          b += 5;
        }
        this.dcpu.ram[(offs + i & 0xFFFF)] = (char)(r << 8 | g << 4 | b);
      }
      this.dcpu.cycles += 16;
    }
  }

  public void render() {
    if ((this.screenMemMap == 0) || (this.startDelay > 0)) {
      int reds = 0;
      int greens = 0;
      int blues = 0;

      if ((this.startDelay > 0) && (this.startDelay < 10)) {
        for (int y = 0; y < 96; y++)
          for (int x = 0; x < 128; x++) {
            int col = this.palette[0];
            this.pixels[(x + y * 128)] = col;
            reds += (col & 0xFF0000);
            greens += (col & 0xFF00);
            blues += (col & 0xFF);
          }
      }
      else {
        for (int y = 0; y < 96; y++) {
          for (int x = 0; x < 128; x++) {
            int cc = this.loadImage[(x + y * 128)] & 0xFF;
            int col = this.palette[1];
            this.pixels[(x + y * 128)] = col;
            reds += (col & 0xFF0000);
            greens += (col & 0xFF00);
            blues += (col & 0xFF);
          }
        }
      }

      int bgColor = 1;
      if (this.startDelay < 26) {
        bgColor = this.startDelay - 10;
      }
      if (bgColor < 0) bgColor = 0;
      int color = this.palette[bgColor];
      for (int y = 96; y < 128; y++) {
        for (int x = 0; x < 128; x++) {
          this.pixels[(x + y * 128)] = color;
        }
      }

      int borderPixels = 100;
      reds += (color & 0xFF0000) * borderPixels;
      greens += (color & 0xFF00) * borderPixels;
      blues += (color & 0xFF) * borderPixels;

      reds = reds / (12288 + borderPixels) & 0xFF0000;
      greens = greens / (12288 + borderPixels) & 0xFF00;
      blues = blues / (12288 + borderPixels) & 0xFF;
      this.lightColor = (reds | greens | blues);
    } else {
      long time = System.currentTimeMillis() / 16L;
      boolean blink = time / 20L % 2L == 0L;
      long reds = 0L;
      long greens = 0L;
      long blues = 0L;

      char[] fontRam = this.font;
      int charOffset = 0;
      if (this.fontMemMap > 0) {
        fontRam = this.dcpu.ram;
        charOffset = this.fontMemMap;
      }
      if (this.paletteMemMap == 0)
        resetPalette();
      else {
        loadPalette(this.dcpu.ram, this.paletteMemMap);
      }

      for (int y = 0; y < 12; y++) {
        for (int x = 0; x < 32; x++) {
          char dat = this.dcpu.ram[(this.screenMemMap + x + y * 32)];
          int ch = dat & 0x7F;
          int colorIndex = dat >> '\b' & 0xFF;
          int co = charOffset + ch * 2;

          int color = this.palette[(colorIndex & 0xF)];
          int colorAdd = this.palette[(colorIndex >> 4 & 0xF)] - color;
          if ((blink) && ((dat & 0x80) > 0)) colorAdd = 0;
          int pixelOffs = x * 4 + y * 8 * 128;

          for (int xx = 0; xx < 4; xx++) {
            int bits = fontRam[(co + (xx >> 1))] >> (xx + 1 & 0x1) * 8 & 0xFF;
            for (int yy = 0; yy < 8; yy++) {
              int col = color + colorAdd * (bits >> yy & 0x1);
              this.pixels[(pixelOffs + xx + yy * 128)] = col;
              reds += (col & 0xFF0000);
              greens += (col & 0xFF00);
              blues += (col & 0xFF);
            }
          }
        }
      }

      int color = this.palette[this.borderColor];
      for (int y = 96; y < 128; y++) {
        for (int x = 0; x < 128; x++) {
          this.pixels[(x + y * 128)] = color;
        }
      }

      int borderPixels = 100;
      reds += (color & 0xFF0000) * borderPixels;
      greens += (color & 0xFF00) * borderPixels;
      blues += (color & 0xFF) * borderPixels;

      reds = reds / (12288 + borderPixels) & 0xFF0000;
      greens = greens / (12288 + borderPixels) & 0xFF00;
      blues = blues / (12288 + borderPixels) & 0xFF;
      this.lightColor = (int)(reds | greens | blues);
    }
  }

  public void tick60hz() {
    if (this.startDelay > 0) this.startDelay -= 1; 
  }

  public void setPixels(int[] pixels)
  {
    this.pixels = pixels;
  }

  public int getLightColor() {
    return this.lightColor;
  }
}