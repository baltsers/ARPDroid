package CFG;

import Artifacts.Details;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;
import soot.jimple.infoflow.android.axml.AXmlAttribute;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by malindam on 8/13/2017.
 */
public class ARPManifestFile extends ProcessManifest {
    private Logger log = LoggerFactory.getLogger(ARPManifestFile.class.getName());
    private Set<String> nonAutoApplicationPermission;
    private ARPAutoGenPermissions ARPAutoGenPermissions;

    public ARPManifestFile(String apkPath) throws IOException, XmlPullParserException {
        super(apkPath);
        ARPAutoGenPermissions = ARPObjectFactory.v().getARPAutoGenPermissions();

    }

    public void printPermision() {
        Set<String> permissions = getPermissions();
        Iterator<String> iter = permissions.iterator();
        File file = new File("sootOutput\\ManifestFile.txt");
        if (file.exists()) {
            file.delete();
        }
        while (iter.hasNext()) {
            final String filepath = "sootOutput\\ManifestFile.txt";
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(filepath, true));
                bw.write(iter.next());
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

    public Boolean needToPrecess() throws IOException {
        return (IsSDKLessThanTwentyThree() && hasNonAutoPermission());
    }

    protected Boolean IsSDKLessThanTwentyThree() {
        log.info("Target SDK versioin of " + Details.APK + " = " + targetSdkVersion());
        return (targetSdkVersion() < 23) ? true : false;
    }


    protected Boolean hasNonAutoPermission() throws IOException {
        Set<String> permissions = getPermissions();
        Iterator<String> iter = permissions.iterator();
        ARPAutoGenPermissions ARPAutoGenPermissions = new ARPAutoGenPermissions();
        boolean flag = false;
        HashSet yesPermission = new HashSet();
        HashSet noPermission = new HashSet();
        while (iter.hasNext()) {
            String appPermission = iter.next();
            flag = false;
            for (Object o : ARPAutoGenPermissions.getAutoPermissions()) {
                String autoGenPermission = o.toString();
                if (autoGenPermission.equals(appPermission)) {
                    flag = true;
                }
            }
            if (flag) {
                yesPermission.add(appPermission);
                log.info(appPermission + "was added to yesPermission");
            } else {
                noPermission.add(appPermission);
                log.info(appPermission + "was added to noPermission");
            }
        }
        return !noPermission.isEmpty();
    }

    public void setTargetSdkVersion(int version) {
        List usesSdk = this.manifest.getChildrenWithTag("uses-sdk");
        if (usesSdk != null && !usesSdk.isEmpty()) {
            AXmlAttribute attr = new AXmlAttribute("targetSdkVersion", version, "");
            ((AXmlNode) usesSdk.get(0)).addAttribute(attr);

        }
    }

    public String getAndroidFilePath() {
        File f1 = new File(Details.ANDROID_PLATFORMS + "\\" + "android-" + targetSdkVersion());
        File f2 = new File(Details.ANDROID_PLATFORMS + "\\" + "android-" + getMinSdkVersion());
        if (f1.exists()) {
            log.info("Android file path = " + f1.getAbsolutePath());
            return (f1.getAbsolutePath() + "\\" + "android.jar");
        } else if (f2.exists()) {
            log.info("Android file path = " + f2.getAbsolutePath());
            return (f2.getAbsolutePath() + "\\" + "android.jar");
        }
        return null;
    }

    public Boolean IsPermissionListedInApplication(String permissions) {
        if (getPermissions().contains(permissions) && !ARPAutoGenPermissions.isAutoGrantedPermission(permissions)) {
            System.out.println("Permission " + permissions + "is application defined");
            return true;
        } else {
            System.out.println("Permission " + permissions + "is non application defined");
            return false;
        }

        //  return permissions.contains(permissions);
    }
}


