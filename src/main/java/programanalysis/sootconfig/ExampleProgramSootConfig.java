package programanalysis.sootconfig;

import java.util.Collections;

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;

public class ExampleProgramSootConfig extends SootConfiguration {

    @Override
    public void setClassPath() {
        Options.v().set_process_dir(Collections.singletonList("C:\\Users\\admin\\cophi\\programAnalysis\\target\\classes"));
        Options.v().set_soot_classpath("C:\\Users\\admin\\cophi\\programAnalysis\\target;C:\\Program Files\\Java\\jdk1.8.0_202\\jre\\lib\\rt.jar");
    }

    @Override
    public SootMethod getTargetMethod() {
        // Load the target class
        SootClass targetClass = Scene.v().loadClassAndSupport("programanalysis.ExampleProgram");
        // Specify the method to analyze
        SootMethod targetMethod = targetClass.getMethodByName("main");

        return targetMethod;
    }

    @Override
    public Local getTargetVariable(Body methodBody) {
        return findLocalVariable(methodBody, "aliasOfA");
    }
    
}
