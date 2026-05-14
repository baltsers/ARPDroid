package CFG;

import org.testng.annotations.Test;

/**
 * Created by malindam on 8/4/2017.
 */
public class ARPApkMainTest {

    @org.testng.annotations.Test
    public void testMain() throws Exception {
        ARPApkMain main = new ARPApkMain();
        String[] array = {""};
        main.main(array);
    }

    @Test
    public void testConstructCallgraphApk() throws Exception {
        ARPApkMain main = new ARPApkMain();
        main.constructCallgraphApk();
    }

    @Test
    public void testConstructCallgraphApk1() throws Exception {

    }

    @Test
    public void testCallGraphToFile() throws Exception {

    }

    @Test
    public void testProcessManifest() throws Exception {
        ARPApkMain main = new ARPApkMain();
        main.processManifest();
    }

    @Test
    public void testConstructCallgraphApk2() throws Exception {

    }

    @Test
    public void testCallGraphToFile1() throws Exception {

    }

    @Test
    public void testProcessManifest1() throws Exception {

    }

    @Test
    public void testChangeTargetSDKversion() throws Exception {
        ARPApkMain main = new ARPApkMain();
        main.changeTargetSDKversion();
    }

    @Test
    public void testFilleToClassesAndMethods() throws Exception {
        ARPApkMain main = new ARPApkMain();
        main.filleToClassesAndMethods();
    }

}