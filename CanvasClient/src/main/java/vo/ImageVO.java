package vo;

import javafx.scene.image.Image;

public class ImageVO {

    private Image image;

    private String imageName;

    private Long modifiedTime;

    public ImageVO(Image image, String imageName, Long modifiedTime) {
        this.image = image;
        this.imageName = imageName;
        this.modifiedTime = modifiedTime;
    }

    public Image getImage() {
        return image;
    }

    public String getImageName() {
        return imageName;
    }

    public Long getModifiedTime() {
        return modifiedTime;
    }

}
