package CFG;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by malindam on 11/21/2017.
 */
public class UtilValidation {
    private static Set<String> InvalidParentClasses;

    public UtilValidation() {
        this.InvalidParentClasses = new HashSet<String>();
        ;
    }

    public static boolean isValidParentClass(String classname) {
        if (InvalidParentClasses.isEmpty()) {
            File file = new File(UtilValidation.class.getClassLoader().getResource("NonModifiableParentClasses.txt").getFile());
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                String st;
                while ((st = br.readLine()) != null) {
                    InvalidParentClasses.add(st);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return InvalidParentClasses.contains(classname);
    }
}
