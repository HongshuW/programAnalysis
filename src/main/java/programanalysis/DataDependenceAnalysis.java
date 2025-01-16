package programanalysis;

import soot.Body;
import soot.EntryPoints;
import soot.G;
import soot.Local;
import soot.PackManager;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.toolkits.scalar.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import programanalysis.sootconfig.ClosureSootConfig;
import programanalysis.sootconfig.ExampleProgramSootConfig;
import programanalysis.sootconfig.SootConfiguration;

public class DataDependenceAnalysis {
    public static void main(String[] args) {
        G.reset();

        /* Configure Soot */
        Options.v().set_keep_line_number(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(Options.output_format_none);
        Options.v().set_include_all(true);

        Options.v().setPhaseOption("cg.spark", "on");
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().setPhaseOption("jb", "verbose:true");

        // SootConfiguration sootConfig = new ClosureSootConfig();
        SootConfiguration sootConfig = new ExampleProgramSootConfig();

        /* Load class */
        sootConfig.setClassPath();

        Scene.v().loadNecessaryClasses();

        /* Configure Spark */
        Map<String,String> sparkOptions = new HashMap<>();
        sparkOptions.put("enabled", "true");
        sparkOptions.put("verbose", "true");
        sparkOptions.put("propagator", "worklist");
        sparkOptions.put("simple-edges-bidirectional", "false");
        sparkOptions.put("on-fly-cg", "true");
        sparkOptions.put("set-impl", "double");
        sparkOptions.put("double-set-old", "hybrid");
        sparkOptions.put("double-set-new", "hybrid");
        sparkOptions.put("context", "true");
        sparkOptions.put("field-based", "true");
        sparkOptions.put("types-for-sites", "true");
        sparkOptions.put("merge-stringbuffer", "true");
        sparkOptions.put("string-constants", "true");
        SparkTransformer.v().transform("", sparkOptions);

        /* Set entry point */
        Scene.v().setEntryPoints(EntryPoints.v().all());

        /* Points-to Analysis */
        PackManager.v().runPacks();
        CallGraph cg = Scene.v().getCallGraph();
        PointsToAnalysis pta = Scene.v().getPointsToAnalysis();

        /* Load target method */
        SootMethod targetMethod = sootConfig.getTargetMethod();

        Body body = targetMethod.retrieveActiveBody();

        /* Load target local variable */
        Local targetLocal = sootConfig.getTargetVariable(body);

        for (Unit unit : body.getUnits()) {
            if (unit instanceof Stmt) {
                Stmt stmt = (Stmt) unit;

                // Check if the statement uses the specified variable or an alias
                for (ValueBox useBox : stmt.getUseBoxes()) {
                    if (useBox.getValue() instanceof Local) {
                        Local usedLocal = (Local) useBox.getValue();

                        // Check aliasing
                        if (aliases(targetLocal, usedLocal, pta)) {
                            System.out.println(usedLocal + " in " + unit);
                        }
                    }
                }
            }
        }

        Set<Local> aliases = findInterProceduralAliases(targetMethod, targetLocal, pta, cg);
        System.out.println("Inter-procedural aliases: " + aliases);

    }

    public static boolean aliases(Local targetLocal, Local usedLocal, PointsToAnalysis pointsToAnalysis) {
        if (targetLocal.equals(usedLocal)) {
            return true;
        }
        PointsToSet targetPointsToSet = pointsToAnalysis.reachingObjects(targetLocal);
        PointsToSet usedPointsToSet = pointsToAnalysis.reachingObjects(usedLocal);
        // Check if the two points-to sets have a non-empty intersection
        return targetPointsToSet.hasNonEmptyIntersection(usedPointsToSet);
    }

    private static Set<Local> findInterProceduralAliases(SootMethod method, Local variable, PointsToAnalysis pta, CallGraph cg) {
        Set<Local> aliases = new HashSet<>();

        // Get points-to set for the variable
        PointsToSet pointsToSet = pta.reachingObjects(variable);
        if (pointsToSet == null) {
            return aliases;
        }

        // Traverse the call graph
        Iterator<Edge> edges = cg.edgesInto(method);

        while (edges.hasNext()) {
            Edge edge = edges.next();
            SootMethod targetMethod = edge.getTgt().method();

            if (!targetMethod.hasActiveBody()) {
                continue;
            }

            // Map arguments to formal parameters
            Pair<Value, Local> aliasInfo = mapAliases(edge, variable, pointsToSet);
            if (aliasInfo != null && aliasInfo.getO2() != null) {
                aliases.add(aliasInfo.getO2());
            }
        }
        return aliases;
    }

    private static Pair<Value, Local> mapAliases(Edge edge, Local variable, PointsToSet pointsToSet) {
        if (edge.srcStmt().containsInvokeExpr()) {
            InvokeExpr invokeExpr = edge.srcStmt().getInvokeExpr();
            for (int i = 0; i < invokeExpr.getArgs().size(); i++) {
                Value arg = invokeExpr.getArg(i);
                if (arg.equivTo(variable)) {
                    SootMethod targetMethod = edge.getTgt().method();
                    if (targetMethod.hasActiveBody()) {
                        Local param = (Local) targetMethod.getActiveBody().getParameterLocal(i);
                        if (param != null && Scene.v().getPointsToAnalysis().reachingObjects(param).hasNonEmptyIntersection(pointsToSet)) {
                            return new Pair<>(arg, param);
                        }
                    }
                }
            }
        }
        return null;
    }

}
