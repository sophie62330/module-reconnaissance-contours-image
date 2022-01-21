
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
class FindContours {
    private Mat srcGray = new Mat();
    private JFrame frame;
    private JLabel imgSrcLabel;
    private JLabel imgContoursLabel;
    private static final int MAX_THRESHOLD = 255;
    private int threshold = 100;
    private Random rng = new Random(12345);

    private int nbCFU;
    private int nbAutresContours;

    public int getNbCFU() {
        return nbCFU;
    }

    public void setNbCFU(int nbCFU) {
        this.nbCFU = nbCFU;
    }

    public int getNbAutresContours() {
        return nbAutresContours;
    }

    public void setNbAutresContours(int nbAutresContours) {
        this.nbAutresContours = nbAutresContours;
    }



    public FindContours(String[] args) {
        String filename=args.length>0 ? args[0] : "C:\\Users\\Administrateur\\Desktop\\Formation java\\projet fil rouge\\photos\\photos benoit\\2 ok.jpg";

        //String filename=url;

        Mat src = Imgcodecs.imread(filename);
        if (src.empty()) {
            System.err.println("Cannot read image: " + filename);
            System.exit(0);
        }
        Imgproc.cvtColor(src, srcGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(srcGray, srcGray, new Size(3, 3));
        // Create and set up the window.
        frame = new JFrame("Finding contours");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Set up the content pane.
        Image img = HighGui.toBufferedImage(src);
        addComponentsToPane(frame.getContentPane(), img);
        // Use the content pane's default BorderLayout. No need for
        // setLayout(new BorderLayout());
        // Display the window.
        frame.pack();
        frame.setVisible(true);
        update();
    }
    private void addComponentsToPane(Container pane, Image img) {
        if (!(pane.getLayout() instanceof BorderLayout)) {
            pane.add(new JLabel("Container doesn't use BorderLayout!"));
            return;
        }
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
        sliderPanel.add(new JLabel("Canny threshold: "));
        JSlider slider = new JSlider(0, MAX_THRESHOLD, threshold);
        slider.setMajorTickSpacing(20);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                threshold = source.getValue();
                update();
            }
        });
        sliderPanel.add(slider);
        pane.add(sliderPanel, BorderLayout.PAGE_START);
        JPanel imgPanel = new JPanel();
        imgSrcLabel = new JLabel(new ImageIcon(img));
        imgPanel.add(imgSrcLabel);
        Mat blackImg = Mat.zeros(srcGray.size(), CvType.CV_8U);
        imgContoursLabel = new JLabel(new ImageIcon(HighGui.toBufferedImage(blackImg)));
        imgPanel.add(imgContoursLabel);
        pane.add(imgPanel, BorderLayout.CENTER);
    }
    private void update() {
        Mat cannyOutput = new Mat();
        Imgproc.Canny(srcGray, cannyOutput, threshold, threshold * 2);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        System.out.println("nb contours total : "+contours.size());

        int mediane=calculerMediane(contours);
        System.out.println("mediane : "+mediane);

        List<MatOfPoint> contoursEloignesDeLaMediane=getListeContoursEloignes(contours,mediane);

        //supprimerContoursAbberrants(contours,listeIndicesContoursASupprimer);
        int compteur=0;
        Mat drawing = Mat.zeros(cannyOutput.size(), CvType.CV_8UC3);
        System.out.println("nombre de CFU trouvés : "+contours.size());
        System.out.println("nombre de contours anormaux trouvés : "+contoursEloignesDeLaMediane.size());

        this.setNbCFU(contours.size());
        this.setNbAutresContours(contoursEloignesDeLaMediane.size());

        for (int u = 0; u < contoursEloignesDeLaMediane.size(); u++) {
            Scalar color = new Scalar(120,120,120);
            Imgproc.drawContours(drawing, contoursEloignesDeLaMediane, u, color, 1, Imgproc.LINE_8, hierarchy, 0, new Point());
            compteur++;
        }

        for (int i = 0; i < contours.size(); i++) {
            Scalar color = new Scalar(256, 18, 27);
            Imgproc.drawContours(drawing, contours, i, color, 1, Imgproc.LINE_8, hierarchy, 0, new Point());
            compteur++;
        }

        imgContoursLabel.setIcon(new ImageIcon(HighGui.toBufferedImage(drawing)));
        imgContoursLabel.setText("nombre de CFU trouvés : "+contours.size()+" - nombre autres contours : "+contoursEloignesDeLaMediane.size());
        frame.repaint();
    }


    public List<MatOfPoint> getListeContoursEloignes(List<MatOfPoint> contours,int mediane){
        List<MatOfPoint> listeResult=new ArrayList<MatOfPoint>();
        List<Integer> listeContoursTriee = trierListePlusPetitAuPlusGrandContour(contours);

        int plusPetitEcart=mediane - listeContoursTriee.get(0);
        if (plusPetitEcart> listeContoursTriee.get(listeContoursTriee.size()-1)-mediane){
            plusPetitEcart=listeContoursTriee.get(listeContoursTriee.size()-1)-mediane;
        }

        System.out.println("plus petit écart : "+plusPetitEcart);

        ///////////finir
        for (int i=0;i<=contours.size()-1;i++){
            //System.out.println("taille "+i+" : "+contours.get(i).toArray().length);
            if (Math.abs(mediane-contours.get(i).toArray().length)>1.2*plusPetitEcart){
                listeResult.add(contours.get(i));
                contours.remove(i);
            }
        }

        return listeResult;
    }

    public Integer calculerMediane(List<MatOfPoint> contours){
        int mediane=-1;
        if (contours.size()!=0){
            List<Integer> listeContoursTriee = trierListePlusPetitAuPlusGrandContour(contours);
            System.out.println(listeContoursTriee.toString());
            int tailleListe = listeContoursTriee.size();

            if (listeContoursTriee.size() % 2 == 0) {

                mediane = (listeContoursTriee.get(tailleListe / 2) + listeContoursTriee.get(tailleListe / 2 - 1)) / 2;
            } else {
                mediane = listeContoursTriee.get(tailleListe/2);
            }
        }

        return mediane;
    }

    public List<Integer> trierListePlusPetitAuPlusGrandContour(List<MatOfPoint> listeATrier){
        List<Integer> listeResult=new ArrayList<Integer>();
        List<MatOfPoint> listeBisATrier=new ArrayList<MatOfPoint>();
        for (int j=0;j<listeATrier.size();j++){
            listeBisATrier.add(listeATrier.get(j));
        }

        int taillePlusPetitContour;
        int indicePlusPetitContour;
        while (listeBisATrier.size()>0){
            taillePlusPetitContour=trouverTaillePlusPetitContour(listeBisATrier);
            indicePlusPetitContour=trouverIndiceEnFonctionDeLaTaille(listeBisATrier,taillePlusPetitContour);
            listeResult.add(taillePlusPetitContour);
            listeBisATrier.remove(indicePlusPetitContour);
        }

        return listeResult;
    }

    public Integer trouverIndiceEnFonctionDeLaTaille(List<MatOfPoint> contours,Integer taille){
        for (int i=0;i<contours.size();i++){
            if (contours.get(i).toArray().length==taille){
                return i;
            }
        }

        return -1;
    }

    public Integer trouverTaillePlusPetitContour(List<MatOfPoint> contours){
        int resultat=contours.get(0).toArray().length;
        for (int i = 0;i<contours.size();i++){
            if (resultat>contours.get(i).toArray().length){
                resultat=contours.get(i).toArray().length;
            }
        }

        return resultat;
    }


    public static void main(String[] args) {
        // Load the native OpenCV  library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // Schedule a job for the event dispatch thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FindContours(args);
            }
        });
    }



}
