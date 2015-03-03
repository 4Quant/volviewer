package tipl.ij;

import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageWindow;
import tipl.util.ITIPLFileSystem;
import tipl.util.TIPLStorageManager;
import tipl.util.TypedPath;
import tipl.util.VirtualTypedPath;

import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mader on 12/4/14.
 */
public class ImageJTypedPath extends TypedPath.NonPosixTypedPath {

    @ITIPLFileSystem.FileSystemInfo(name="ImageJ Path", desc="a path stored in imagej windows")
    static public class IJP extends ITIPLFileSystem.WebPrefixFileSystem {

        public IJP() {
            super(false, "imagej");
        }
        @Override
        public boolean isValidPath(final String currentString) {
            if(super.isValidPath(currentString)) {
                String imagePath = currentString.split("://")[1];
                try {
                    ImagePlus ip = WindowManager.getImage(imagePath);
                    return true;
                } catch (Exception e) {
                    System.out.println("ImageJ Window:"+imagePath+" not found");
                }
                try {
                    ImagePlus ip = WindowManager.getImage(Integer.parseInt(imagePath));
                    return true;
                } catch (Exception e) {
                    System.out.println("ImageJ Window:"+imagePath+" not an integer");
                }
                return false;
            } else {
                return false;
            }
        }


        @Override
        protected TypedPath openPath(String prefix, String contents, String originalString) {
            try {
                ImagePlus ip = WindowManager.getImage(contents);
                return new ImageJTypedPath(ip);
            } catch (Exception e) {
                System.out.println("ImageJ Window:"+contents+" not found");
            }
            try {
                ImagePlus ip = WindowManager.getImage(Integer.parseInt(contents));
                return new ImageJTypedPath(ip);
            } catch (Exception e) {
                System.out.println("ImageJ Window:"+contents+" not an integer");
            }
            throw new IllegalArgumentException("ImageJ path:"+originalString+" could not be " +
                    "opened");

        }
    }

    protected ImagePlus ip;

    public ImageJTypedPath(final ImagePlus ip) {
        super(ip.getTitle(),"");
        this.ip =ip;
    }

    public ImageJTypedPath(int imageNo) {
        this(WindowManager.getImage(imageNo));
    }

    @Override
    public PATHTYPE getPathType() {
        return PATHTYPE.IMAGEJ;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public TypedPath changePath(String newPath) {
        return null;
    }

    //TODO implement getfileobject code
    @Override
    public FileObject getFileObject() {
        final ImagePlus cIP = this.ip;

        return new FileObject() {

            @Override
            public byte[] getData() {

                return new byte[0];
            }

            @Override
            public String[] getText() {
                return new String[0];
            }

            @Override
            public InputStream getInputStream() {
                return null;
            }

            @Override
            public OutputStream getOutputStream(boolean append) {
                return null;
            }
        };
    }

    @Override
    public TypedPath[] listFiles(PathFilter pf) {
        ImageStack subslices = ip.getImageStack();
        ImageJTypedPath[] outList = new ImageJTypedPath[subslices.getSize()];

        for(int i=0;i<subslices.getSize();i++) {
            outList[i]=new ImageJTypedPath(
                    new ImagePlus(
                            subslices.getSliceLabel(i),
                            subslices.getProcessor(i)
                    )
            );

        }
        return outList;
    }

    public static ImageJTypedPath[] listWindows() {
        ImageJTypedPath[] outList = new ImageJTypedPath[WindowManager.getImageCount()];

        int i = 0;
            for (int curImage : WindowManager.getIDList()) {
                outList[i]=new ImageJTypedPath(curImage);
                i++;
            }
        return outList;

    }
}
