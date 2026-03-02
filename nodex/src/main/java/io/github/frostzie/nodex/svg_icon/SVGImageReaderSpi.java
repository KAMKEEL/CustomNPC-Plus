package io.github.frostzie.nodex.svg_icon;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Locale;

public class SVGImageReaderSpi extends ImageReaderSpi {

    protected static final String[] FORMAT_NAMES = {"svg", "SVG"};
    protected static final String[] EXTENSIONS = {"svg"};
    protected static final String[] MIME_TYPES = {"image/svg", "image/x-svg", "image/svg+xml", "image/svg-xml"};

    public SVGImageReaderSpi() {
        super(
            "jsvgfx",
            "1.0",
            FORMAT_NAMES,
            EXTENSIONS,
            MIME_TYPES,
            "jsvgfx.SVGImageReader",
            new Class<?>[]{ImageInputStream.class},
            null,
            false,
            null,
            null,
            null,
            null,
            true,
            null,
            null,
            null,
            null
        );
    }

    @Override
    public String getDescription(Locale locale) {
        return "Scalable Vector Graphics (SVG) format image reader";
    }

    @Override
    public boolean canDecodeInput(Object input) throws IOException {
        return input instanceof ImageInputStream iis && canDecode(iis);
    }

    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new SVGImageReader(this);
    }

    //=========================================================================

    @SuppressWarnings("StatementWithEmptyBody")
    protected boolean canDecode(ImageInputStream iis) throws IOException {
        try {
            iis.mark();

            int b;
            while (Character.isWhitespace((char) (b = iis.read()))) {
                // skip leading whitespaces
            }

            return b == '<';
        } catch (EOFException ignore) {
            return false;
        } finally {
            iis.reset();
        }
    }
}
