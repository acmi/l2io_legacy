package acmi.l2.clientmod.unreal.bytecode;

public final class NativeFunction {
    private final String name;
    private final boolean preOperator;
    private final int operatorPrecedence;
    private final boolean operator;

    public NativeFunction(String name, boolean preOperator, int operatorPrecedence, boolean operator) {
        this.name = name;
        this.preOperator = preOperator;
        this.operatorPrecedence = operatorPrecedence;
        this.operator = operator;
    }

    public String getName() {
        return name;
    }

    public boolean isPreOperator() {
        return preOperator;
    }

    public int getOperatorPrecedence() {
        return operatorPrecedence;
    }

    public boolean isOperator() {
        return operator;
    }
}
