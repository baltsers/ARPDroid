package CFG;
/**
 * Created by malindam on 8/15/2017.
 */

import Artifacts.Details;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;
import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.Transform;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;


public class ARPAndroidInstrument {
    private static Logger log = LoggerFactory.getLogger(ARPAndroidInstrument.class.getName());
    private Set<String> modificationNeededMethods;
    private HashMap<String, Set<String>> finalChangingMethods;

    public ARPAndroidInstrument() {
        finalChangingMethods = new HashMap<String, Set<String>>();
    }

    public void setModificationNeededMethods(Set<String> modificationNeededMethods) {
        this.modificationNeededMethods = modificationNeededMethods;
    }

    public void refineModificationNeededMethods() {
        for (String modificationNeededMethod : modificationNeededMethods) {
            analyzeCallBacks(modificationNeededMethod);
        }
        removeNonApplicationPermissionMethods();
    }

    public void removeNonApplicationPermissionMethods() {
        for (String modificationNeededMethod : modificationNeededMethods) {
            if (!ARPObjectFactory.v().getPermission().getApplicationPermissionOfMethod(modificationNeededMethod).isEmpty()) {
                log.info(modificationNeededMethod + " was added as final modifying method");
                finalChangingMethods.put(modificationNeededMethod, ARPObjectFactory.v().getPermission()
                        .getApplicationPermissionOfMethod(modificationNeededMethod));
            }
        }

    }

    public void analyzeCallBacks(String modiMethod) {
        if (modiMethod.equals(Details.SOOT_DUMMY_METHOD))
            return;
        SootMethod method = Scene.v().getMethod(modiMethod);
        for (Method method1 : method.getClass().getSuperclass().getMethods()) {
            //   if (method.getSubSignature().contains(method1.getName()))
            log.info("modificationNeededMethod = " + method.getSubSignature() + "Super Class " + method.getClass().getSuperclass() +
                    "Methods " + method1.getName());
        }

        traverseThroughClass(method.getClass().getSuperclass(), method);
    }

    public void traverseThroughClass(Class SuperClass, SootMethod method) {
        try {
            for (Method sootMethod : SuperClass.getSuperclass().getMethods()) {
                if (method.getSubSignature().contains(sootMethod.getName())) {
                    log.info("call back method found" + method.getSignature());
                }
                log.info("modificationNeededMethod = " + method.getSubSignature() + "Super Class " + sootMethod.getClass() +
                        "Methods " + sootMethod.getName());
            }
        } catch (NullPointerException e) {
            return;
        }
        traverseThroughClass(SuperClass.getSuperclass(), method);
    }

    public void changeMethods(Set<String> methodList) {
        Soot.init(ARPObjectFactory.v().getARPManifestFile());//init again to reset
        setModificationNeededMethods(methodList);
        refineModificationNeededMethods();
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myAnalysis", new ARPDSceneTransformer(finalChangingMethods)));
        PackManager.v().runPacks();
        PackManager.v().writeOutput();
        printStatistics(methodList);
    }

    public void printStatistics(Set<String> methodList)
    {
        log.info("+++++Following methods are modified+++++");
        for (String s : methodList) {
            log.info(s);
        }

    }
}
