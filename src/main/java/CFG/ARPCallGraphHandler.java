package CFG;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import soot.toolkits.graph.DirectedGraph;

import java.util.Iterator;
import java.util.List;

/**
 * Created by malindam on 12/1/2017.
 */
public class ARPCallGraphHandler {
    public ARPCallGraphHandler() {
    }

    public void ConstructUnitGraph(SootMethod mathod)
    {
        InfoflowCFG icfg = new InfoflowCFG();


        DirectedGraph<Unit> ug = icfg.getOrCreateUnitGraph(mathod);
        Iterator<Unit> uit = ug.iterator();
        while (uit.hasNext()) {
            Unit u = uit.next();
            if (u.branches()) {
                System.out.println(u);
                List<Unit> list = icfg.getSuccsOf(u);
                System.out.println(list);
            }else if(icfg.isCallStmt(u)) {

            }else if(icfg.isReturnSite(u)) {

            }
        }
    }
}
