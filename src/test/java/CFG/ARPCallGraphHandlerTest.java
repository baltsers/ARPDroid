package CFG;

import Artifacts.Details;
import org.testng.annotations.Test;
import org.xmlpull.v1.XmlPullParserException;
import soot.Scene;
import soot.options.Options;

import java.io.IOException;

import static org.testng.Assert.*;

/**
 * Created by malindam on 12/1/2017.
 */
public class ARPCallGraphHandlerTest {

    @Test
    public void testConstructUnitGraph() throws Exception {
        ARPManifestFile manifestFile = null;
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
        Soot.init(manifestFile);
        Soot.getAnalyzer().constructCallgraph();
        ARPCallGraphHandler arpCallGraphHandler = new ARPCallGraphHandler();
        arpCallGraphHandler.ConstructUnitGraph(Scene.v().getMethod(Details.SOOT_DUMMY_METHOD));
    }
}