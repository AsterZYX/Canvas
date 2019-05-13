package blservice;

import enums.ResultMessage;
import vo.ImageVO;

import java.util.ArrayList;

public interface PageBLService {

    public ArrayList<ImageVO> getAllPics();

    public ResultMessage deletePic(String picName);
}
