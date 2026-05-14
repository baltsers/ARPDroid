package CFG;

import Artifacts.Details;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Created by malindam on 8/13/2017.
 */
public class ARPManifestFileTest {
    private Logger log = LoggerFactory.getLogger(ARPManifestFileTest.class.getName());

    @Test
    public void testPrintPermision() throws Exception {
        ARPManifestFile test = new ARPManifestFile(Details.APK_PATH);
        test.printPermision();
    }

    @Test
    public void testNeedToProcess() throws Exception {
        ARPManifestFile test = new ARPManifestFile(Details.APK_PATH);
        if (test.needToPrecess()) {
            System.out.println(Details.APK + "Need to be Processed");
        } else {
            System.out.println(Details.APK + "Need to not be Processed");
        }
    }

    @Test
    public void testPrintPermision1() throws Exception {

    }

    @Test
    public void testIsSDKLessThanTwentyThree() throws Exception {

    }

    @Test
    public void testHasNonAutoPermission() throws Exception {
        ARPManifestFile test = new ARPManifestFile(Details.APK_PATH);
        if (test.IsSDKLessThanTwentyThree())
            System.out.println(Details.APK + "Need to be Processed");
    }

    @Test
    public void testGetAndroidFilePath() throws Exception {
        ARPManifestFile ARPManifestFile = new ARPManifestFile(Details.APK_PATH);
        System.out.println(ARPManifestFile.getAndroidFilePath());
    }
}