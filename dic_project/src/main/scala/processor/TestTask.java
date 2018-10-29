package processor;

import java.io.ByteArrayInputStream;
import java.io.File;

import javafx.scene.image.Image;
import javafx.concurrent.*;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;


public class TestTask extends Task<Image>{

    private String directory = null;
    private String videoPath = null;

    public TestTask(String directory, String videoPath) {
        this.directory = directory;
        this.videoPath = videoPath;
    }

    @Override
    public Image call() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println(videoPath);
        VideoCapture camera = new VideoCapture();
        camera.open(videoPath);
        System.out.println("Video loaded!");
        if (camera.isOpened()) {
            System.out.println("File open succeed!");
            Mat frame = new Mat();
            while (camera.read(frame)) {
                MatOfByte buffer = new MatOfByte();
                Imgcodecs.imencode(".jpg", frame, buffer);
                updateValue(new Image(new ByteArrayInputStream(buffer.toArray())));
//                Thread.sleep(100);
            }
        } else {
            System.out.println("File open failed!");
        }

        return null;
    }

}
