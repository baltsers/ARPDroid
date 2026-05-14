package CFG;

/**
 * Created by malindam on 8/1/2017.
 */

import Artifacts.Details;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;
import soot.*;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

//import polyglot.visit.FlowGraph;

public class ARPApkMain {

    private static Logger log = LoggerFactory.getLogger(ARPApkMain.class.getName());

    public static void main(String[] args) throws IOException, XmlPullParserException {
        if (processManifest()) // check if the target version is less than 3 and has non auto granted permissions.
        {
            Soot.init(ARPObjectFactory.v().getARPManifestFile());
            removeInstrumentedAPPifexist();
            Soot.getAnalyzer().constructCallgraph();
            callGraphToFile();
            filleToClassesAndMethods();
            ARPObjectFactory.v().getARPDroidMethods().analyzeAndChange();
        }
        // apkDroidMethods.printApplicationMethod();
        // apkDroidMethods.printsourceToTarget();
        // apkDroidMethods.printTargetToSourc();
    }

    public static void removeInstrumentedAPPifexist() {
        File file = new File("sootOutput\\" + Details.APK);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void printMethod(String metod) {
        SootMethod m = Scene.v().getMethod(metod);
        Body body = m.retrieveActiveBody();
        Iterator<Unit> i = body.getUnits().snapshotIterator();
        while (i.hasNext()) {
            Unit u = i.next();
            System.out.println(metod + " $$$$ " + u.toString());
        }
    }

    public static void filleToClassesAndMethods() {
        File file = new File("sootOutput\\ApplicationClasses.txt");
        if (file.exists()) {
            file.delete();
        }
        final String filepath = "sootOutput\\ApplicationClasses.txt";
        BufferedWriter bw = null;

        for (SootClass sootClass : Scene.v().getApplicationClasses()) {
            for (SootMethod sootMethod : sootClass.getMethods()) {

                String line = "class = " + sootClass.getName() + " method = " + sootMethod.getSignature();
                try {
                    bw = new BufferedWriter(new FileWriter(filepath, true));
                    bw.write(line);
                    bw.newLine();
                    bw.flush();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } finally {                       // always close the file
                    if (bw != null) try {
                        bw.close();
                    } catch (IOException ioe2) {
                        // just ignore it
                    }
                }
            }
        }
        File file1 = new File("sootOutput\\LibraryClasses.txt");
        if (file.exists()) {
            file1.delete();
        }
        final String filepath1 = "sootOutput\\LibraryClasses.txt";
        BufferedWriter bw1 = null;

        for (SootClass sootClass : Scene.v().getLibraryClasses()) {
            for (SootMethod sootMethod : sootClass.getMethods()) {
                for (SootClass aClass : sootMethod.getExceptions()) {
                    String exception = aClass.getName();
                    String line = "class = " + sootClass.getName() + " method = " + sootMethod.getSignature()
                            + " exception " + exception;
                    try {
                        bw1 = new BufferedWriter(new FileWriter(filepath1, true));
                        bw1.write(line);
                        bw1.newLine();
                        bw1.flush();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    } finally {                       // always close the file
                        if (bw1 != null) try {
                            bw1.close();
                        } catch (IOException ioe2) {
                            // just ignore it
                        }
                    }

                }


            }

        }
    }

    public static void callGraphToFile() {
        File file = new File("sootOutput\\callgraph.txt");
        if (file.exists()) {
            file.delete();
        }
        final String filepath = "sootOutput\\callgraph.txt";
        BufferedWriter bw = null;

        for (Edge edge : Scene.v().getCallGraph()) {
            String source = edge.getSrc().method().getSignature();
            String target = edge.getTgt().method().getSignature();
            try {
                bw = new BufferedWriter(new FileWriter(filepath, true));
                bw.write(source + " ====> " + target);
                bw.newLine();
                bw.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {                       // always close the file
                if (bw != null) try {
                    bw.close();
                } catch (IOException ioe2) {
                    // just ignore it
                }
            }

        }
    }

    public static Boolean processManifest() {
        try {
            ARPManifestFile ARPManifestFile = new ARPManifestFile(Details.APK_PATH);
            ARPManifestFile.printPermision();
            if (ARPManifestFile.needToPrecess()) {
                log.info(Details.APK + " need to be processed");
                return true;
            } else {
                log.info(Details.APK + " need to not be processed");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return false;
        }

    }

    public void constructCallgraphApk() {

        SetupApplication analyzer = new SetupApplication(Details.ANDROID_PLATFORMS,
                Details.APK_PATH);

        analyzer.getConfig().setTaintAnalysisEnabled(false);
        Options.v().set_whole_program(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_validate(true);
        Scene.v().addBasicClass("android.hardware.Camera", SootClass.BODIES);
        Scene.v().addBasicClass("android.support.v4.app.ActivityCompat", SootClass.BODIES);


        Scene.v().addBasicClass("android.app.Service", SootClass.HIERARCHY);
        analyzer.setCallbackFile(getClass().getClassLoader().getResource("AndroidCallbacks.txt").getFile());
        analyzer.constructCallgraph();
        callGraphToFile();
        filleToClassesAndMethods();
//        System.out.println("+++++" +Scene.v().getMethod("<android.hardware.Camera: android.hardware.Camera$Parameters getParameters()>").getDeclaringClass().getName());

        //   for (Type type : Scene.v().getMethod("<yogi.corporationapps.telescope.bigzoomhd.MainActivity$5: void onPictureTaken(byte[],android.hardware.Camera)>").getParameterTypes()) {
        //       System.out.println(type.getEscapedName());

        //       }
        //   ;
    }

    public void changeTargetSDKversion() {
        if (processManifest()) {
            try {
                ARPManifestFile ARPManifestFile = new ARPManifestFile(Details.APK_PATH);
                ARPManifestFile.setTargetSdkVersion(23);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
        }
    }
}







