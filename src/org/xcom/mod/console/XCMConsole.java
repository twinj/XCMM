package org.xcom.mod.console;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.xcom.mod.Main;
import org.xcom.mod.exceptions.ConfigFileException;
import org.xcom.mod.exceptions.XmlSaveException;
import org.xcom.mod.gui.CopyFileException;
import org.xcom.mod.gui.XCMGUI;
import org.xcom.mod.pojos.Config;
import org.xcom.mod.pojos.HexEdit;
import org.xcom.mod.pojos.ModConfig;
import org.xcom.mod.pojos.ResFile;
import org.xcom.mod.pojos.XMod;
import org.xcom.mod.tools.exceptions.ExportFileAccessException;
import org.xcom.mod.tools.exceptions.UpkFileAccessException;
import org.xcom.mod.tools.exceptions.UpkFileNotDecompressedException;
import org.xcom.mod.tools.exceptions.UpkResourceNotFoundException;
import org.xcom.mod.tools.installer.Installer;
import org.xcom.mod.tools.installer.exceptions.SearchInterruptedException;
import org.xcom.mod.tools.installer.exceptions.UpkFileNotFoundException;
import org.xcom.mod.tools.maker.Maker;
import org.xcom.mod.tools.maker.exceptions.DetectUpkChangesException;
import org.xcom.mod.tools.maker.exceptions.ProcessFileChangesException;
import org.xcom.mod.tools.xshape.MHash;
import org.xcom.mod.tools.xshape.XShape;
import org.xcom.mod.tools.xshape.exceptions.CalculateHashException;
import org.xcom.mod.tools.xshape.exceptions.XModXmlAccessException;

/**
 * 
 * @author Anthony Surma
 * @author Daniel Kemp
 * 
 */
public class XCMConsole extends Main {
    
