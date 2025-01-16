package programanalysis.sootconfig;

import soot.Body;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;

public abstract class SootConfiguration {

    public abstract void setClassPath();

    public abstract SootMethod getTargetMethod();

    public abstract Local getTargetVariable(Body methodBody);

    public SootMethod findMethodBySignature(SootClass sootClass, String methodSignature) {
        for (SootMethod method : sootClass.getMethods()) {
            if (method.getSubSignature().equals(methodSignature)) {
                return method;
            }
        }
        return null;
    }

    public Local findLocalVariable(Body body, String sourceVariableName) {
        for (Local local : body.getLocals()) {
            if (local.getName().equals(sourceVariableName)) {
                return local;
            }
        }
        return null;
    }

}
