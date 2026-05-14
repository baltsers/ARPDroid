package CFG;

import Artifacts.Details;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Created by malindam on 9/4/2017.
 */
public class Permission {

    private Logger log = LoggerFactory.getLogger(ARPApkMain.class.getName());
    private Map<String, Set<String>> permisionToClass;
    private Map<String, Set<String>> classToPermission;
    private Map<String, Set<String>> methodsToPermission;
    private ARPAutoGenPermissions autoPermission;

    public Permission()  {
        permisionToClass = new HashMap<String, Set<String>>();
        classToPermission = new HashMap<String, Set<String>>();
        methodsToPermission = new HashMap<String, Set<String>>();
        autoPermission = ARPObjectFactory.v().getARPAutoGenPermissions();
        try {
            analyzePermissionFiles(Details.PERMISSION_MAPPINGS);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        printClassToPermission();
    }

    public Boolean isPermissionNeededAPIClass(String object) {
        return classToPermission.containsKey(object);
    }

    public Boolean isPermissionNeededAPIMethod(String object) {
        return methodsToPermission.containsKey(object);
    }

    public Set<String> getPermissionOfClass(String clas) {
        return classToPermission.get(clas);
    }

    public Set<String> getPermissionOfSystemMethods(String method) {
        return methodsToPermission.get(method);
    }

    public Set<String> getApplicationPermissionOfMethod(String method) //filter permission defined in the application
    {
        return ARPDroidMethods.getPermissionOFMethod(ARPDroidMethods.
                getBaseMethodOfModifyingMethod(method)).stream().filter(s ->
                ARPObjectFactory.v().getARPManifestFile().IsPermissionListedInApplication(s)).collect(Collectors.toSet());
    }

    public void analyzePermissionFiles(final String path) throws IOException {
        File f = new File(path);
        File[] list = f.listFiles();
        if (list == null) {
            return;
        }
        for (File entry : list) {
            if (entry.isFile()) {
                File file = new File(entry.getAbsolutePath());
                BufferedReader br = new BufferedReader(new FileReader(file));
                String st;
                String permission = null;
                while ((st = br.readLine()) != null) {
                    String aClass = st.substring(st.indexOf("<") + 1, st.lastIndexOf(":"));
                    String method = st.substring(st.indexOf("<") + 1, st.lastIndexOf(">") + 1);
                    if (aClass.equals("Permission")) {
                        permission = st.substring(st.indexOf(":") + 1);
                    }
                    if (permisionToClass.get(permission) == null) {
                        permisionToClass.put(permission, new HashSet<String>());
                    } else {
                        permisionToClass.get(permission).add(aClass);
                    }
                    if (!aClass.equals("Permission")) {
                        if (classToPermission.get(aClass) == null) {
                            HashSet<String> hashset = new HashSet<String>();
                            hashset.add(permission);
                            classToPermission.put(aClass, hashset);

                        } else {
                            classToPermission.get(aClass).add(permission);
                        }
                        if (methodsToPermission.get("<" + method) == null) {
                            HashSet<String> hashset = new HashSet<String>();
                            hashset.add(permission);
                            methodsToPermission.put("<" + method, hashset);

                        } else {
                            methodsToPermission.get("<" + method).add(permission);
                        }
                    }
                }
            }
        }
    }

    public void printClassToPermission() {
        File file = new File("sootOutput\\ClassToPermission.txt");
        if (file.exists()) {
            file.delete();
        }
        final String filepath = "sootOutput\\ClassToPermission.txt";

        for (Map.Entry<String, Set<String>> stringStringEntry : classToPermission.entrySet()) {
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(filepath, true));
                String line = "Permission = " + stringStringEntry.getValue() + " Class = " + stringStringEntry.getKey();
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

    public void printPermissionToClassMap() {
        for (Map.Entry<String, Set<String>> entry : permisionToClass.entrySet()) {
            for (Object o : entry.getValue()) {

                log.info("permission =" + entry.getKey() + "class =" + o.toString());
            }
            ;
        }
    }

    public void printMethodToPermissionMap() {
        File file = new File("sootOutput\\MethodsToPermission.txt");
        if (file.exists()) {
            file.delete();
        }
        final String filepath = "sootOutput\\MethodsToPermission.txt";

        for (Map.Entry<String, Set<String>> stringStringEntry : methodsToPermission.entrySet()) {
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(filepath, true));
                String method = "Method = " + stringStringEntry.getKey();
                String permssion = null;
                for (String s : stringStringEntry.getValue()) {
                    permssion = "Permission  = " + s;
                }
                String line = method + permssion;
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

    public Map<String, Set<String>> getPermisionToClass() {
        return permisionToClass;
    }

    public Map<String, Set<String>> getClassToPermission() {
        return classToPermission;
    }

    public ARPAutoGenPermissions getAutoPermission() {
        return autoPermission;
    }


}
