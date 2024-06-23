package kubeiaas.common.utils;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

public class XmlDescUtils {
    public static Optional<String> getDomainOsBootDevice(String xmlDesc) {
        SAXBuilder builder = new SAXBuilder();
        try {
            Document document = builder.build(new StringReader(xmlDesc));
            Element root = document.getRootElement();
            Element os = root.getChild("os");
            Element boot = os.getChild("boot");
            String dev = boot.getAttributeValue("dev");
            if (dev == null) {
                return Optional.empty();
            } else {
                return Optional.of(dev);
            }
        } catch (JDOMException | IOException e) {
            return Optional.empty();
        }
    }
}
