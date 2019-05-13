package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import vo.ImageVO;

import java.util.Date;

public class PageCellController {

    private PageController pageController;

    @FXML
    private ImageView pic;

    @FXML
    private Label picName;

    @FXML
    private Label picTime;

    @FXML
    private Label deleteButton;

    @FXML
    private void initialize(){
        deleteButton.setText("\ue634");
        deleteButton.setVisible(false);
        pic.setSmooth(true);
    }

    public void setPageController(PageController pageController){
        this.pageController = pageController;
    }

    //设置图片信息
    public void setCellInfo(ImageVO imageVO){
        Image image = imageVO.getImage();
        pic.setImage(image);
        picName.setText(imageVO.getImageName());
        picTime.setText(new Date(imageVO.getModifiedTime()).toString());
    }

    //设置删除按钮是否可见
    public void setDeleteButton(boolean isVisible){
        deleteButton.setVisible(isVisible);
    }

    //点击打开按钮
    @FXML
    private void clickOpenButton(){
        pageController.clickOpenButton(picName.getText());
    }

    //点击删除按钮
    @FXML
    private void clickDeleteButton(){
        pageController.clickDeleteButton(picName.getText());
    }
}
