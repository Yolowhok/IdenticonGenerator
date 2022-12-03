import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args) throws NoSuchAlgorithmException  {
        String s1 = "okey.exe@yandex.ru";
        display(generateIdent(s1, false));
    }

    private static JFrame frame;
    private static JLabel label;
    public static void display(BufferedImage image){
        if(frame==null){
            frame=new JFrame();
            frame.setTitle("stained_image");
            frame.setSize(image.getWidth(), image.getHeight());
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            label=new JLabel();
            label.setIcon(new ImageIcon(image));
            frame.getContentPane().add(label, BorderLayout.CENTER);
            frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setVisible(true);
        }else label.setIcon(new ImageIcon(image));
    }
    public static BufferedImage generateIdent(String text, boolean type) throws NoSuchAlgorithmException {
        int image_width = 400;
        int image_height = 400;
        int width = 16;
        int height = 16;
        int red=0;
        int green=0;
        int blue=0;
        String s1 = sha1(text);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = image.getRaster();

        String r = strBinary(s1, type);

        for (int i = 0; i < 8; i++) {
            red += r.charAt(16+i) << i;
            green += r.charAt(24+i) << i;
            blue += r.charAt(32+i) << i;
        }

        int [] background = new int[] {blue, green, blue, 25};
        int [] color = new int [] {red, green, blue, 255};

        int x = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++, x++) {
                x = x==r.length() ? x-1 : x;        //костыль, если выполнять вторым алгоритмом, то длина r может быть меньше x
                if (r.charAt(x)=='1') {
                    raster.setPixel(j, i, color);
                }
                else {
                    raster.setPixel(j,i, background);
                }
            }
        }
        BufferedImage finalImage = new BufferedImage(image_width, image_height, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale(image_width/width, image_height/height);
        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        finalImage = op.filter(image, finalImage);
        return joinImage(finalImage);
    }
    private static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
    private static BufferedImage joinImage(BufferedImage image) {
        BufferedImage finalImage = new BufferedImage(image.getWidth()*2, image.getHeight()*2, BufferedImage.TYPE_INT_ARGB);
        finalImage.getGraphics().drawImage(image, 0, 0, null);
        finalImage.getGraphics().drawImage(affineFlip(image, '|'), image.getWidth(), 0, null);
        finalImage.getGraphics().drawImage(affineFlip(image, '\\'), image.getWidth(), image.getHeight(), null);
        finalImage.getGraphics().drawImage(affineFlip(image, '-'), 0, image.getHeight(), null);
        return finalImage;
    }
    private static BufferedImage affineFlip(BufferedImage image, char parametr) {
        AffineTransform tx;
        AffineTransformOp op;
        if (parametr=='\\') {
            tx = AffineTransform.getScaleInstance(-1, -1);
            tx.translate(-image.getWidth(), -image.getHeight());
            op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            return op.filter(image, null);
        } else if (parametr == '|') {
            tx = AffineTransform.getScaleInstance(-1,1);
            tx.translate(-image.getWidth(), 0);
            op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            return op.filter(image, null);
        }
        else if (parametr == '-') {
            tx = AffineTransform.getScaleInstance(1, -1);
            tx.translate(0, -image.getHeight());
            op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            return op.filter(image, null);
        }
        return null;
    }
    private static String strBinary(String s1, boolean type) {
        StringBuilder str = new StringBuilder();
        if (type) {
            for (byte element : s1.getBytes()) {
                str.append(String.format("%8s", Integer.toBinaryString(element & 0xFF)).replace(' ', '0'));
            }
            return str.toString();
        }
        else {
            for (byte element: s1.getBytes()) {
                str.append(Integer.toBinaryString(element));
            }
            return str.toString();
        }
    }
}