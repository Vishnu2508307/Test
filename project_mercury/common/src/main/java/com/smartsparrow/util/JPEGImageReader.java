package com.smartsparrow.util;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;

/**
 * This class reads the input JPEG image file and converts CMYK color space to RGB color space.
 */
public class JPEGImageReader {

    /**
     * Reads a JPEG image file and returns a buffered image with RGB color space.
     * @param file containing JPEG image
     * @return a BufferedImage in the RGB color space
     * @throws IOException if there is error to read image from byte array
     * @throws ImageReadException if there is error to read an image
     */
    public BufferedImage readImage(File file) throws IOException, ImageReadException {
        //creating ImageInputStream of file to process the image
        ImageInputStream stream = ImageIO.createImageInputStream(file);
        Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);
        if (iter.hasNext()) {
            ImageReader reader = iter.next();
            reader.setInput(stream);

            BufferedImage image;
            ICC_Profile profile;
            try {
                //trying to read the input image with ImageReader
                image = reader.read(0);
            } catch (IIOException e) {
                //extract the ICC profile of the JPEG file when image read fails
                profile = Imaging.getICCProfile(file);
                //creating a WritableRaster object containing the raw pixel data from the image stream, without any color conversion applied.
                WritableRaster raster = (WritableRaster) reader.readRaster(0, null);
                //converting cmyk to rgb color space using Raster and ICC profile
                image = convertCmykToRgb(raster, profile);
            }
            return image;
        }
        return null;
    }

    /**
     * Creates a buffered image from a raster in the CMYK color space, converting the colors to RGB
     * using the provided CMYK ICC_Profile.
     * @param cmykRaster    A raster with (at least) 4 bands of samples.
     * @param cmykProfile   An ICC_Profile for conversion from the CMYK color space to the RGB color space.
     * If this parameter is null, a default profile is used.
     * @throws IOException if it fails to parse Generic CMYK profile file
     * @return a BufferedImage in the RGB color space.
     */
    public static BufferedImage convertCmykToRgb(Raster cmykRaster, ICC_Profile cmykProfile) throws IOException {
        if (cmykProfile == null) {
            cmykProfile = ICC_Profile.getInstance(JPEGImageReader.class.getResourceAsStream("/Generic_CMYK_Profile.icc"));
        }

        if (cmykProfile.getProfileClass() != ICC_Profile.CLASS_DISPLAY) {
            byte[] profileData = cmykProfile.getData();
            //Convert image to RGB using a simple conversion algorithm.
            if (profileData[ICC_Profile.icHdrRenderingIntent] == ICC_Profile.icPerceptual) {
                intToBigEndian(ICC_Profile.icSigDisplayClass, profileData, ICC_Profile.icHdrDeviceClass);

                cmykProfile = ICC_Profile.getInstance(profileData);
            }
        }

        ICC_ColorSpace cmykCS = new ICC_ColorSpace(cmykProfile);
        BufferedImage rgbImage = new BufferedImage(cmykRaster.getWidth(), cmykRaster.getHeight(), BufferedImage.TYPE_INT_RGB);
        WritableRaster rgbRaster = rgbImage.getRaster();
        ColorSpace rgbCS = rgbImage.getColorModel().getColorSpace();
        ColorConvertOp cmykToRgb = new ColorConvertOp(cmykCS, rgbCS, null);
        cmykToRgb.filter(cmykRaster, rgbRaster);
        return rgbImage;
    }

    static void intToBigEndian(int value, byte[] array, int index) {
        array[index]   = (byte) (value >> 24);
        array[index+1] = (byte) (value >> 16);
        array[index+2] = (byte) (value >>  8);
        array[index+3] = (byte) (value);
    }
}
