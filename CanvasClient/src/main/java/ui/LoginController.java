package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class LoginController {

    private MainUIController mainUIController;

    @FXML
    private Label openIcon;

    @FXML
    private Label newIcon;

    @FXML
    private Label minusButton;

    @FXML
    private Label closeButton;

    @FXML
    private void initialize(){
        openIcon.setText("\ue644");
        newIcon.setText("\ue664");
        minusButton.setText("\ue729");
        closeButton.setText("\ue723");
    }

    public void setMainUIController(MainUIController mainUIController){
        this.mainUIController = mainUIController;
    }

    //点击最小化按钮
    @FXML
    private void clickMinusButton(){
        mainUIController.minimizeWindow();
    }

    //点击关闭按钮
    @FXML
    private void clickCloseButton(){
        mainUIController.closeWindow();
    }

    //点击创建按钮
    @FXML
    private void clickCreatePane(){
        mainUIController.showDrawBoard(null);
    }

    //点击打开按钮
    @FXML
    private void clickOpenPane(){
        mainUIController.showMyPage();
    }

}
