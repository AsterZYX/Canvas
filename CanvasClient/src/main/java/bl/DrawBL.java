package bl;

import blservice.DrawBLService;
import com.sun.javafx.tk.Toolkit;
import enums.ResultMessage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import org.opencv.core.Point;
import vo.ImageVO;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.utils.Converters;

public class DrawBL implements DrawBLService {

    //原图保存路径
    private final static String SAVEPATH = "CanvasClient/images";
    //检测结果路径
    private final static String SAVE_MARK_PATH = "CanvasClient/marks";
    private final static String fileSeparator = System.getProperty("file.separator");

    //更新保存的原图和检测结果
    @Override
    public ResultMessage updatePic(String picName, Image image, Image markImage) {
        if(image!=null) {
            ResultMessage re = update(picName, image, false);
            if(re != ResultMessage.SUCCESS){
                return re;
            }
        }
        if(markImage!=null) {
            ResultMessage re = update(picName, markImage, true);
            if(re != ResultMessage.SUCCESS){
                return re;
            }
        }

        return ResultMessage.SUCCESS;
    }

    //保存原图和检测结果
    @Override
    public ResultMessage savePic(String fileName, Image image, Image markImage) {
        if(image!=null) {
            ResultMessage re = save(fileName, image, false);
            if (re != ResultMessage.SUCCESS) {
                return re;
            }
        }

        if(markImage!=null) {
            ResultMessage re = save(fileName, markImage, true);
            if (re != ResultMessage.SUCCESS) {
                return re;
            }
        }

        return ResultMessage.SUCCESS;
    }

    //通过图片名称得到原图或检测结果图
    @Override
    public Image getPicByName(String picName, boolean isMark) {
        try {
            FileInputStream input = new FileInputStream(SAVEPATH + fileSeparator + picName + ".png");
            if(isMark) {
                File file = new File(SAVE_MARK_PATH + fileSeparator + picName + ".png");
                if(!file.exists()){
                    return null;
                }
                input = new FileInputStream(SAVE_MARK_PATH + fileSeparator + picName + ".png");
            }
            Image image = new Image(input);
            input.close();
            return image;
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    //识别图中的图形
    @Override
    public Image recognizeShape(WritableImage image) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        BufferedImage img = SwingFXUtils.fromFXImage(image, null);
        try {
            Mat src = BufferedImage2Mat(img);
            Mat dst = src.clone();
            Imgproc.cvtColor(dst, dst, Imgproc.COLOR_BGRA2GRAY);
            //阈值图
            Imgproc.adaptiveThreshold(dst, dst, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                    Imgproc.THRESH_BINARY_INV, 3, 3);
            //轮廓
            ArrayList<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(dst, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            for (int i=0;i<contours.size();i++){
                MatOfPoint2f matOfPoint2f = new MatOfPoint2f(contours.get(i).toArray());
                String shape = detectDetail(contours.get(i), matOfPoint2f);

                //求图形质心
                Moments mom =  Imgproc.moments(contours.get(i), false);
                int cx = (int) (mom.get_m10() / mom.get_m00() - shape.length()*6);
                int cy = (int) (mom.get_m01() / mom.get_m00());
                //画轮廓
                Imgproc.drawContours(src, contours, i, new Scalar(0, 255, 0, 0), 2);
                //显示文字
                Imgproc.putText(src, shape, new Point(cx, cy), Core.FONT_HERSHEY_SIMPLEX, 0.8 ,new  Scalar(255));
            }

            return SwingFXUtils.toFXImage(Mat2BufferedImage(src), null);

        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    //将BuffedImage转为Mat
    private static Mat BufferedImage2Mat(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
    }

    //将Mat转为BufferedImage
    public static BufferedImage Mat2BufferedImage(Mat matrix)throws IOException {
        MatOfByte mob=new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mob);
        return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
    }

    //检测轮廓为何图形
    private String detectDetail(MatOfPoint mp,MatOfPoint2f mp2f){
        //轮廓总长度
        double peri = Imgproc.arcLength(mp2f, true);
        //对图像轮廓点进行多边形拟合
        MatOfPoint2f polyShape = new MatOfPoint2f();
        Imgproc.approxPolyDP(mp2f,polyShape,0.04*peri,true);
        int lenCount = polyShape.toArray().length;
        switch (lenCount) {
            case 3:
                return "triangle";
            case 4:
                Point[] points = polyShape.toArray();
                double cosine1 = calculateCos(points[0],points[1],points[2]);
                double cosine2 = calculateCos(points[1],points[2],points[3]);
                double cosine3 = calculateCos(points[2],points[3],points[0]);
                double cosine4 = calculateCos(points[3],points[0],points[1]);
                double maxCos = Math.max(Math.max(Math.max(cosine1, cosine2), cosine3), cosine4);
                if (maxCos < 0.2) {
                    Rect rect = Imgproc.boundingRect(mp);
                    float width = rect.width;
                    float height = rect.height;
                    float ratio = width / height;
                    //计算宽高比，判断是矩形还是正方形
                    if (ratio >= 0.90 && ratio <= 1.1) {
                        return "square";
                    } else {
                        return "rectangle";
                    }
                }
                else {
                    return "quadrangle";
                }
            case 5:
                return "pentagon";
            default:
                return "circle";
        }
    }

    //计算cos值
    private static double calculateCos(Point A, Point B, Point C){
        double a = calculateDis(B,C);
        double c = calculateDis(A,B);
        double b = calculateDis(A,C);
        return (a*a+c*c-b*b)/(2*a*c);
    }

    //计算两点间距离
    private static double calculateDis(Point A, Point B){
        return Math.sqrt((A.x-B.x)*(A.x-B.x)+(A.y-B.y)*(A.y-B.y));
    }

    //保存单张图片
    private ResultMessage save(String fileName, Image image, boolean isMark){
        try {
            String path = SAVEPATH;
            if (isMark) {
                path = SAVE_MARK_PATH;
            }
            File dir = new File(path);

            if (!dir.exists() || !dir.isDirectory()) {
                dir.mkdirs();
            }

            File outFile = new File(path + fileSeparator + fileName + ".png");
            if (outFile.exists()) {
                return ResultMessage.NAME_DEPULICATE;
            } else {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", outFile);
            }

            return ResultMessage.SUCCESS;
        } catch (IOException e){
            e.printStackTrace();
        }

        return ResultMessage.FAIL;
    }

    //更新单张图片
    private ResultMessage update(String fileName, Image image, boolean isMark){
        String path = SAVEPATH;
        if(isMark){
            path = SAVE_MARK_PATH;
        }
        File file = new File(path + fileSeparator + fileName + ".png");
        if(file.exists()) {
            boolean result = file.delete();
            if (!result) {
                return ResultMessage.FAIL;
            }
        }
        ResultMessage re = save(fileName, image, isMark);
        return re;
    }
}
