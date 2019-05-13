package blservice;

import enums.ResultMessage;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import vo.ImageVO;

import java.util.ArrayList;

public interface DrawBLService {

    public ResultMessage savePic(String fileName, Image image, Image markImage);

    public Image getPicByName(String picName, boolean isMark);

    public ResultMessage updatePic(String picName, Image image, Image markImage);

    public Image recognizeShape(WritableImage image);

}
