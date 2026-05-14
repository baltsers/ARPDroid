package CFG;

import Artifacts.Details;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by malindam on 8/13/2017.
 */
public class ARPAutoGenPermissions {
    private static HashSet autoPermissions;
    private static Logger log = LoggerFactory.getLogger(ARPApkMain.class.getName());

    public ARPAutoGenPermissions() {

    }

    private void readFile() throws IOException {
        File file = new File(getClass().getClassLoader().getResource
                (Details.PERMISSIONFILE).getFile());
        HashSet permissions = new HashSet();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        while ((st = br.readLine()) != null) {
            permissions.add(st);
        }
        autoPermissions = permissions;
    }

    public HashSet getAutoPermissions() throws IOException {
        readFile();
        return autoPermissions;
    }

    public Boolean isAutoGrantedPermission(String key) {

        if (autoPermissions.isEmpty()) {
            try {
                readFile();
            } catch (IOException e) {
                log.info("Probably Auto granted permission list file not found");
                e.printStackTrace();
            }
            return autoPermissions.contains(key);
        } else {
            return autoPermissions.contains(key);
        }
    }

    public Boolean isAutoGrantedPermission(Set<String> set) {
        try {
            readFile();
        } catch (IOException e) {
            log.info("Probably Auto granted permission list file not found");
            e.printStackTrace();
        }

        for (String s : set) {
            if (!autoPermissions.contains(s)) {
                return false;
            }
        }
        return true;
    }

}
