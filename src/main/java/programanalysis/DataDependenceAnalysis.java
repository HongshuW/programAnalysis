package programanalysis;

import soot.Body;
import soot.G;
import soot.Local;
import soot.PackManager;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.Stmt;
import soot.jimple.spark.SparkTransformer;
import soot.options.Options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataDependenceAnalysis {
    public static void main(String[] args) {
        G.reset();

        /* Configure Soot */
        Options.v().set_keep_line_number(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(Options.output_format_none);
        Options.v().setPhaseOption("cg.spark", "on");
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().setPhaseOption("jb", "verbose:true");


        /* Load class */

        // Options.v().set_process_dir(Collections.singletonList("C:\\Users\\admin\\cophi\\Closure1Bug\\build\\test"));
        // Options.v().set_soot_classpath("C:\\Users\\admin\\cophi\\Closure1Bug\\build;C:\\Program Files\\Java\\jdk1.8.0_202\\jre\\lib\\rt.jar");

        Options.v().set_process_dir(Collections.singletonList("C:\\Users\\admin\\cophi\\programAnalysis\\target\\classes"));
        Options.v().set_soot_classpath("C:\\Users\\admin\\cophi\\programAnalysis\\target;C:\\Program Files\\Java\\jdk1.8.0_202\\jre\\lib\\rt.jar");

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
        List<SootMethod> entryPoints = new ArrayList<>();
        entryPoints.add(Scene.v().getMethod("<programanalysis.ExampleProgram: void main(java.lang.String[])>"));
        Scene.v().setEntryPoints(entryPoints);

        /* Points-to Analysis */
        PackManager.v().runPacks();
        PointsToAnalysis pa = Scene.v().getPointsToAnalysis();

        // // Load the target class
        // SootClass targetClass = Scene.v().loadClassAndSupport("com.google.javascript.jscomp.CommandLineRunnerTest");

        // // Specify the method to analyze
        // String targetMethodSignature = "void test(java.lang.String[],java.lang.String[],com.google.javascript.jscomp.DiagnosticType)";
        // SootMethod targetMethod = findMethodBySignature(targetClass, targetMethodSignature);

        // Load the target class
        SootClass targetClass = Scene.v().loadClassAndSupport("programanalysis.ExampleProgram");
        // Specify the method to analyze
        SootMethod targetMethod = targetClass.getMethodByName("main");

        Body body = targetMethod.retrieveActiveBody();
        Local targetLocal = findLocalVariable(body, "aliasOfA");

        for (Unit unit : body.getUnits()) {
            if (unit instanceof Stmt) {
                Stmt stmt = (Stmt) unit;

                // Check if the statement uses the specified variable or an alias
                for (ValueBox useBox : stmt.getUseBoxes()) {
                    if (useBox.getValue() instanceof Local) {
                        Local usedLocal = (Local) useBox.getValue();

                        // Check aliasing
                        if (aliases(targetLocal, usedLocal, pa)) {
                            System.out.println(usedLocal + " in " + stmt);

                            // // Find definitions reaching this use
                            // for (Unit defUnit : defs.getDefsOfAt(usedLocal, stmt)) {
                            //     System.out.println("Definition: " + defUnit + " -> Use: " + stmt);
                            // }
                        }
                    }
                }
            }
        }

        // for (Local local : body.getLocals()) {
        //     PointsToSet pts = pa.reachingObjects(local);
        //     if (pts.isEmpty()) {
        //         System.out.println("PointsToSet is empty for variable: " + local.getName());
        //     } else {
        //         System.out.println("PointsToSet for " + local.getName() + ": " + pts.possibleTypes());
        //     }
        // }
    }

    public static SootMethod findMethodBySignature(SootClass sootClass, String methodSignature) {
        // Find the method in the class
        for (SootMethod method : sootClass.getMethods()) {
            if (method.getSubSignature().equals(methodSignature)) {
                return method;
            }
        }
        return null; // Return null if the method is not found
    }

    public static Local findLocalVariable(Body body, String sourceVariableName) {
        for (Local local : body.getLocals()) {
            if (local.getName().equals(sourceVariableName)) {
                return local;
            }
        }
        return null;
    }

    public static boolean aliases(Local targetLocal, Local usedLocal, PointsToAnalysis pointsToAnalysis) {
        if (targetLocal.equals(usedLocal)) {
            // Same variable: trivially aliases itself
            return true;
        }
        PointsToSet targetPointsToSet = pointsToAnalysis.reachingObjects(targetLocal);
        PointsToSet usedPointsToSet = pointsToAnalysis.reachingObjects(usedLocal);
        // Check if the two points-to sets have a non-empty intersection
        return targetPointsToSet.hasNonEmptyIntersection(usedPointsToSet);
    }

}
