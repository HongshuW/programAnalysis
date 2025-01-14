package programanalysis;

public class ExampleProgram {
    public static void main(String[] args) {
        A a = new A();
        B b = new B();
        A aliasOfA = a;
        if ("s".length() == 1) {
            A x = aliasOfA;
            x.foo();
        }

        a.foo();
        aliasOfA.foo();
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
