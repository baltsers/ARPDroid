package CFG;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created by malindam on 8/13/2017.
 */
public class ARPAutoGenPermissionsTest {

    @Test
    public void testReadFile() throws Exception {
        ARPAutoGenPermissions ARPAutoGenPermissions = new ARPAutoGenPermissions();
        for (Object o : ARPAutoGenPermissions.getAutoPermissions()) {
            System.out.println(o.toString());
        }
    }

    @Test
    public void testGetAutoPermissions() throws Exception {

    }

    @Test
    public void testIsAutoGrantedPermission() throws Exception {
        ARPAutoGenPermissions ARPAutoGenPermissions = new ARPAutoGenPermissions();
        assertEquals(ARPAutoGenPermissions.isAutoGrantedPermission("test"), Boolean.FALSE);
        assertEquals(ARPAutoGenPermissions.isAutoGrantedPermission("android.permission.MODIFY_AUDIO_SETTINGS"), Boolean.TRUE);

    }
}