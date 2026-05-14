package CFG;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by malindam on 10/27/2017.
 */
public class ARPUtilInstrument {
    private static Logger log = LoggerFactory.getLogger(ARPUtilInstrument.class.getName());

    public static InvokeStmt createRequestPermissions(Set<String> permission, Body body, Unit unit, SootClass clz) {
        LinkedList args = new LinkedList();  // Creating arguments

        Local clzPointer = null;
        for (Local local : body.getLocals()) {
            if (local.getType().equals(clz.getType())) {
                clzPointer = local;
                break;
            }
        }
        ;

        log.info("Class type " + clzPointer.getType());

     //   if (clz.getSuperclass().getName().equals("java.lang.Object")) {
      //      clz.setSuperclass(Scene.v().getSootClass("android.app.Activity"));
      //  }

        args.add(clzPointer);
        LocalGenerator localGenerator = new LocalGenerator(body);
        Local local1 = localGenerator.generateLocal(ArrayType.v(RefType.v("java.lang.String"), 1));
        NewArrayExpr newArrayExpr = Jimple.v().newNewArrayExpr(RefType.v("java.lang.String"), IntConstant.v(permission.size()));
        AssignStmt assignStmt2 = Jimple.v().newAssignStmt(local1, newArrayExpr);
        body.getUnits().insertAfter(assignStmt2, unit);
        int index = 0;
        for (String s : permission) {
            body.getUnits().insertAfter(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(local1, IntConstant.v(index)), StringConstant.v(s)), assignStmt2);
            index++;
        }
        args.add(local1);
        Value REQUEST_CODE_ASK_PERMISSIONS = IntConstant.v(123);
        args.add(REQUEST_CODE_ASK_PERMISSIONS);
        SootMethod sm = Scene.v().getMethod
                ("<android.support.v4.app.ActivityCompat: void requestPermissions(android.app.Activity,java.lang.String[],int)>");
        return Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(sm.makeRef(), args));
    }

    public static void enrichActivityCompatContextCompat() {
        try {
            Scene.v().getMethod
                    ("<android.support.v4.app.ActivityCompat: void requestPermissions(android.app.Activity,java.lang.String[],int)>");

        } catch (RuntimeException e) {
            log.warn("requestPermissions can not be inserted from android or support lib jars. Creating the method");
            ArrayList<Type> argu = new ArrayList<>();
            argu.add(RefType.v("android.app.Activity"));
            argu.add(ArrayType.v(RefType.v("java.lang.String"), 1));
            argu.add(IntType.v());
            SootMethod sm1 = new SootMethod("requestPermissions",
                    argu, VoidType.v(), Modifier.PUBLIC | Modifier.STATIC);
            sm1.setPhantom(true);

            SootClass ac = Scene.v().getSootClass("android.support.v4.app.ActivityCompat");
            ac.addMethod(sm1);
        }

        try {
            Scene.v().getMethod
                    ("<android.support.v4.content.ContextCompat: int checkSelfPermission(android.content.Context,java.lang.String)>");
        } catch (RuntimeException e) {
            SootClass cc = Scene.v().getSootClass("android.support.v4.content.ContextCompat");
            ArrayList<Type> argu = new ArrayList<>();
            argu.add(RefType.v("android.content.Context"));
            argu.add(RefType.v("java.lang.String"));
            SootMethod sm1 = new SootMethod("checkSelfPermission",
                    argu, IntType.v(), Modifier.PUBLIC | Modifier.STATIC);
            sm1.setPhantom(true);
            cc.addMethod(sm1);
        }

    }

    public static StaticInvokeExpr generateCheckSelfPermissionMethod(Body body, String permission, SootClass clz) {
        LinkedList args = new LinkedList();
        Local clzPointer = null;

        for (Local local : body.getLocals()) {
            if (local.getType().equals(clz.getType())) {
                clzPointer = local;
                break;
            }
        }
        ;

        args.add(clzPointer);

        Value ReqPermission = StringConstant.v(permission);
        args.add(ReqPermission);
        SootMethod sm = Scene.v().getMethod
                ("<android.support.v4.content.ContextCompat: int checkSelfPermission(android.content.Context,java.lang.String)>");
        StaticInvokeExpr invokeExpr = Jimple.v().newStaticInvokeExpr(sm.makeRef(), args);
        return invokeExpr;
    }

    public static AssignStmt createNewStringArray(Body body, int size) {
        LocalGenerator localGenerator = new LocalGenerator(body);
        Local local1 = localGenerator.generateLocal(ArrayType.v(RefType.v("java.lang.String"), 1));
        NewArrayExpr newArrayExpr = Jimple.v().newNewArrayExpr(RefType.v("java.lang.String"), IntConstant.v(size));
        AssignStmt assignStmt2 = Jimple.v().newAssignStmt(local1, newArrayExpr);
        return assignStmt2;
    }
}
