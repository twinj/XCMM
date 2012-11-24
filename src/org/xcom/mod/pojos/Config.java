package org.xcom.mod.pojos;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.xcom.mod.gui.XCMGUI;

/**
 * 
 * @author Daniel Kemp
 */
@XmlRootElement(name = "Config")
@XmlType(propOrder = {
        "author", "xcomPath", "unpackedPath"
})
public class Config implements Serializable, ModXml {

    private static final long serialVersionUID = 1L;

    private final static String AUTHOR = "Author";
    private final static String XCOM_PATH = "XCom";
    private final static String UNPACKED_PATH = "Unpacked";
    public final static String PATH = "config\\config.xml";

    private final static String DEFAULT_XCOM_PATH = "C:\\Program Files (x86)\\Steam\\SteamApps\\common\\XCom-Enemy-Unknown";
    private final static String DEFAULT_UNPACKED_PATH = System.getProperty("user.dir") + "\\unpacked";
    private final static String DEFAULT_COOKED_PATH = "C:\\Program Files (x86)\\Steam\\SteamApps\\common\\XCom-Enemy-Unknown\\XComGame\\CookedPCConsole";
    //private final static String DEFAULT_COOKED_BACKUP = "";

    private String author = "unknown";
    private String xcomPath = DEFAULT_XCOM_PATH;
    private String unpackedPath = DEFAULT_UNPACKED_PATH;

    // Clean constructor required for serialisation
    public Config() {

    }

    @XmlElement(name = AUTHOR)
    public String getAuthor() {

        return author;
    }

    public void setAuthor(String author) {

        this.author = author;
    }

    @XmlElement(name = XCOM_PATH)
    public String getXcomPath() {

        return xcomPath;
    }

    public void setXcomPath(String xcomPath) {

        this.xcomPath = xcomPath;
    }

    @XmlElement(name = UNPACKED_PATH)
    public String getUnpackedPath() {

        return Paths.get(unpackedPath).toString();
    }

    public void setUnpackedPath(String unpackedPath) {

        this.unpackedPath = unpackedPath;
    }

    @XmlTransient
    public Path getCookedPath() {

        return Paths.get(DEFAULT_COOKED_PATH).toAbsolutePath();
    }

    @Override
    public Path getBasePath() {

        return getDir();
    }

    @Override
    @XmlTransient
    public Path getXmlSavePath() {

        return getFilePath();
    }

    public static Path getModPath() {

        return Paths.get("mods");
    }

    @Override
    public String getPrintName() {

        return XCMGUI.GUI_NAME + ": MAIN CONFIG";
    }

    public static Path getDir() {

        return Paths.get("config").toAbsolutePath();
    }

    public static Path getFilePath() {

        return Paths.get(PATH);
    }
}
