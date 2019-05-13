package bl;

import blservice.PageBLService;
import enums.ResultMessage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import net.coobird.thumbnailator.Thumbnails;
import vo.ImageVO;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.Buffer;
import java.util.ArrayList;

public class PageBL implements PageBLService {

    //原图保存路径
    private final static String SAVEPATH = "CanvasClient/images";
    //检测图保存路径
    private final static String SAVE_MARK_PATH = "CanvasClient/marks";
    private final static String fileSeparator = System.getProperty("file.separator");

    //得到所有保存的原图
    @Override
    public ArrayList<ImageVO> getAllPics() {
        ArrayList<ImageVO> result = new ArrayList<>();

        File dir = new File(SAVEPATH);
        File[] files = dir.listFiles();
        try {
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
//                    FileInputStream input = new FileInputStream(files[i].getPath());
//                    ImageVO imageVO = new ImageVO(new Image(input), files[i].getName().substring(0,files[i].getName().lastIndexOf(".")), files[i].lastModified());
//                    result.add(imageVO);
//                    input.close();

                    BufferedImage image = ImageIO.read(files[i]);
                    BufferedImage thumbnail = Thumbnails.of(image)
                            .scale(0.25f)
                            .asBufferedImage();
                    WritableImage writableImage = SwingFXUtils.toFXImage(thumbnail, null);
                    ImageVO imageVO = new ImageVO(writableImage, files[i].getName().substring(0,files[i].getName().lastIndexOf(".")), files[i].lastModified());
                    result.add(imageVO);
                }
            }
        } catch (IOException ex){
            ex.printStackTrace();
        }
        return result;
    }

    //删除原图及其检测图
    @Override
    public ResultMessage deletePic(String picName) {
        File file = new File(SAVEPATH+fileSeparator+picName+".png");
        boolean result = file.delete();
        if(!result){
            return ResultMessage.FAIL;
        }
        File markFile = new File(SAVE_MARK_PATH+fileSeparator+picName+".png");
        if(markFile.exists()){
            boolean re = markFile.delete();
            if(!re){
                return ResultMessage.FAIL;
            }
        }

        return ResultMessage.SUCCESS;
    }
}
