package programanalysis;

public class ExampleProgram {
    public static void main(String[] args) {
        A a = new A();
        A aliasOfA = createA();
        if ("string".length() == 1) {
            aliasOfA = a;
        }
        A a2 = createA();

        a.foo();
        aliasOfA.foo();
        a2.foo();
    }

    public static A createA() {
        return new A();
    }
}

class A {
    public void foo() {
        System.out.println("A.foo()");
    }
}
