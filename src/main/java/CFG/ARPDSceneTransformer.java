package CFG;

import Artifacts.Details;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.*;
import soot.jimple.toolkits.typing.fast.Integer127Type;
import soot.util.Chain;

import java.util.*;

/**
 * Created by malindam on 10/15/2017.
 */
public class ARPDSceneTransformer extends SceneTransformer {
    private static Logger log = LoggerFactory.getLogger(ARPDSceneTransformer.class.getName());
    private HashMap<String, Set<String>> finalChangingMethods;

    public ARPDSceneTransformer(HashMap<String, Set<String>> finalChangineMethods) {
        this.finalChangingMethods = finalChangineMethods;
    }

    private static Unit defaultReturn(Type returnType) {
        if (returnType == VoidType.v()) {
            return Jimple.v().newReturnVoidStmt();
        }
        if (returnType instanceof RefType
                || returnType instanceof ArrayType) {
            return Jimple.v().newReturnStmt(NullConstant.v());
        }

        if (returnType instanceof BooleanType) {
            return Jimple.v().newReturnStmt(IntConstant.v(0));
        }
        if (returnType instanceof ByteType) {
            return Jimple.v().newReturnStmt(IntConstant.v(0));
        }
        if (returnType instanceof CharType) {
            return Jimple.v().newReturnStmt(IntConstant.v(0));
        }
        if (returnType instanceof DoubleType) {
            return Jimple.v().newReturnStmt(DoubleConstant.v(0));
        }
        if (returnType instanceof FloatType) {
            return Jimple.v().newReturnStmt(FloatConstant.v(0));
        }
        if (returnType instanceof IntType) {
            return Jimple.v().newReturnStmt(IntConstant.v(0));
        }
        if (returnType instanceof LongType) {
            return Jimple.v().newReturnStmt(LongConstant.v(0));
        }
        if (returnType instanceof ShortType) {
            return Jimple.v().newReturnStmt(IntConstant.v(0));
        }
        throw new VerifyError(String.format("Invalid return type: %1s",
                returnType));
    }

    public void setFinalChangingMethods(HashMap<String, Set<String>> finalChangingMethods) {
        this.finalChangingMethods = finalChangingMethods;
    }

    private Local generateNewLocal(Body body, Type type) {
        LocalGenerator lg = new LocalGenerator(body);
        return lg.generateLocal(type);
    }

    private NopStmt insertNopStmt(Body body, Unit u) {
        NopStmt nop = Jimple.v().newNopStmt();
        body.getUnits().insertBefore(nop, u);
        return nop;
    }

    @Override
    protected void internalTransform(String s, Map<String, String> map) {

        ARPUtilInstrument.enrichActivityCompatContextCompat(); // add permission request and check methods as phantom methods.

        for (Map.Entry<String, Set<String>> entry : finalChangingMethods.entrySet()) {
            String method = entry.getKey();
            Set<String> permission = entry.getValue();
            if (method.equals(Details.SOOT_DUMMY_METHOD))
                continue;
            SootMethod sm = Scene.v().getMethod(method);
            SootClass clz = Scene.v().getSootClass(sm.getDeclaringClass().getName());

            if (clz.isPhantom() || clz.resolvingLevel() != 3) {
                log.error(clz.getName() + "This is a Phantom class or the resolving level is not 3, isPhantom = " + clz.isPhantom()
                        + "resolvingLevel =" + clz.resolvingLevel());
                continue;
            }
            if (clz.isInnerClass())
            {
                log.info("The method "+ sm.getSignature()+" Will not be modified");
                continue;
            }


            log.info("The method" + method + " in class " + sm.getDeclaringClass().getName() + "will be" +
                    " modified with permission " + permission);

            Body bd = sm.retrieveActiveBody();
            log.info("++++++++++Method body before modification++++++++++");
            System.err.println(bd);

            Iterator<Unit> it = bd.getUnits().snapshotIterator();
            Unit unitThis1 = null;
            Unit unitThis = null;
            int i = 0;
            int j = 0;
            while (it.hasNext()) { // Creating reference unit to insert the code
                unitThis1 = (Unit) it.next();
                soot.jimple.Stmt st = (soot.jimple.Stmt) unitThis1;
                if (unitThis1.toString().contains("@parameter")) {
                    unitThis = unitThis1;
                }

                if (st.containsInvokeExpr()) {
                    break;
                }
                i++;
            }

            InsertCodeAfter(unitThis, bd, permission, clz, sm);
            addInterface(clz);
            AnalyzeInnerClass(clz);
            log.info("++++++++++Method body after modification++++++++++");
            System.err.println(bd);
            bd.validate();
        }
    }

