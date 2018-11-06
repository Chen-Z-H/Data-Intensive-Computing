package bean;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import scala.reflect.io.Streamable;

import java.util.Base64;

public class DataPackage {

    private long time = 0;
    private double rate = 0;
    private double height = 0;
    private double width = 0;
    private double fourcc = 0;
    private String frame = null;

    public DataPackage(long time, double rate, double height, double width, double fourcc, String frame) {
        this.time = time;
        this.rate = rate;
        this.height = height;
        this.width = width;
        this.fourcc = fourcc;
        this.frame = frame;
    }

    public Mat decodeToMat() {
        byte[] arrayBuffer = Base64.getDecoder().decode(frame);
        Mat container = new Mat(1, arrayBuffer.length, CvType.CV_8U);
        container.put(0, 0, arrayBuffer);
        return Imgcodecs.imdecode(container, 1);
    }

    public static Mat decodeToMat(String str) {
        byte[] arrayBuffer = Base64.getDecoder().decode(str);
        Mat container = new Mat(1, arrayBuffer.length, CvType.CV_8U);
        container.put(0, 0, arrayBuffer);
        return Imgcodecs.imdecode(container, 1);
    }

    public static String encodeToString(Mat mat) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", mat, buffer);
        return Base64.getEncoder().encodeToString(buffer.toArray());
    }

    public byte[] getFrameBytes() {
        return frame.getBytes();
    }

    public double getFourcc() {
        return fourcc;
    }

    public void setFourcc(double fourcc) {
        this.fourcc = fourcc;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getFrame() {
        return frame;
    }

    public void setFrame(String frame) {
        this.frame = frame;
    }

}
