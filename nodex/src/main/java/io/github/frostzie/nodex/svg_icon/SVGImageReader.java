package io.github.frostzie.nodex.svg_icon;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;


public class SVGImageReader extends ImageReader {

    protected SVGImage svgImage;

    protected SVGImageReader(SVGImageReaderSpi spi) {
        super(spi);
    }

    @Override
    public int getNumImages(boolean allowSearch) {
        return 1;
    }

    @Override
    public int getWidth(int imageIndex) {
        return svgImage != null ? svgImage.getWidth() : 0;
    }

    @Override
    public int getHeight(int imageIndex) {
        return svgImage != null ? svgImage.getHeight() : 0;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) {
        return Collections.singletonList(
            ImageTypeSpecifier.createFromBufferedImageType(SVGImage.BUFFERED_IMAGE_TYPE)
        ).iterator();
    }

    @Override
    public IIOMetadata getStreamMetadata() {
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) {
        return null;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) {
        Dimension size = null;

        if (param != null) {
            size = param.getSourceRenderSize();
        }

        if (size == null) {
            size = new Dimension(getWidth(imageIndex), getHeight(imageIndex));
        }

        if (svgImage == null) {
            throw new SVGException("You must set input before reading the image");
        }

        return svgImage.toBufferedImage((int) size.getWidth(), (int) size.getHeight());
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        super.setInput(input, true, true);

        try {
            if (input instanceof ImageInputStream iis) {
                svgImage = SVGImage.of(iis);
            } else {
                throw new SVGException("Unexpected input type: " + input.getClass().getCanonicalName());
            }
        } catch (IOException e) {
            throw new SVGException(e.getMessage(), e);
        }
    }

    @Override
    public ImageReadParam getDefaultReadParam() {
        return new SVGImageReadParam();
    }

    @Override
    public void dispose() {
        super.dispose();
        svgImage = null;
    }

    //=========================================================================

    protected static final class SVGImageReadParam extends ImageReadParam {

        @Override
        public boolean canSetSourceRenderSize() {
            // Image cannot be adjusted to the available frame size here
            // https://mail.openjdk.org/pipermail/openjfx-dev/2025-July/055365.html
            return false;
        }
    }
}
