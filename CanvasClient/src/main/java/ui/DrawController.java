package ui;

import bl.DrawBL;
import blservice.DrawBLService;
import com.jfoenix.controls.*;
import enums.DrawStyle;
import enums.ResultMessage;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.util.*;

public class DrawController {

    private MainUIController mainUIController;

    private GraphicsContext gc;

    private Stack<WritableImage> undoStack;

    private Stack<WritableImage> redoStack;

    private DrawBLService drawBLService;

    @FXML
    private Canvas myCanvas;

    @FXML
    private AnchorPane mainPane;

    @FXML
    private JFXButton changeButton;

    private JFXButton pencil, edit, clear, identify;

    private static final String FX_BUTTON_STYLE = "-fx-text-fill:WHITE; -fx-font-family:iconfont; -fx-font-size: 20";
    private static final String ANIMATED_OPTION_BUTTON = "animated-option-button";
    private static final String ANIMATED_OPTION_SUB_BUTTON = "animated-option-sub-button";
    private static final String ANIMATED_OPTION_SUB_BUTTON2 = "animated-option-sub-button2";
    private static final String ANIMATED_OPTION_SUB_BUTTON3 = "animated-option-sub-button3";
    private static final String ANIMATED_OPTION_SUB_BUTTON4 = "animated-option-sub-button4";

    private DrawStyle drawStyle = DrawStyle.MOUSE;
    private double startX, startY, initX, initY;
    //是否是开始画线的第一个点
    private boolean lineFlag = false;
    //是否为新创建文件
    private boolean isNew = true;
    //是否做过修改
    private boolean hasModified = false;
    //是否已被检测过
    private boolean isDetected = false;
    //图片名称
    private String picName;
    //原图
    private Image originalImage = null;
    //检测图
    private Image markImage = null;

    @FXML
    private void initialize(){
        drawBLService = new DrawBL();

        gc = myCanvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        initializeToolMenu();
        changeButton.setText("\ue693");
        changeButton.setTooltip(new Tooltip("切换原图/识别图"));
        changeButton.setVisible(false);

        undoStack = new Stack<>();
        WritableImage initImg = myCanvas.snapshot(new SnapshotParameters(), null);
        undoStack.push(initImg);
        gc.drawImage(initImg, 0,0);

        redoStack = new Stack<>();
    }

    public void setMainUIController(MainUIController mainUIController){
        this.mainUIController = mainUIController;
    }

