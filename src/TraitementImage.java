import java.awt.image.BufferedImage;

public class TraitementImage {
    private String url;
    private int nbCFU;
    private BufferedImage photoOriginale;
    private BufferedImage photoFinale;

    public TraitementImage() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getNbCFU() {
        return nbCFU;
    }

    public void setNbCFU(int nbCFU) {
        this.nbCFU = nbCFU;
    }



    public BufferedImage getPhotoFinale() {
        return photoFinale;
    }

    public void setPhotoFinale(BufferedImage photoFinale) {
        this.photoFinale = photoFinale;
    }

    public void lancerTraitement(String url) throws Exception {
        this.setUrl(url);
        //on passe les param
        String[] args=new String[1];
        args[0]=this.getUrl();
        FindContours.test(args);
        this.setNbCFU(FindContours.getNbCFU());
        this.setPhotoFinale(FindContours.getImageFinale());
    }
}
