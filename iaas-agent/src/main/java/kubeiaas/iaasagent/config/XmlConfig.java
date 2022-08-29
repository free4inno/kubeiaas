package kubeiaas.iaasagent.config;

import kubeiaas.common.bean.IpUsed;
import kubeiaas.common.bean.Volume;
import kubeiaas.common.constants.bean.VolumeConstants;
import kubeiaas.common.enums.volume.VolumeFormatEnum;
import kubeiaas.common.utils.VmCUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 此类中操作xml文件的类为W3C中的dom，可以很好地实现对xml文件的随机存取.
 * 2020/9/27 14:55.
 * 1.0
 */
@Slf4j
@Service
public class XmlConfig {

    @Resource
    LibvirtConfig libvirtConfig;

    public XmlConfig() {
    }

    public String getVolumeDevice(Volume volume) {
        log.info("getVolumeDevice ---- start ----");
        String res = libvirtConfig.volumeToDisk(volume);
        log.info("getVolumeDevice ---- end ----");
        return res;
    }

    public String getIpDevice(IpUsed ip) {
        log.info("getIpDevice ---- start ----");
        String res = libvirtConfig.ipToNetwork(ip);
        log.info("getIpDevice ---- end ----");
        return res;
    }

    public String modifyXml(String originXML, int newCores, int newMemory) {
        log.info("modifyXml ---- start ----");
        //1、判断newCores是否为空，不为空再进行修改之后返回String类型的修改结果
        String resultXml = null;
        Document document = stringToDocument(originXML);
        if (newCores > 0) {
            String coreString = String.valueOf(VmCUtils.memUnitConvert(newCores));
            Node cpu = document.getElementsByTagName("cpu").item(0);
            Element cpuElement = (Element) cpu;
            Node topology = cpuElement.getElementsByTagName("topology").item(0);
            NamedNodeMap topologyNodeMap = topology.getAttributes();
            Node cores = topologyNodeMap.getNamedItem("cores");
            cores.setTextContent(coreString);
        }
        if (newMemory > 0) {
            String memoryString = String.valueOf(newMemory);
            Node memory = document.getElementsByTagName("memory").item(0);
            Node currentMemory = document.getElementsByTagName("currentMemory").item(0);
            Element memoryElement = (Element) memory;
            Element currentMemoryElement = (Element) currentMemory;
            memoryElement.setTextContent(memoryString);
            currentMemoryElement.setTextContent(memoryString);
        }
        resultXml = documentToString(document);
        log.info("modifyXml ---- end ----");
        return resultXml;
    }

    //
    public String attachDisk(String originXML, Volume volume) {
        log.info("attachDisk ---- start ----");
        //1、根据volume信息生成一段xml信息，然后插入到originXml disk之后
        String resultXml = null;
        Document document = stringToDocument(originXML);
        if (volume != null) {
            Node nodeDrivers = document.getElementsByTagName("devices").item(0);
            String volumeType = getVolumeDiskDevice(volume.getFormatType());
            Element newDiskElement = document.createElement("disk");
            newDiskElement.setAttribute("type", "file");
            newDiskElement.setAttribute("device", volumeType);
            if (volumeType.equals(VolumeConstants.VOLUME_DEVICE_CDROM)) {
                Element driverElement = document.createElement("driver");
                driverElement.setAttribute("name", "qemu");
                driverElement.setAttribute("type", "raw");
                newDiskElement.appendChild(driverElement);
            } else {
                Element driverElement = document.createElement("driver");
                driverElement.setAttribute("name", "qemu");
                driverElement.setAttribute("type", "qcow2");
                newDiskElement.appendChild(driverElement);
            }
            Element sourceElement = document.createElement("source");
            sourceElement.setAttribute("file", VolumeConstants.DEFAULT_NFS_SRV_PATH + volume.getProviderLocation());
            newDiskElement.appendChild(sourceElement);
            Element targetElement = document.createElement("target");
            sourceElement.setAttribute("dev", volume.getMountPoint());
            sourceElement.setAttribute("bus", volume.getBus());
            newDiskElement.appendChild(targetElement);
            nodeDrivers.appendChild(newDiskElement);
            resultXml = documentToString(document);
        }
        log.info("attachDisk ---- end ----");
        return resultXml;
    }

    public String detachDisk(String originXML, String uuid) {
        log.info("detachDisk ---- start ----");
        String resultXml = null;
        Document document = stringToDocument(originXML);
        if (uuid != null) {
            Node devicesNode = document.getElementsByTagName("devices").item(0);
            NodeList diskList = document.getElementsByTagName("disk");
            for (int i = 0; i < diskList.getLength(); i++) {
                Element disk = (Element) diskList.item(i);
                Node source = disk.getElementsByTagName("source").item(0);
                NamedNodeMap sourceAttributes = source.getAttributes();
                Node file = sourceAttributes.getNamedItem("file");
                Pattern p = Pattern.compile(uuid);
                Matcher m = p.matcher(file.getTextContent());
                while (m.find()) {
                    devicesNode.removeChild(disk);
                    resultXml = documentToString(document);
                }
            }
        }
        log.info("detachDisk ---- end ----");
        return resultXml;
    }

    public Document stringToDocument(String xml) {
        log.info("stringToDocument ---- start ---- xml");
        StringReader stringReader = new StringReader(xml);
        InputSource inputSource = new InputSource(stringReader);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(inputSource);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        log.info("stringToDocument ---- end ----");
        return document;
    }

    public String documentToString(Document doc) {
        log.info("documentToString ---- start ---- xml");
        // XML转字符串
        String xmlStr = "";
        try {
            DOMSource source = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(source, result);
            xmlStr = writer.getBuffer().toString();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        log.info("documentToString ---- end ----");
        return xmlStr;
    }

    private String getVolumeDiskDevice(VolumeFormatEnum volumeType) {
        log.info("getVolumeDiskDevice ---- start ----");
        if (volumeType.equals(VolumeFormatEnum.ISO)) {
            log.info("getVolumeDiskDevice ---- end ---- ISO");
            return VolumeConstants.VOLUME_DEVICE_CDROM;
        }
        log.info("getVolumeDiskDevice ---- end ---- not ISO");
        return VolumeConstants.VOLUME_DEVICE_DISK;
    }

}