    //初始化工具栏
    private void initializeToolMenu(){
        JFXButton mouse = new JFXButton();
        Label mouseLabel = new Label("\ue69a");
        mouseLabel.setStyle(FX_BUTTON_STYLE);
        mouse.setGraphic(mouseLabel);
        mouse.setButtonType(JFXButton.ButtonType.RAISED);
        mouse.getStyleClass().addAll(ANIMATED_OPTION_BUTTON, ANIMATED_OPTION_SUB_BUTTON);
        mouse.setTooltip(new Tooltip("路径选择工具"));
        mouse.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                drawStyle = DrawStyle.MOUSE;
            }
        });

        pencil = new JFXButton();
        Label pencilLabel = new Label("\ue602");
        pencilLabel.setStyle("-fx-text-fill:WHITE; -fx-font-family:iconfont; -fx-font-size: 15");
        pencil.setGraphic(pencilLabel);
        pencil.setButtonType(JFXButton.ButtonType.RAISED);
        pencil.getStyleClass().addAll(ANIMATED_OPTION_BUTTON, ANIMATED_OPTION_SUB_BUTTON2);
        pencil.setTooltip(new Tooltip("绘制工具"));

        JFXButton straightLine = new JFXButton();
        Label straightLineLabel = new Label("\ue62e");
        straightLineLabel.setStyle(FX_BUTTON_STYLE);
        straightLine.setGraphic(straightLineLabel);
        straightLine.setButtonType(JFXButton.ButtonType.RAISED);
        straightLine.getStyleClass().addAll(ANIMATED_OPTION_BUTTON, ANIMATED_OPTION_SUB_BUTTON2);
        straightLine.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                drawStyle = DrawStyle.LINE;
            }
        });
        straightLine.setTooltip(new Tooltip("直线"));

        JFXButton curve = new JFXButton();
        Label curveLabel = new Label("\ue600");
        curveLabel.setStyle(FX_BUTTON_STYLE);
        curve.setGraphic(curveLabel);
        curve.setButtonType(JFXButton.ButtonType.RAISED);
        curve.getStyleClass().addAll(ANIMATED_OPTION_BUTTON, ANIMATED_OPTION_SUB_BUTTON2);
        curve.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                drawStyle = DrawStyle.FREE;
            }
        });
        curve.setTooltip(new Tooltip("曲线"));

        JFXNodesList lineTool = new JFXNodesList();
        lineTool.setSpacing(10);
        // init nodes
        addRotateAnimationNode(lineTool, pencil);
        pencil.setOpacity(1);
        lineTool.addAnimatedNode(straightLine);
        lineTool.addAnimatedNode(curve);
        lineTool.setRotate(270);

        edit = new JFXButton();
        Label editLabel = new Label("\ue645");
        editLabel.setStyle(FX_BUTTON_STYLE);
        edit.setGraphic(editLabel);
        edit.setButtonType(JFXButton.ButtonType.RAISED);
        edit.getStyleClass().addAll(ANIMATED_OPTION_BUTTON, ANIMATED_OPTION_SUB_BUTTON4);
        edit.setTooltip(new Tooltip("编辑"));

        JFXButton undo = new JFXButton();
        Label undoLabel = new Label("\ue607");
        undoLabel.setStyle(FX_BUTTON_STYLE);
        undo.setGraphic(undoLabel);
        undo.setButtonType((JFXButton.ButtonType.RAISED));
        undo.getStyleClass().addAll(ANIMATED_OPTION_BUTTON, ANIMATED_OPTION_SUB_BUTTON4);
        undo.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                undo();
            }
        });
        undo.setTooltip(new Tooltip("撤销"));

        JFXButton redo = new JFXButton();
        Label redoLabel = new Label("\ue608");
        redoLabel.setStyle(FX_BUTTON_STYLE);
        redo.setGraphic(redoLabel);
        redo.setButtonType((JFXButton.ButtonType.RAISED));
        redo.getStyleClass().addAll(ANIMATED_OPTION_BUTTON, ANIMATED_OPTION_SUB_BUTTON4);
        redo.setTooltip(new Tooltip("重做"));
        redo.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                redo();
            }
        });

        JFXNodesList editTool = new JFXNodesList();
        addRotateAnimationNode(editTool, edit);
        edit.setOpacity(1);
        editTool.addAnimatedNode(undo);
        editTool.addAnimatedNode(redo);
        editTool.setRotate(270);
        editTool.setSpacing(10);

        JFXButton save = new JFXButton();
        Label saveLabel = new Label("\ue628");
        saveLabel.setStyle(FX_BUTTON_STYLE);
        save.setGraphic(saveLabel);
        save.setButtonType((JFXButton.ButtonType.RAISED));
        save.getStyleClass().addAll(ANIMATED_OPTION_BUTTON, ANIMATED_OPTION_SUB_BUTTON);
        save.setTooltip(new Tooltip("保存"));
        save.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                clickSaveButton();
            }
        });

        clear = new JFXButton();
        Label clearLabel = new Label("\ue6c3");
        clearLabel.setStyle(FX_BUTTON_STYLE);
        clear.setGraphic(clearLabel);
        clear.setButtonType((JFXButton.ButtonType.RAISED));
        clear.getStyleClass().addAll(ANIMATED_OPTION_BUTTON, ANIMATED_OPTION_SUB_BUTTON);
        clear.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                gc.clearRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());
                hasModified = true;
            }
        });
        clear.setTooltip(new Tooltip("清空"));

        identify = new JFXButton();
        Label identifyLabel = new Label("\ue603");
        identifyLabel.setStyle(FX_BUTTON_STYLE);
        identify.setGraphic(identifyLabel);
        identify.setButtonType(JFXButton.ButtonType.RAISED);
        identify.getStyleClass().addAll(ANIMATED_OPTION_BUTTON, ANIMATED_OPTION_SUB_BUTTON);
        identify.setTooltip(new Tooltip("图像识别"));
        identify.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                clickIdentifyButton();
            }
        });

        JFXButton home = new JFXButton();
        Label homeLabel = new Label("\ue601");
        homeLabel.setStyle(FX_BUTTON_STYLE);
        home.setGraphic(homeLabel);
        home.setButtonType(JFXButton.ButtonType.RAISED);
        home.getStyleClass().addAll(ANIMATED_OPTION_BUTTON, ANIMATED_OPTION_SUB_BUTTON);
        home.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                clickHomeButton();
            }
        });
        home.setTooltip(new Tooltip("主页"));

        JFXButton option = new JFXButton();
        Label optionLabel = new Label("\ue64e");
        optionLabel.setStyle(FX_BUTTON_STYLE);
        option.setGraphic(optionLabel);
        option.setButtonType((JFXButton.ButtonType.RAISED));
        option.getStyleClass().addAll(ANIMATED_OPTION_BUTTON, ANIMATED_OPTION_SUB_BUTTON3);
        option.setTooltip(new Tooltip("菜单"));

        JFXNodesList tool = new JFXNodesList();
        tool.setSpacing(10);

        addRotateAnimationNode(tool, option);
