package CFG;

import Artifacts.Details;
import org.testng.annotations.Test;

/**
 * Created by malindam on 9/16/2017.
 */
public class ARPDroidMethodsTest {

    @Test
    public void testPrintsourceToTarget() throws Exception {
        ARPDroidMethods ARPDroidMethods = new ARPDroidMethods();
        ARPManifestFile ARPManifestFile = new ARPManifestFile(Details.APK_PATH);
        ARPApkMain.main(new String[]{""});
        //   apkDroidMethods.analyzeAndChange();
        ARPDroidMethods.printsourceToTarget();
    }
}