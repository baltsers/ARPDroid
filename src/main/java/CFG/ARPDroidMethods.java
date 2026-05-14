package CFG;

import Artifacts.Details;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Sources;

import java.io.*;
import java.util.*;

/**
 * Created by malindam on 9/15/2017.
 */
public class ARPDroidMethods {
    public static HashMap<String, String> appToBaseMethod;
    private static Map<String, Set<String>> baseMethodsToPermission;
    private static Logger log = LoggerFactory.getLogger(ARPDroidMethods.class.getName());
    Permission permission;
    Boolean reachTOtop = false;
    private Map<String, String> appMethods;
    private Map<String, LinkedList<SootMethod>> sourceToTarget;
    private Map<String, LinkedList<SootMethod>> targetToSource;
    private Set<String> permissionNeededMethods;
    private Set<String> nonModifiableMethod;
    private Set<String> modificationNeededMethods;
    private HashMap<String, Boolean> visited;


    public ARPDroidMethods() {
        permission = ARPObjectFactory.v().getPermission();
        permissionNeededMethods = new HashSet<String>();
        sourceToTarget = new HashMap<String, LinkedList<SootMethod>>();
        modificationNeededMethods = new HashSet<String>();
        targetToSource = new HashMap<String, LinkedList<SootMethod>>();
        appMethods = new HashMap<String, String>();
        nonModifiableMethod = new HashSet<String>();
        visited = new HashMap<String, Boolean>();
        appToBaseMethod = new HashMap<String, String>();
        baseMethodsToPermission = new HashMap<String, Set<String>>();
    }

    //Return the permission list of base methods
    public static Set<String> getPermissionOFMethod(String method) {
        return baseMethodsToPermission.get(method);
    }

    //This is to map base method to application method
    public static String getBaseMethodOfModifyingMethod(String method) {
        if (appToBaseMethod.containsKey(method)) {
            return appToBaseMethod.get(method);
        } else log.error("The application method to base method mapping is not present");
        return null;
    }

    public Set<String> getModificationNeededMethods() {
        return modificationNeededMethods;
    }

    public void analyzeAndChange() throws IOException {
        anylzeMethods();
        constructSrcTarMaps();
        for (SootClass sootClass : Scene.v().getApplicationClasses()) {
            for (SootMethod sootMethod : sootClass.getMethods()) {
                appMethods.put(sootMethod.getSignature(), sootClass.getName());
            }
        }
        analyzeModificationNeededMethod();
        printModificationNeededMethods();
        printPermissionNeededMethods();
        ARPAndroidInstrument instrument = new ARPAndroidInstrument();
        instrument.changeMethods(modificationNeededMethods);
    }

    private void analyzeModificationNeededMethod() {
        for (String permissionNeededMethod : permissionNeededMethods) {
            String baseMethod = permissionNeededMethod;
            if (permissionNeededMethod.equals(Details.SOOT_DUMMY_METHOD)) {
                log.info("Dummy main method is ignored");
                continue;
            }
            if (isApplicationMethod(permissionNeededMethod) && isModifiable(permissionNeededMethod)) {
                modificationNeededMethods.add(permissionNeededMethod);
                appToBaseMethod.put(permissionNeededMethod, baseMethod);
                log.info("Modifying method " + permissionNeededMethod);
                continue;
            }
            visited.clear();
            travelCallGraph(permissionNeededMethod, baseMethod);
        }
    }