//        tool.addAnimatedNode(option);
        tool.addAnimatedNode(mouse);
        tool.addAnimatedNode(lineTool);
        tool.addAnimatedNode(editTool);
        tool.addAnimatedNode(save);
        tool.addAnimatedNode(clear);
        tool.addAnimatedNode(identify);
        tool.addAnimatedNode(home);

        mainPane.getChildren().add(tool);
        AnchorPane.setTopAnchor(tool, 40.0);
        AnchorPane.setLeftAnchor(tool, 15.0);
    }

    //鼠标点击事件
    @FXML
    private void onMouseClickedListener(MouseEvent e){
        switch (drawStyle) {
            case FREE:
                hasModified = true;
                gc.beginPath();
                break;
            case LINE:
                hasModified = true;
                if(!lineFlag){
                    startX = e.getX();
                    startY = e.getY();
                    initX = e.getX();
                    initY = e.getY();
//                    lastX = e.getX();
//                    lastY = e.getY();
                    gc.beginPath();
                    lineFlag = true;
                }
                else{
                    gc.strokeLine(startX, startY, e.getX(), e.getY());
                    WritableImage img = myCanvas.snapshot(new SnapshotParameters(), null);
                    undoStack.push(img);
                    if(getDistance(initX, initY, e.getX(), e.getY()) <= 20){
                        if(initX!=e.getX() || initY!=e.getY()){
                            gc.strokeLine(initX, initY, e.getX(), e.getY());
                        }
                        gc.closePath();
                        lineFlag = false;
                    }
                    startX = e.getX();
                    startY = e.getY();
                }
                break;
        }
    }

    //鼠标拖拽事件
    @FXML
    private void onMouseDraggedListener(MouseEvent e){
        switch (drawStyle) {
            case FREE:
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
                break;
        }

    }

    //鼠标移动事件
    @FXML
    private void onMousePressedListener(MouseEvent e){
        switch (drawStyle){
            case FREE:
                initX = e.getX();
                initY = e.getY();
                break;
        }
    }

    //鼠标释放事件
    @FXML
    private void onMouseReleasedListener(MouseEvent e){
        switch (drawStyle) {
            case FREE:
                if(initX!=e.getX() || initY!=e.getY()){
                    gc.strokeLine(initX,initY,e.getX(),e.getY());
                }
                gc.closePath();
                break;
        }
        if(drawStyle != DrawStyle.LINE){
            WritableImage img = myCanvas.snapshot(new SnapshotParameters(), null);
            undoStack.push(img);
        }
    }

    //撤销
    private void undo(){
        if(!undoStack.isEmpty()) {
            WritableImage temp = undoStack.pop();
            if(!undoStack.isEmpty()) {
                WritableImage img = undoStack.peek();
                gc.drawImage(img, 0, 0);
            }
            redoStack.push(temp);
            hasModified = true;
        }
    }

    //重做
    private void redo(){
        if(!redoStack.isEmpty()){
            WritableImage temp = redoStack.pop();
            gc.drawImage(temp, 0, 0);
            undoStack.push(temp);
            hasModified = true;
        }
    }

    //计算两点间距离
    private static double getDistance(double x1, double y1, double x2, double y2){
        return Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
    }

    //添加根节点及其动画
    private void addRotateAnimationNode(JFXNodesList list, JFXButton label){
        list.addAnimatedNode(label, (expanded, duration) -> {
            List<KeyFrame> frames = new ArrayList<>();
            frames.add(new KeyFrame(duration,
                    new KeyValue(label.rotateProperty(), 120, Interpolator.EASE_BOTH)
            ));
            frames.add(new KeyFrame(Duration.millis(duration.toMillis()),
                    new KeyValue(label.rotateProperty(), 240, Interpolator.EASE_BOTH)
            ));
            frames.add(new KeyFrame(Duration.millis(duration.toMillis() + 40),
                    new KeyValue(label.rotateProperty(), 360, Interpolator.EASE_BOTH)
            ));
            return frames;
        });
    }

    //点击保存按钮
    private void clickSaveButton(){
        if(isNew) {
            Dialog dialog = new Dialog();
            dialog.getDialogPane().getStylesheets().add("/style/dialog.css");
            dialog.setTitle("Save Dialog");
            dialog.setHeaderText(null);

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            BorderPane dialogPane = new BorderPane();
            dialogPane.setStyle("-fx-background-color: #ffffff");

            JFXTextField filename = new JFXTextField();
            filename.setPromptText("Please input the name of your file");
            filename.setPadding(new Insets(0, 30, 0, 30));
            filename.setStyle("-fx-font-size: 15px");

            dialogPane.setCenter(filename);

            Node OKButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
            OKButton.setDisable(true);

            filename.textProperty().addListener((observable, oldValue, newValue) -> {
                OKButton.setDisable(newValue.trim().isEmpty());
            });

            dialog.getDialogPane().setContent(dialogPane);

//        Platform.runLater(() -> filename.requestFocus());
//
//        dialog.setResultConverter(dialogButton -> {
//            if (dialogButton == ButtonType.OK) {
//                return filename.getText();
//            }
//            return null;
//        });

            Optional result = dialog.showAndWait();

            if (result.isPresent()) {
                if (result.get() == ButtonType.OK) {
                    Image img = myCanvas.snapshot(new SnapshotParameters(), null);
                    if(isDetected){
                        img = originalImage;
                    }
                    ResultMessage re = drawBLService.savePic(filename.getText(), img, markImage);
                    if(re == ResultMessage.NAME_DEPULICATE){
                        Dialog alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Canvas");
                        alert.setHeaderText("该文件名已存在！");
                        Node cancelButton = alert.getDialogPane().lookupButton(ButtonType.CANCEL);
                        if (cancelButton != null)
                            cancelButton.setId("CancelButton");
                        alert.getDialogPane().getStylesheets().add("/style/dialog.css");
                        alert.showAndWait();

//                        JFXSnackbar bar = new JFXSnackbar(mainPane);
//                        bar.enqueue(new JFXSnackbar.SnackbarEvent("该文件名已存在"));
                    }
                    else if(re == ResultMessage.FAIL){
                        Dialog alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Canvas");
                        alert.setHeaderText("保存文件失败！");
                        Node cancelButton = alert.getDialogPane().lookupButton(ButtonType.CANCEL);
                        if (cancelButton != null)
                            cancelButton.setId("CancelButton");
                        alert.getDialogPane().getStylesheets().add("/style/dialog.css");
                        alert.showAndWait();
                    }
                    else {
                        hasModified = false;
                    }
                }
            }
        }
        else{
            if(picName!=null && !picName.isEmpty()){
                Image img = myCanvas.snapshot(new SnapshotParameters(), null);
                if(isDetected){
                    img = originalImage;
                }
                ResultMessage re = drawBLService.updatePic(picName, img, markImage);
                if(re == ResultMessage.FAIL){
                    Dialog alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Canvas");
                    alert.setHeaderText("更新文件失败！");
                    Node cancelButton = alert.getDialogPane().lookupButton(ButtonType.CANCEL);
                    if (cancelButton != null)
                        cancelButton.setId("CancelButton");
                    alert.getDialogPane().getStylesheets().add("/style/dialog.css");
                    alert.showAndWait();
                }
                else{
                    hasModified = false;
                }
            }
        }
    }

    //点击识别按钮
    private  void clickIdentifyButton(){
        originalImage = myCanvas.snapshot(new SnapshotParameters(), null);
        Image reImage = drawBLService.recognizeShape(myCanvas.snapshot(new SnapshotParameters(), null));
        gc.drawImage(reImage, 0, 0);
        changeButton.setVisible(true);
        isDetected = true;
        markImage = reImage;
        drawStyle = DrawStyle.MOUSE;
        pencil.setDisable(true);
        edit.setDisable(true);
        clear.setDisable(true);
        identify.setDisable(true);
        hasModified = true;
    }

    //点击主页按钮
    private void clickHomeButton(){
        if(hasModified) {
            Dialog alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("您有新的修改未保存，确定要返回主页面吗？");
            alert.setTitle("Canvas");
            Node cancelButton = alert.getDialogPane().lookupButton(ButtonType.CANCEL);
            if (cancelButton != null)
                cancelButton.setId("CancelButton");
            alert.getDialogPane().getStylesheets().add("/style/dialog.css");
            Optional result = alert.showAndWait();


            if (result.isPresent()) {
                if (result.get() == ButtonType.OK) {
                    mainUIController.showLogin();
                }
            }
        }
        else {
            mainUIController.showLogin();
        }
    }

    //点击更改视图按钮
    @FXML
    private void clickChangeButton(){
        if(isDetected){
            if(originalImage!=null){
                gc.drawImage(originalImage,0, 0);
                isDetected = false;
                pencil.setDisable(false);
                edit.setDisable(false);
                clear.setDisable(false);
                identify.setDisable(false);
            }
        }
        else {
            if(markImage!=null){
                originalImage = myCanvas.snapshot(new SnapshotParameters(), null);
                gc.drawImage(markImage, 0, 0);
                isDetected = true;
                pencil.setDisable(true);
                edit.setDisable(true);
                clear.setDisable(true);
                identify.setDisable(true);
                drawStyle = DrawStyle.MOUSE;
            }
        }
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

    //若打开的为已保存图片，设置画板
    public void setCanvas(String picName){
        Image image = drawBLService.getPicByName(picName, false);
        gc.drawImage(image, 0, 0);
        originalImage = image;
        Image mark = drawBLService.getPicByName(picName, true);
        if(mark!=null){
            markImage = mark;
            changeButton.setVisible(true);
        }
        isNew = false;
        this.picName = picName;
    }
}
