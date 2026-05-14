package CFG;

import Artifacts.Details;
import org.testng.annotations.Test;

/**
 * Created by malindam on 9/5/2017.
 */
public class PermissionTest {

    @Test
    public void testIsPermissionNeededAPI() throws Exception {
        Permission permission = new Permission();
        if (permission.isPermissionNeededAPIClass("android.hardware.Camera")) {
            System.out.println("Permission dependent API found");
        }
        if (!permission.isPermissionNeededAPIClass("test")) {
            System.out.println("Permission dependent API not found");
        }


    }

    @Test
    public void testPrintPermissionToClassMap() throws Exception {
        Permission permission = new Permission();
        permission.printPermissionToClassMap();

    }

    @Test
    public void testPrintMethodToPermissionMap() throws Exception {
        Permission permission = new Permission();
        permission.printMethodToPermissionMap();
    }

    @Test
    public void testAnalyzePermissionFiles() throws Exception {
        Permission permission = new Permission();
        permission.analyzePermissionFiles(Details.PERMISSION_MAPPINGS);
    }
}