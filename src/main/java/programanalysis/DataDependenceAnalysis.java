package programanalysis;

import soot.Body;
import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.Stmt;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;
import java.util.Collections;

public class DataDependenceAnalysis {
    public static void main(String[] args) {
        // Configure Soot
        Options.v().set_prepend_classpath(true);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().setPhaseOption("cg.spark", "on");
        Options.v().setPhaseOption("cg.spark", "verbose:true");

        Options.v().set_process_dir(Collections.singletonList("C:\\Users\\admin\\cophi\\Closure1Bug\\build\\test"));

        // Load the target class
        SootClass targetClass = Scene.v().loadClassAndSupport("com.google.javascript.jscomp.CommandLineRunnerTest");
        Scene.v().loadNecessaryClasses();

        // Specify the method to analyze
        String targetMethodSignature = "void test(java.lang.String[],java.lang.String[],com.google.javascript.jscomp.DiagnosticType)";
        SootMethod targetMethod = findMethodBySignature(targetClass, targetMethodSignature);
        String targetVariableName = "r9";

        if (targetMethod != null && targetMethod.isConcrete()) {
            System.out.println("Analyzing method: " + targetMethod.getName());
            analyzeVariableDependenciesWithAliases(targetMethod, targetVariableName);
        } else {
            System.out.println("Method not found or is not concrete: " + targetMethodSignature);
        }
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

    public static void analyzeVariableDependenciesWithAliases(SootMethod method, String sourceVariableName) {
        Body body = method.retrieveActiveBody();

        // Map source variable name to Soot local variable
        Local targetLocal = findLocalVariable(body, sourceVariableName);

        if (targetLocal == null) {
            System.out.println("Variable not found: " + sourceVariableName);
            return;
        }

        System.out.println("Found local variable: " + targetLocal.getName());

        // Build the control flow graph
        UnitGraph cfg = new ExceptionalUnitGraph(body);

        // Perform reaching definitions analysis
        SimpleLocalDefs defs = new SimpleLocalDefs(cfg);

        // Perform alias analysis
        PointsToAnalysis pointsToAnalysis = Scene.v().getPointsToAnalysis();

        for (Unit unit : body.getUnits()) {
            if (unit instanceof Stmt) {
                Stmt stmt = (Stmt) unit;

                // Check if the statement uses the specified variable or an alias
                for (ValueBox useBox : stmt.getUseBoxes()) {
                    if (useBox.getValue() instanceof Local) {
                        Local usedLocal = (Local) useBox.getValue();

                        // Check aliasing
                        if (aliases(targetLocal, usedLocal, pointsToAnalysis)) {
                            System.out.println("Found use (or alias) of variable in statement: " + stmt);

                            // Find definitions reaching this use
                            for (Unit defUnit : defs.getDefsOfAt(usedLocal, stmt)) {
                                System.out.println("Definition: " + defUnit + " -> Use: " + stmt);
                            }
                        }
                    }
                }
            }
        }
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

        System.out.println("Points-to set for " + targetLocal.getName() + ": " + targetPointsToSet);
        System.out.println("Points-to set for " + usedLocal.getName() + ": " + usedPointsToSet);

        // Check if the two points-to sets have a non-empty intersection
        return targetPointsToSet.hasNonEmptyIntersection(usedPointsToSet);
    }
}
