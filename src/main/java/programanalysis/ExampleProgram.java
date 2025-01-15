package programanalysis;

public class ExampleProgram {
    public static void main(String[] args) {
        A a = new A();
        B b = new B();
        A aliasOfA = new A();
        if ("string".length() == 1) {
            aliasOfA = a;
        }
        A a2 = new A();

        a.foo();
        aliasOfA.foo();
        a2.foo();
        b.bar();
    }
}

class A {
    public void foo() {
        System.out.println("A.foo()");
    }
}

class B {
    public void bar() {
        System.out.println("B.bar()");
    }
}