    private void travelCallGraph(String targetMethod, String baseMEthod) {
        if (reachTOtop == false && isApplicationMethod(targetMethod) && isModifiable(targetMethod)) {
            modificationNeededMethods.add(targetMethod);
            appToBaseMethod.put(targetMethod, baseMEthod);
            reachTOtop = true;
        }
        if (targetMethod.equals(Details.SOOT_DUMMY_METHOD)) {
            reachTOtop = false;
        }
        visited.put(targetMethod, true);
        log.info("call graph " + targetMethod);
        // iterate over unvisited parents
        Iterator<MethodOrMethodContext> ptargets = new Sources(Scene.v().getCallGraph().edgesInto(Scene.v().getMethod(targetMethod)));
        if (ptargets != null) {
            while (ptargets.hasNext()) {
                SootMethod parent = (SootMethod) ptargets.next();
                //   log.info("call graph inside "+ parent.getSignature());
                if (!visited.containsKey(parent.getSignature())) travelCallGraph(parent.getSignature(), baseMEthod);
            }
        }
    }

    private Boolean isModifiable(String method) {
        if (nonModifiableMethod.isEmpty()) {
            File file = new File(getClass().getClassLoader().getResource("NonModifiableClasses.txt").getFile());
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                String st;
                while ((st = br.readLine()) != null) {
                    nonModifiableMethod.add(st);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (String s : nonModifiableMethod) {
                if (method.contains(s))
                    return false;
            }
        } else {
            for (String s : nonModifiableMethod) {
                if (method.contains(s))
                    return false;

            }
        }
        return true;
    }

    private void anylzeMethods() throws IOException {
        analyzeSignatureDependants();
        analyzeMethodDependants();
    }

    private void analyzeMethodDependants() {
        for (Edge edge : Scene.v().getCallGraph()) {
            if (permission.isPermissionNeededAPIMethod(edge.getSrc().method().getSignature())) {
                if (!permission.getAutoPermission().isAutoGrantedPermission(permission.
                        getPermissionOfSystemMethods(edge.getSrc().method().getSignature()))) {
                    permissionNeededMethods.add(edge.getSrc().method().getSignature());
                    if (!baseMethodsToPermission.containsKey(edge.getSrc().method().getSignature()))
                        baseMethodsToPermission.put(edge.getSrc().method().getSignature(),
                                permission.getPermissionOfSystemMethods(edge.getSrc().method().getSignature()));
                    else
                        log.error("the base method is already present in the map");

                    log.info("permission dependent API call found in call graph = " + edge.getSrc().method().getSignature() +
                            " Permission = " + permission.getPermissionOfSystemMethods(edge.getSrc().method().getSignature()));
                }

            }

            if (permission.isPermissionNeededAPIMethod(edge.getTgt().method().getSignature())) {
                if (!permission.getAutoPermission().isAutoGrantedPermission(permission.
                        getPermissionOfSystemMethods(edge.getTgt().method().getSignature()))) {
                    permissionNeededMethods.add(edge.getTgt().method().getSignature());

                    if (!baseMethodsToPermission.containsKey(edge.getTgt().method().getSignature()))
                        baseMethodsToPermission.put(edge.getTgt().method().getSignature(),
                                permission.getPermissionOfSystemMethods(edge.getTgt().method().getSignature()));
                    else
                        log.error("the base method is already present in the map");

                    log.info("permission dependent API call found in call graph = " + edge.getTgt().method().getSignature()
                            + " Permission = " + permission.getPermissionOfSystemMethods(edge.getTgt().method().getSignature()));
                }
            }
        }
    }

    private void analyzeSignatureDependants() throws IOException {
        for (Edge edge : Scene.v().getCallGraph()) {
            edge.getSrc().method().getParameterTypes().stream().filter
                    (type -> permission.isPermissionNeededAPIClass(type.getEscapedName()) &&
                            !permission.getAutoPermission().isAutoGrantedPermission
                                    (permission.getPermissionOfClass(type.getEscapedName()))).forEach(type -> {

                log.info("permission dependent API call found in SRC signature method = " +
                        edge.getSrc().method().getSignature() +
                        "permission = " + permission.getPermissionOfClass(type.getEscapedName()));
                permissionNeededMethods.add(edge.getSrc().method().getSignature());

                if (!baseMethodsToPermission.containsKey(edge.getSrc().method().getSignature())) {
                    for (String s : permission.getPermissionOfClass(type.getEscapedName())) {
                        Set<String> set = new HashSet<String>();
                        set.add(s);
                        baseMethodsToPermission.put(edge.getSrc().method().getSignature(), set);
                    }
                } else {
                    for (String s : permission.getPermissionOfClass(type.getEscapedName()))
                        baseMethodsToPermission.get(edge.getSrc().method().getSignature()).add(s);
                }
            });

            edge.getTgt().method().getParameterTypes().stream().filter
                    (type -> permission.isPermissionNeededAPIClass(type.getEscapedName()) &&
                            !permission.getAutoPermission().isAutoGrantedPermission
                                    (permission.getPermissionOfClass(type.getEscapedName()))).forEach(type -> {
                permissionNeededMethods.add(edge.getTgt().method().getSignature());
                log.info("permission dependent API call found in TRC signature method = " +
                        edge.getTgt().method().getSignature() +
                        "permission = " + permission.getPermissionOfClass(type.getEscapedName()));

                if (!baseMethodsToPermission.containsKey(edge.getTgt().method().getSignature())) {
                    for (String s : permission.getPermissionOfClass(type.getEscapedName())) {
                        Set<String> set = new HashSet<String>();
                        set.add(s);
                        baseMethodsToPermission.put(edge.getTgt().method().getSignature(), set);
                    }
                } else {
                    for (String s : permission.getPermissionOfClass(type.getEscapedName()))
                        baseMethodsToPermission.get(edge.getTgt().method().getSignature()).add(s);
                }
            });
        }
    }

    private void constructSrcTarMaps() {
        for (Edge edge : Scene.v().getCallGraph()) {

            if (sourceToTarget.containsKey(edge.getSrc().method().getSignature())) {
                sourceToTarget.get(edge.getSrc().method().getSignature()).add(edge.getTgt().method());
            } else {
                LinkedList<SootMethod> linkedlist = new LinkedList<SootMethod>();
                linkedlist.add(edge.getTgt().method());
                sourceToTarget.put(edge.getSrc().method().getSignature(), linkedlist);
            }
        }
    }

    public Boolean isApplicationMethod(String method) {
        return appMethods.containsKey(method);
    }

    public void printApplicationMethod() {
        for (Map.Entry<String, String> stringStringEntry : appMethods.entrySet()) {
            log.info("method =" + stringStringEntry.getKey() + " class = " + stringStringEntry.getValue());
        }
    }

    public void printsourceToTarget() {
        for (Map.Entry<String, LinkedList<SootMethod>> stringStringEntry : sourceToTarget.entrySet()) {
            for (Object o : stringStringEntry.getValue()) {
                log.info("src =" + stringStringEntry.getKey() + " target = " + o.toString());
            }
        }

    }

    public void printTargetToSourc() {
        for (Map.Entry<String, LinkedList<SootMethod>> stringStringEntry : targetToSource.entrySet()) {
            for (Object o : stringStringEntry.getValue()) {
                log.info("tar =" + stringStringEntry.getKey() + " src = " + o.toString());
            }
        }
    }

    public void printModificationNeededMethods() {
        File file = new File("sootOutput\\ModificationNeededMethods.txt");
        if (file.exists()) {
            file.delete();
        }
        for (String modificationNeededMethod : modificationNeededMethods) {
            final String filepath = "sootOutput\\ModificationNeededMethods.txt";
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(filepath, true));
                bw.write(modificationNeededMethod);
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

    public void printPermissionNeededMethods() {
        File file = new File("sootOutput\\PermissionNeededMethods.txt");
        if (file.exists()) {
            file.delete();
        }
        for (String permissionNeededMethod : permissionNeededMethods) {
            final String filepath = "sootOutput\\PermissionNeededMethods.txt";
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(filepath, true));
                bw.write(permissionNeededMethod);
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

    public Set<String> getPermissionNeededMethods() {
        return permissionNeededMethods;
    }
}