    // MAIN ENTRY POINT
    public static void main(String[] args) {
        
        print("STARTED", "");
        
        // Marshal helpers and Algorithms
        try {
            md = MessageDigest.getInstance(MHash.ALGORITHM);
            jc = JAXBContext.newInstance(Config.class, HexEdit.class, ModConfig.class,
                        ResFile.class, XMod.class);
            u = jc.createUnmarshaller();
            m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            
        } catch (Exception e) {
            print("FATAL ERROR: Could not create XCMM services.\n");
            e.printStackTrace(System.err);
            System.exit(Error.SYS_SERVICE_CREATE_FAIL.ordinal());
        }
        
        try {
            Path c = Config.getDir();
            if (Files.notExists(c)) {
                try {
                    Files.createDirectory(c);
                } catch (IOException e) {
                    print("FATAL ERROR: Could not create directory for config file.\n");
                    throw new ConfigFileException("IOException,createDirectory");
                }
                print("CREATED CONFIG DIRECTORY [\\config]", "");
            }
            
            c = Paths.get(Config.PATH);
            
            if (Files.exists(c)) {
                try {
                    config = (Config) u.unmarshal(c.toFile());
                } catch (JAXBException e) {
                    print("FATAL ERROR: Could not read/unmarshal config file.\n");
                    throw new ConfigFileException("JAXBException,unmarshal");
                }
            } else {
                config = new Config();
                print("CONFIG FILE NOT FOUND CREATING DEFAULT.", "");
            }
        } catch (ConfigFileException e) {
            e.printStackTrace(System.err);
            System.exit(Error.CONFIG_FILE_GET_ERROR.ordinal());
        }
        
        if (args.length == 0) {
            printUsage();
            System.exit(Error.NOTHING.ordinal());
        }
        
        if (args.length != 0) {
            String invoker = args[0];
            
            if (invoker.equals(INVOKE_GUI) && args.length == 1) {
            	IN_GUI = true;
                XCMGUI.run(true);
            } else try {
                XCMConsole.run(args);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
    
    /**
     * Run in command line mode.
     * 
     * @param args
     */
    public static void run(String[] args) {
        
        verfiyConfigForCommandLine();
        
        String invoker = args[0];
        
        if (invoker.equals(INVOKE_MAKE)) {
            printActionMessage("MAKE");
            String modName;
            ModConfig modConfig = null;
            
            // No args manual install
            if (args.length == 1) {
                modConfig = createModConfigFromUserInput(null);
                
                // Mod name supplied
            } else if (args.length == 2) {
                modName = args[1];
                
                Path path = ModConfig.getXmlSavePath(modName);
                
                if (Files.notExists(path)) {
                    modConfig = createModConfigFromUserInput(modName);
                } else {
                    try {
                        modConfig = (ModConfig) u.unmarshal(path.toFile());
                    } catch (JAXBException e) {
                        e.printStackTrace(System.err);
                        exit(Error.MOD_CONFIG_FILE_ERROR, "JAXBException ["
                                    + path.toString() + "]");
                    }
                }
                
                String unpacked = config.getUnpackedPath();
                List<String> editedFiles = modConfig.getOriginalFilePaths();
                
                // Generate edited files list if not yet made
                if (modConfig.getEditedFiles() == null) {
                    
                    List<File> files = new ArrayList<File>();
                    
                    for (String editedFile : editedFiles) {
                        
                        Path p = Paths.get(modConfig.getEditedFilesPath().toString(),
                                    Paths.get(editedFile).getFileName().toString());
                        
                        print(p.toString());
                        files.add(p.toFile());
                    }
                    modConfig.setEditedFiles(files);
                }
                
                // Generate edited files list if not yet made
                if (modConfig.getOriginalFiles() == null) {
                    
                    List<File> files = new ArrayList<File>();
                    
                    for (String editedFile : editedFiles) {
                        Path p = Paths.get(unpacked + editedFile);
                        files.add(p.toFile());
                    }
                    modConfig.setOriginalFiles(files);
                }
            }
            // Need to ensure we have file helper lists initialised
            try {
                new Maker(modConfig).runc();
            } catch (XmlSaveException e) {
                // TODO Auto-generated catch block
                e.printStackTrace(System.err);
            } catch (XModXmlAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace(System.err);
            } catch (ProcessFileChangesException e) {
                // TODO Auto-generated catch block
                e.printStackTrace(System.err);
            } catch (DetectUpkChangesException e) {
                // TODO Auto-generated catch block
                e.printStackTrace(System.err);
            } catch (CopyFileException e) {
                // TODO Auto-generated catch block
                e.printStackTrace(System.err);
            } catch (CalculateHashException e) {
                // TODO Auto-generated catch block
                e.printStackTrace(System.err);
            }
            
            // DO INSTALL
        } else if (invoker.equals(INVOKE_INSTALL)) {
            String modName = args[1];
            
            Path modFile = XMod.getExportPath(modName);
            
            if (Files.notExists(modFile)) {
                exit(Error.MOD_EXPORT_FILE_ERROR);
            }
            try {
                new Installer(modFile.toFile()).runc();
            } catch (UpkFileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace(System.err);
            } catch (UpkFileAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace(System.err);
            } catch (ExportFileAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace(System.err);
            } catch (SearchInterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace(System.err);
            } catch (UpkFileNotDecompressedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace(System.err);
            } catch (UpkResourceNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace(System.err);
            } catch (XmlSaveException e) {
                // TODO Auto-generated catch block
                e.printStackTrace(System.err);
            }
            
            // DO XSHAPE OF XCOM EXE
        } else if (invoker.equals(INVOKE_XSHAPE)) {
            
            printActionMessage("XSHAPE");
            Path configFile = Paths.get("XSHAPE.config");
            Path exeFile = Paths.get(config.getXcomPath(), RELATIVE_EXE_PATH);
            List<String> configLines = null;
            List<String> tocLines = null;
            List<Path> paths = null;
            
            if (Files.notExists(configFile) || Files.notExists(exeFile)
                        || !Files.isReadable(exeFile) || !Files.isWritable(exeFile)) {
                exit(Error.XSHAPE_CONFIG_ERROR);
            }
            
            try {
                configLines = Files.readAllLines(configFile, Charset.forName("UTF-8"));
                paths = new ArrayList<Path>();
                
                for (String line : configLines) {
                    
                    if (Files.notExists(Paths.get(config.getCookedPath().toString(),
                                line, COMPRESSED_UPK_EXT))) {} else {
                        configLines.remove(line);
                    }
                }
                
                if (configLines.isEmpty()) {
                    exit(Error.DEFAULT);
                }
                
                Path toc = Paths.get(config.getXcomPath(), RELATIVE_TOC_PATH);
                tocLines = Files.readAllLines(toc, Charset.forName("UTF-8"));
                
            } catch (IOException e) {
                e.printStackTrace();
                exit(Error.XSHAPE_FILE_READ_ERROR);
            }
            
            for (String fileName : configLines) {
                boolean valid = false;
                Path path = Paths.get(config.getCookedPath().toString(), fileName);
                if (!Files.isRegularFile(path)) {
                    exit(Error.DEFAULT);
                }
                
                for (String line : tocLines) {
                    if (line.toLowerCase().contains(fileName)) {
                        valid = true;
                        paths.add(path);
                        break;
                    }
                }
                if (!valid) exit(Error.DEFAULT);
            }
            
            for (String fileName : configLines) {
                Paths.get(fileName);
            }
            
            new Thread(new XShape(exeFile, paths)).start();
            
        } else {
            printUsage();
            exit(Error.PROGRAM_ARGUMENTS, "USAGE ERROR");
        }
    }
    
    /**
     * Verifies the configuration file of the program.
     */
    private static void verfiyConfigForCommandLine() {
        
        // GET AUTHOR
        if (config.getAuthor().equals("unknown")) {
            config.setAuthor(geStringFromUser("Please enter a valid Author name: "));
        }
        // GET XCOM PATH
        while (!isXComPathValid(config.getXcomPath())) {
            print("The system cannot verify your XCOM installion.\n");
            config.setXcomPath(getPathFromUser().toString());
        }
        print("The system has verfied your XCOM installion.\n");
        
        // GET UNPACKED PATH
        if (!isUnPackedPathValid(config.getUnpackedPath())) {
            print("The system cannot verify your unpacked resources.\n");
            config.setUnpackedPath(getPathFromUser().toString());
        }
        print("The system has verfied your unpacked resources.\n");
        try {
            printXml(config);
            saveXml(config);
        } catch (XmlSaveException e) {
            print("FATAL ERROR: Could not save config file to stream.\n");
            e.printStackTrace(System.err);
            exit(Error.CONFIG_FILE_SAVE_ERROR, "XmlSaveException");
        }
    }
    
    /**
     * Creates modConfig java object from user input.
     * 
     * @param modName
     * @return
     */
    private static ModConfig createModConfigFromUserInput(String modName) {
        
        ModConfig modConfig = new ModConfig();
        
        print("Need user input to create mod configuration.\n");
        print("MOD AUTHOR [", modConfig.getAuthor(), "] added to xml");
        
        if (modName == null) {
            modName = geStringFromUser("Please enter mod name: ");
            modConfig.setName(modName);
            
            print("MOD NAME [", modConfig.getName(), "] added to xml.");
        }
        modConfig.setAuthor(config.getAuthor());
        modConfig.setDescription(geStringFromUser("Please enter mod description: "));
        
        print("MOD DESCRIPTION [", modConfig.getDescription(), "] added to xml.");
        
        List<String> list = new ArrayList<String>();
        List<File> files = new ArrayList<File>();
        
        do {
            print("Please enter valid paths to ORIGINAL files.\n");
            Path p = getPathFromUser();
            files.add(p.toFile());
            list.add(p.toString().substring(config.getUnpackedPath().length()));
            print("Are there more files? ");
            
            print(MAKE_DELEGATE, "ORIGINAL FILE [" + p.getFileName(), "] added to xml.");
            
        } while (yesFromUser());
        
        modConfig.setOriginalFilePaths(list);
        modConfig.setOriginalFiles(files);
        
        files = new ArrayList<File>();
        
        do {
            print("Please enter valid paths to EDITED files.\n");
            
            Path p = getPathFromUser();
            print(MAKE_DELEGATE, "EDITED FILE [" + p.getFileName(),
                        "] READY TO COMPARE TO ORIGINAL.");
            files.add(p.toFile());
            
            if (!(list.size() == files.size())) {
                print("Please match original file. ");
            } else break;
        } while (yesFromUser());
        
        modConfig.setEditedFiles(files);
        return modConfig;
    }
    
    /**
     * Returns true if user responds with a y.
     * 
     * @return
     */
    private static Boolean yesFromUser() {
        Scanner sc = new Scanner(System.in);
        String input = "";
        while (!(input.equals("y") || input.equals("n"))) {
            print("Please enter [y] | [n]: ");
            input = sc.nextLine();
        }
        return input.equals("y");
    }
    
    /**
     * Gets a string from System.in
     * 
     * @param queryMsg
     *            What is is the systems needs?
     * @return
     */
    private static String geStringFromUser(String queryMsg) {
        Scanner sc = new Scanner(System.in);
        print(queryMsg);
        return sc.nextLine();
    }
    
    /**
     * Gets a valid file path from the user.
     * 
     * @return String input
     */
    private static Path getPathFromUser() {
        Scanner sc = new Scanner(System.in);
        Path input;
        do {
            print("Please enter valid file path: ");
            input = Paths.get(sc.nextLine());
            if (Files.isDirectory(input) || input.equals("")) continue;
            else {
                break;
            }
        } while (true);
        return input;
    }
    
    private static void print(String... strings) {
        print(MAIN, strings);
    }
    
    /**
     * Prints error code; will be matched to error string on update
     * 
     * @param configFileError
     *            enum code
     */
    public static void exit(Error code) {
        print(MAIN, code.getMsg());
        System.exit(code.ordinal());
    }
    
    /**
     * Prints error code; will be matched to error string on update
     * 
     * @param code
     *            enum code
     */
    public static void exit(Error code, String msg) {
        print("ERROR CODE [" + code.ordinal(), "]: ", code.getMsg(), " [" + msg, "]");
        System.exit(code.ordinal());
    }
    
    /**
     * Prints console usage
     */
    protected static void printUsage() {
        
        print(MAIN_DELEGATE, "(([-i] | [-m]) (XMod Name)) | (-g)\n", "\tE.g:\t",
                    MAIN_DELEGATE, "-i GenderRatioMod\n", "\t\t", MAIN_DELEGATE, "-g");
    }
    
    @Override
    public void run() {
        printUsage();
    }
}
