package com.smartsparrow.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.batik.bridge.BridgeException;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.commons.imaging.ImageReadException;

import com.google.common.collect.Sets;
import com.google.common.io.BaseEncoding;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

/**
 * This class contains methods to operate with images: encode to/decode from base64 string, rescale.
 */
public class Images {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(Images.class);
    private static final Set<String> SUPPORTED_TYPES = Sets.newHashSet("image/jpg", "image/jpeg", "image/png", "image/gif");

    /**
     * Parses a base64 encoded string representing an image into {@link ImageData}
     *
     * @param image a base64 encoded string ("data:{mimeType};base64,{data}")
     * @return image data object (mime type + data)
     * @throws IllegalArgumentException if {@param image} has invalid format
     */
    public static ImageData parse(String image) throws IllegalArgumentException {

        Pattern pattern = Pattern.compile("(data:|;base64,)");
        String[] parts = pattern.split(image);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid format");
        }
        String mimeType = parts[1];
        String data = parts[2];

        if (mimeType == null || mimeType.isEmpty()) {
            throw new IllegalArgumentException("MimeType part is missing");
        }

        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data part is missing");
        }

        return new ImageData(mimeType, data);
    }

    /**
     * Re-scales an original image into new image with new width and height
     *
     * @param originalImage message to scale
     * @param width         width
     * @param height        height
     * @return rescaled image
     */
    public static BufferedImage rescaleImage(BufferedImage originalImage, int width, int height) {
        Image image = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage bi = new BufferedImage(image.getWidth(null), image.getHeight(null), originalImage.getType());
        bi.getGraphics().drawImage(image, 0, 0, null);
        return bi;
    }

    /**
     * Re-scales an original image into new PNG image with new width and height
     *
     * @param originalImage message to scale
     * @param width         width
     * @param height        height
     * @return rescaled image
     */
    public static BufferedImage rescaleImageToPng(BufferedImage originalImage, int width, int height) {
        Image image = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage bi = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        bi.getGraphics().drawImage(image, 0, 0, null);
        return bi;
    }

    /**
     * Reads a image file and return object containing its dimensions
     * @param imgFile must be a valid image file
     * @return pojo containing image dimensions
     * @throws IOException
     */
    private static ImageDimensions getImageDimensions(File imgFile) throws IOException {
        BufferedImage image = ImageIO.read(imgFile);
        return new ImageDimensions()
                .setHeight((double) image.getHeight())
                .setWidth((double) image.getWidth());
    }

    /**
     * Reads an image file of jpeg and return object containing its dimensions
     * @param imageFile must be a valid image file
     * @return pojo containing image dimensions
     * @throws IOException if there is an error to convert jpeg file to buffered image
     * @throws ImageReadException if there is an error to read image file
     */
    private static ImageDimensions getImageDimensionsForJpeg(File imageFile) throws IOException, ImageReadException {
        BufferedImage image = new JPEGImageReader().readImage(imageFile);
        return new ImageDimensions()
                .setHeight((double) image.getHeight())
                .setWidth((double) image.getWidth());
    }

    /**
     * Reads a image file and return object containing its dimensions. Support reading of svg files
     * @param imgFile must be a valid image file
     * @return pojo containing image dimensions
     * @throws IOException
     */
    public static ImageDimensions getImageDimensions(File imgFile, String mimeType) throws IOException, ImageReadException {
        if (mimeType.toLowerCase().contains("svg")) {
            return getImageDimensionsForSVG(imgFile);
        }
        if (mimeType.toLowerCase().contains("jpeg")) {
            return getImageDimensionsForJpeg(imgFile);
        }
        return getImageDimensions(imgFile);
    }

    /**
     * Writes the image into byte array and then encodes it to String
     *
     * @param im image to encode
     * @return a encoded string
     * @throws IOException if there is error to convert image to byte arrays
     */
    public static String writeToString(RenderedImage im, String contentType) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(im, getFormatName(contentType), output);
        return BaseEncoding.base64().encode(output.toByteArray());
    }

    /**
     * Reads an image from base64 encoded string
     *
     * @param image base64 encoded string
     * @return image object
     * @throws IOException              if there is error to read image from byte array
     * @throws IllegalArgumentException if there is error to decode string into byte array
     */
    public static BufferedImage readFromString(String image) throws IOException, IllegalArgumentException {
        byte[] originalData = BaseEncoding.base64().decode(image);
        return ImageIO.read(new ByteArrayInputStream(originalData));
    }

    /**
     * Verifies if provided content type is supported
     *
     * @param mimeType mime type to verify
     * @return {@code true} if mime type is supported, otherwise - {@code false}
     */
    public static boolean isValidMimeType(String mimeType) {
        return SUPPORTED_TYPES.contains(mimeType);
    }

    private static String getFormatName(String contentType) {
        switch (contentType) {
            case "image/png":
                return "png";
            case "image/jpeg": case "image/jpg":
                return "jpg";
            case "image/gif":
                return "gif";
            default:
                throw new IllegalArgumentException("Unsupported content type : " + contentType);
        }
    }

    public static class ImageData {
        private String mimeType;
        private String data;

        public ImageData(String mimeType, String data) {
            this.mimeType = mimeType;
            this.data = data;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getData() {
            return data;
        }
    }

    /**
     * Compare images pixel by pixel.
     * This is inefficient and there should be better ways to do it.
     *
     * Motivated because old tests were based on Base64 encoded byte arrays and somewhere in Java 9 upgrade the
     * PNG encoder that creates avatar thumbnails started to produce different Base64 encoded strings  between a
     * created thumbnail and the result of reading the reference file with IOUtils.readBytes.
     * TLDR: java 9 broke image encoded string comparison.
     *
     * @param imgA
     * @param imgB
     * @return
     */
    public static boolean isSameImage(BufferedImage imgA, BufferedImage imgB) {
        if(imgA.getWidth() !=  imgB.getWidth()) return false;
        if(imgA.getHeight() != imgB.getHeight()) return false;

        for (int y = 0; y < imgA.getHeight(); y++) {
            for (int x = 0; x < imgA.getWidth(); x++) {
                if(imgA.getRGB(x, y) != imgB.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Reads an image from svg files and return object containing its dimensions
     *
     * @param imgFile must be a valid image svg file
     * @return pojo containing image dimensions
     */
    private static ImageDimensions getImageDimensionsForSVG(File imgFile) {
        BufferedImage image;
        ImageDimensions dimensions = new ImageDimensions();
        try {
            image = readSVG(imgFile);
            dimensions
                    .setHeight((double) image.getHeight())
                    .setWidth((double) image.getWidth());
        } catch (Exception e) {
            log.warn(String.format("Error while processing %s the SVG image file", imgFile.getName()), e);
            dimensions
                    .setHeight((double) 0)
                    .setWidth((double) 0);
        }
        return dimensions;
    }

    /**
     * Returns a {@code BufferedImage} as the result from svg file.
     *
     * @param svgFile svg file
     * @return @BufferedImage
     * @throws IOException if there is error to convert svg file to buffered image
     */
    public static BufferedImage readSVG(File svgFile) throws IOException {
        final BufferedImage[] imagePointer = new BufferedImage[1];
        try {
            TranscoderInput input = new TranscoderInput(new FileInputStream(svgFile));
            ImageTranscoder imageTranscoder = new ImageTranscoder() {
                @Override
                public BufferedImage createImage(int width, int height) {
                    return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                }

                @Override
                public void writeImage(BufferedImage image, TranscoderOutput out) {
                    imagePointer[0] = image;
                }
            };
            imageTranscoder.transcode(input, null);
        } catch (TranscoderException ex) {
            throw new IOException("Error in converting " + svgFile);
        } catch (Exception e) {
            log.error("Error occurred while transcoding the image",e);
            throw new IOException("Error occurred while transcoding the image", e);
        }
        return imagePointer[0];
    }

    /**
     * Reads a icon image file and return object containing its dimensions. Support reading of svg files only
     * @param imgFile must be a valid image file
     * @return pojo containing icon image dimensions
     * @throws IOException
     */
    public static ImageDimensions getIconImageDimensions(File imgFile, String mimeType) throws IOException, ImageReadException {
        if (mimeType.toLowerCase().contains("svg")) {
            return getImageDimensionsForSVG(imgFile);
        }
        return getImageDimensions(imgFile);
    }
}
