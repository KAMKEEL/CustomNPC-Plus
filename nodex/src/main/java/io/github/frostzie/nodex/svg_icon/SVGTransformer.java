package io.github.frostzie.nodex.svg_icon;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;

public class SVGTransformer {

    public static final String SVG_WIDTH_EXPR = "/svg/@width";
    public static final String SVG_HEIGHT_EXPR = "/svg/@height";

    protected final XPath xPath = XPathFactory.newInstance().newXPath();
    protected final Document document;

    public SVGTransformer(String path) {
        try {
            var builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = builder.parse(path);
        } catch (Exception e) {
            throw new SVGException(e.getMessage(), e);
        }
    }

    public SVGTransformer setWidth(int width) {
        return setNodeValue(SVG_WIDTH_EXPR, String.valueOf(width));
    }

    public SVGTransformer setHeight(int height) {
        return setNodeValue(SVG_HEIGHT_EXPR, String.valueOf(height));
    }

    public SVGTransformer setNodeValue(String xpathExpression, String value) {
        try {
            var nodes = (NodeList) xPath.evaluate(xpathExpression, document, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                nodes.item(i).setNodeValue(value);
            }
        } catch (XPathExpressionException e) {
            throw new SVGException(e.getMessage(), e);
        }

        return this;
    }

    public InputStream toInputStream() {
        try {
            var transformer = TransformerFactory.newInstance().newTransformer();

            try (var bos = new ByteArrayOutputStream()) {
                var result = new StreamResult(bos);
                transformer.transform(new DOMSource(document), result);

                return new ByteArrayInputStream(bos.toByteArray());
            }
        } catch (Exception e) {
            throw new SVGException(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        try {
            var transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");

            var writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));

            return writer.getBuffer().toString();
        } catch (Exception e) {
            throw new SVGException(e.getMessage(), e);
        }
    }
}