    private void AnalyzeInnerClass(SootClass clz)
    {
        log.info("Parent class of the class "+clz.getName() + " is "+clz.getSuperclass());
        System.err.println(clz);

    }

    private void InsertCodeAfter(Unit unitThis, Body bd, Set<String> permission, SootClass clz, SootMethod sm) {
        String firstAPermission = "";
        for (String s1 : permission) {
            firstAPermission = s1;//TODO modify to insert all the permissions
            break;
        }
        Type Int = IntType.v();//Generate statement to check self permission
        Local hasPermissionVar = generateNewLocal(bd, Int);
        AssignStmt astmt = Jimple.v().newAssignStmt(hasPermissionVar,
                ARPUtilInstrument.generateCheckSelfPermissionMethod(bd, firstAPermission, clz));
        bd.getUnits().insertAfter(astmt, unitThis);

        IntConstant hasPermission = IntConstant.v(0);//TODO create enumeration constance
        EqExpr equalExpr = Jimple.v().newEqExpr(hasPermissionVar, hasPermission);
        NopStmt nop = Jimple.v().newNopStmt();
        IfStmt ifStmt = Jimple.v().newIfStmt(equalExpr, nop);

        InvokeStmt reqPer = ARPUtilInstrument.createRequestPermissions(permission, bd, unitThis, clz);
        bd.getUnits().insertAfter(reqPer, astmt);
        Unit ret = defaultReturn(sm.getReturnType());
        bd.getUnits().insertAfter(ret, reqPer);
        bd.getUnits().insertAfter(nop, ret);
        bd.getUnits().insertAfter(ifStmt, astmt);

    }

    private void insertCodeBefore(Unit unitThis, Body bd, Set<String> permission, SootClass clz, SootMethod sm) {
        Chain units = bd.getUnits();
        List<Unit> generated = new ArrayList<Unit>();
        InvokeStmt reqPer = ARPUtilInstrument.createRequestPermissions(permission, bd, unitThis, clz);
        units.insertBefore(reqPer, unitThis);
        String firstAPermission = "";
        for (String s1 : permission) {
            firstAPermission = s1;
            break;
        }
        Type Int = IntType.v();//Generate statement to check self permission
        Local hasPermissionVar = generateNewLocal(bd, Int);
        AssignStmt astmt = Jimple.v().newAssignStmt(hasPermissionVar,
                ARPUtilInstrument.generateCheckSelfPermissionMethod(bd, firstAPermission, clz));
        // units.insertBefore(astmt,reqPer);
        generated.add(astmt);

        IntConstant hasPermission = IntConstant.v(0);//TODO create enumeration constance
        EqExpr equalExpr = Jimple.v().newEqExpr(hasPermissionVar, hasPermission);
        NopStmt nop = insertNopStmt(bd, unitThis);

        //   UnitBox next = Jimple.v().newStmtBox(null);
        //      bd.getUnits().insertAfter(nop, reqPer);
        IfStmt ifStmt = Jimple.v().newIfStmt(equalExpr, nop);

        generated.add(ifStmt);
        bd.getUnits().insertBefore(generated, reqPer);
        //bd.getUnits().insertAfter(nop,reqPer);
        bd.getUnits().insertAfter(defaultReturn(sm.getReturnType()), reqPer);
    }

    private void addPermissionValidation(SootMethod method, Body body) {
        Type hasWriteContactsPermission = Integer127Type.v();
    }

    private void addInterface(SootClass clz) {
        for (SootClass sootClass : clz.getInterfaces()) {
            log.info("Interface classes  of  " + clz.getName() + " Is " + sootClass.getName());
            if (sootClass.getName().equals("android.support.v4.app.ActivityCompat$OnRequestPermissionsResultCallback")) {
                log.warn("The interface ActivityCompat$OnRequestPermissionsResultCallback " +
                        "already present in the class " + sootClass.getName());
                return;
            }
        }
        SootClass appClass = Scene.v().getSootClass("android.support.v4.app.ActivityCompat$OnRequestPermissionsResultCallback");
        clz.addInterface(appClass);
        for (SootClass sootClass : clz.getInterfaces()) {
            log.info("Interface classes of class " + clz.getName() + " after adding Is " + sootClass.getName());
        }
    }
}
