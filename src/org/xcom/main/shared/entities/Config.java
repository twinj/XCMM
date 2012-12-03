package org.xcom.main.shared.entities;

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
        "author", "xcomPath", "unpackedPath", "compressorPath", "extractorPath"
})
public class Config implements Serializable, ModXml {

    private static final long serialVersionUID = 1L;

    private final static String AUTHOR = "Author";
    private final static String XCOM_PATH = "XCom";
    private final static String UNPACKED_PATH = "Unpacked";
    private static final String EXTRACTOR_PATH = "Extractor";
		private static final String COMPRESSOR_PATH = "Compressor";

    private final static String DEFAULT_XCOM_PATH = "C:\\Program Files (x86)\\Steam\\SteamApps\\common\\XCom-Enemy-Unknown";
    private final static String DEFAULT_UNPACKED_PATH = System.getProperty("user.dir") + "\\unpacked";
    private final static String DEFAULT_COOKED_PATH = "C:\\Program Files (x86)\\Steam\\SteamApps\\common\\XCom-Enemy-Unknown\\XComGame\\CookedPCConsole";
    private final static String DEFAULT_COMPRESSOR_PATH = System.getProperty("user.dir") + "\\tools\\decompress.exe";
    private final static String DEFAULT_EXTRACTOR_PATH = System.getProperty("user.dir") + "\\tools\\extract.exe";
    //private final static String DEFAULT_COOKED_BACKUP = "";
		
    private String author = "unknown";
    private String xcomPath = DEFAULT_XCOM_PATH;
    private String unpackedPath = DEFAULT_UNPACKED_PATH;
    private String compressorPath = DEFAULT_COMPRESSOR_PATH;
    private String extractorPath = DEFAULT_EXTRACTOR_PATH;

    public final static String PATH = "config\\config.xml";

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
    
    @XmlElement(name = COMPRESSOR_PATH)
    public String getCompressorPath() {
			return this.compressorPath;
		}

		public void setCompressorPath(String compressorPath) {
			this.compressorPath = compressorPath;
		}

    @XmlElement(name = EXTRACTOR_PATH)
		public String getExtractorPath() {
			return this.extractorPath;
		}

		public void setExtractorPath(String extractorPath) {
			this.extractorPath = extractorPath;
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
