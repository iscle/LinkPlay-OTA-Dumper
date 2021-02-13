import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;

public class Main {

    private static final String BASE_URL = "http://silenceota.linkplay.com/";

    public static void main(String[] args) {

        try (InputStream is = new URL(BASE_URL).openConnection().getInputStream()) {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();
            NodeList contents = doc.getElementsByTagName("Contents");

            ArrayList<Firmware> firmwares = new ArrayList<>();
            for (int i = 0; i < contents.getLength(); i++) {
                Node n = contents.item(i);
                NodeList child = n.getChildNodes();
                String key = null;
                String lastModified = null;
                for (int j = 0; j < child.getLength(); j++) {
                    Element e = (Element) child.item(j);
                    if (e.getTagName().equals("Key")) {
                        key = e.getTextContent();
                    } else if (e.getTagName().equals("LastModified")) {
                        lastModified = e.getTextContent();
                    }
                    if (key != null && lastModified != null) break;
                }
                firmwares.add(new Firmware(key, lastModified));
            }

            firmwares.sort(Comparator.comparing(o -> o.lastModified));

            for (Firmware f : firmwares) {
                if (!f.key.contains("wifi_audio_image") || !f.key.contains("uImage")
                        || (!f.lastModified.contains("2020-") && !f.lastModified.contains("2021-"))) continue;

                System.out.println(BASE_URL + f.key + ", " + f.lastModified);
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
    }

    public static class Firmware {
        String key;
        String lastModified;

        public Firmware(String key, String lastModified) {
            this.key = key;
            this.lastModified = lastModified;
        }
    }
}
