package processor;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import scala.reflect.io.Streamable;

public class DataPackage {

    private long time = 0;
    private int rate = 0;
    private int height = 0;
    private int width = 0;
    private String frame = null;

    public DataPackage(long time, int rate, int height, int width, String frame) {
        this.time = time;
        this.rate = rate;
        this.height = height;
        this.width = width;
        this.frame = frame;
    }

    public Mat decodeToMat() {
        Mat container = new Mat(1, frame.length(), CvType.CV_8U);
        container.put(0, 0, frame.getBytes());
        return Imgcodecs.imdecode(container, 1);
    }

    public byte[] getFrameBytes() {
        return frame.getBytes();
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public String getFrame() {
        return frame;
    }

    public void setFrame(String frame) {
        this.frame = frame;
    }

}
