package CFG;

import Artifacts.Details;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.jimple.infoflow.android.SetupApplication;
import soot.options.Options;

import java.util.Collections;

/**
 * Created by malindam on 9/7/2017.
 */
public class Soot {
    public static boolean INITIALIZED;
    private static SetupApplication analyzer;
    private static Logger log = LoggerFactory.getLogger(Soot.class.getName());

    public Soot() {
    }

    public static void init(ARPManifestFile ARPManifestFile) {
        G.reset();
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_whole_program(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_validate(true);
        Options.v().set_output_format(Options.output_format_dex);
        Options.v().set_soot_classpath(ARPManifestFile.getAndroidFilePath());
        Options.v().set_force_android_jar(ARPManifestFile.getAndroidFilePath());
        Options.v().set_src_prec(Options.src_prec_apk);
        // Options.v().set_oaat(true);
        Options.v().set_process_dir(Collections.singletonList(Details.APK_PATH));
        Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
        Scene.v().addBasicClass("android.app.Activity",SootClass.BODIES);
        Scene.v().addBasicClass("android.app.Service", SootClass.HIERARCHY);
        analyzer = new SetupApplication(Details.ANDROID_PLATFORMS,Details.APK_PATH);
        analyzer.getConfig().setTaintAnalysisEnabled(false);
        analyzer.setCallbackFile(Soot.class.getClassLoader().getResource("AndroidCallbacks.txt").getFile());
        Scene.v().loadNecessaryClasses();
    }

    public static SetupApplication getAnalyzer() {
        return analyzer;
    }
}
