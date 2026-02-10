package Character;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Animation {
    private BufferedImage[] frames;
    private int frameIndex;
    private int frameCount;
    private int frameDelay;
    private int frameDelayCounter = 0;

    public int x, y;

    public Animation(BufferedImage[] frames, int frameDelay) {
        this.frames = frames;
        this.frameDelay = frameDelay;
        this.frameCount = frames.length;
        this.frameIndex = 0;
    }

    public void update() {
        frameDelayCounter++;
        if (frameDelayCounter >= frameDelay)
        {
            frameIndex = (frameIndex + 1) % frameCount;
            frameDelayCounter = 0;
        }


    }

    public BufferedImage getCurrentFrame() {
        return frames[frameIndex];
    }


    public void setFrame(int index) {
        if (index >= 0 && index < frames.length) {
            frameIndex = index;
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }



}
