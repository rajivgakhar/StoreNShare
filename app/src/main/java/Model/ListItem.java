package Model;


public class ListItem {
    private String image;
    private String imgId;
    private String ownerId;

    public String getOwnerId() {
        return ownerId;
    }

    public ListItem(String image, String imgId, String ownerId) {
        this.image = image;
        this.imgId = imgId;

        this.ownerId = ownerId;
    }


    public String getImgId() {
        return imgId;
    }

    public ListItem(String image) {
        this.image=image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }
}
