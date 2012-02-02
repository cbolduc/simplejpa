package com.spaceprogram.simplejpa.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.PersistenceException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.spaceprogram.simplejpa.PersistenceProviderImpl;

/**
 * Represent the information given by the user in a persistence.xml file
 * for a persistence unit. 
 */
public class PersistenceUnitInformation
{
    private static PersistenceUnitInformation extractPersistenceUnit(
            Element element)
    {
        PersistenceUnitInformation persistenceUnitInfo = new PersistenceUnitInformation();
        persistenceUnitInfo.setName(element.getAttribute("name"));
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element subelement = (Element) children.item(i);
                String tag = subelement.getTagName();
                if (tag.equals("provider")) {
                    persistenceUnitInfo.setPersistenceProvider(extractTextFrom(subelement));
                } else if (tag.equals("class")) {
                    persistenceUnitInfo.getExplicitClasses().add(extractTextFrom(subelement));
                } else if (tag.equals("jar-file")) {
                    persistenceUnitInfo.getExplicitJarFiles().add(extractTextFrom(subelement));
                }
            }
        }
        return persistenceUnitInfo;
    }

    public static String extractTextFrom(
            Element element)
    {
        if (element != null) {
            StringBuilder text = new StringBuilder("");
            NodeList children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.TEXT_NODE) {
                    text.append(children.item(i).getNodeValue());
                }
            }
            return text.toString().trim();
        } else {
            return null;
        }
    }

    /**
     * Find the information for the specified persistence unit in all available persistence.xml file.
     * @param persistenceUnitName The name of the persistence unit.
     * @return The information for this persistence unit.
     */
    public static PersistenceUnitInformation foundPersistenceUnitInfoFor(
            String persistenceUnitName)
    {
        try {
            Enumeration<URL> persistenceXmlUrls = Thread.currentThread().getContextClassLoader()
                .getResources("META-INF/persistence.xml");
            while (persistenceXmlUrls.hasMoreElements()) {
                URL url = persistenceXmlUrls.nextElement();
                List<PersistenceUnitInformation> persistenceUnitInfos = loadPersistenceXmlFile(url);
                for (PersistenceUnitInformation persistenceUnitInfo : persistenceUnitInfos) {
                    if (isCorrectPersistenceUnitProvider(persistenceUnitInfo)) {
                        return persistenceUnitInfo;
                    }
                }
            }
            return null;
        } catch (Exception exc) {
            throw new PersistenceException("Unable to load the persistence unit info for " + persistenceUnitName, exc);
        }
    }

    private static boolean isCorrectPersistenceUnitProvider(
            PersistenceUnitInformation persistenceUnitInfo)
    {
        return persistenceUnitInfo.getPersistenceProvider() == null
                || PersistenceProviderImpl.class.getName().equalsIgnoreCase(
                    persistenceUnitInfo.getPersistenceProvider());
    }

    /**
     * Load the information of all persistence units defined in a supplied persistence.xml file.
     * @param url The URL of the persistence.xml file.
     * @return A list of information of all persistence units defined in the persistence.xml file.
     */
    private static List<PersistenceUnitInformation> loadPersistenceXmlFile(
            URL url)
    {
        ArrayList<PersistenceUnitInformation> persistenceUnitInfos = new ArrayList<PersistenceUnitInformation>();
        try {
            Document doc = loadXmlDocument(url);
            Element top = doc.getDocumentElement();

            NodeList children = top.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) children.item(i);
                    String tag = element.getTagName();
                    if (tag.equals("persistence-unit")) {
                        persistenceUnitInfos.add(extractPersistenceUnit(element));
                    }
                }
            }
        } catch (Exception exc) {
            //TODO: Should log here but currently we do not take action.
        }
        return persistenceUnitInfos;
    }

    private static Document loadXmlDocument(
            URL url)
        throws IOException,
            ParserConfigurationException,
            SAXException
    {
        InputStream is = null;
        if (url != null) {
            URLConnection conn = url.openConnection();
            conn.setUseCaches(false); //avoid JAR locking on Windows and Tomcat
            is = conn.getInputStream();
        }
        if (is == null) {
            throw new PersistenceException("Failed to read the persistence.xml file: " + url);
        }

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setValidating(false);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

        Document doc = docBuilder.parse(new InputSource(is));
        return doc;
    }

    private String name;

    private String persistenceProvider;

    private Set<String> explicitClasses = new HashSet<String>();

    private Set<String> explicitJarFiles = new HashSet<String>();

    public Set<String> getExplicitClasses()
    {
        return explicitClasses;
    }

    public Set<String> getExplicitJarFiles()
    {
        return explicitJarFiles;
    }

    public String getName()
    {
        return name;
    }

    public String getPersistenceProvider()
    {
        return persistenceProvider;
    }

    public void setExplicitClasses(
            Set<String> explicitClasses)
    {
        this.explicitClasses = explicitClasses;
    }

    public void setExplicitJarFiles(
            Set<String> explicitJarFiles)
    {
        this.explicitJarFiles = explicitJarFiles;
    }

    public void setName(
            String name)
    {
        this.name = name;
    }

    public void setPersistenceProvider(
            String persistenceProvider)
    {
        this.persistenceProvider = persistenceProvider;
    }
}
