package CFG;

import Artifacts.Details;
import org.jboss.util.Null;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by malindam on 12/1/2017.
 */
public class ARPObjectFactory {
    private static ARPObjectFactory Instance = null;
    private  Permission permission = null;
    private ARPDroidMethods arpDroidMethods = null;
    private ARPAndroidInstrument arpAndroidInstrument = null;
    private ARPAutoGenPermissions autoGenPermissions = null;
    private ARPManifestFile manifestFile = null;

    private ARPObjectFactory() {
    }

    public static synchronized ARPObjectFactory v(){
        if(Instance == null){
            Instance = new ARPObjectFactory();
        }
        return Instance;
    }

    public Permission getPermission()  {
        if (permission == null)
        {
            permission = new Permission();
        }
        return permission;
    }

    public ARPDroidMethods getARPDroidMethods() {
        if (arpDroidMethods == null)
        {
            arpDroidMethods = new ARPDroidMethods();
        }
        return arpDroidMethods;
    }

    public ARPAutoGenPermissions getARPAutoGenPermissions() {
        if (autoGenPermissions == null)
        {
            autoGenPermissions = new ARPAutoGenPermissions();
        }
        return autoGenPermissions;
    }

    public ARPManifestFile getARPManifestFile()  {
        if (manifestFile == null)
        {
            try {
                manifestFile = new ARPManifestFile(Details.APK_PATH);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        return manifestFile;
    }
}
