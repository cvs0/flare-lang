package net.cvs0.flare;

import net.cvs0.flare.ast.Statement;

import java.lang.reflect.Field;
import java.util.Deque;

/**
 * Simple debugger for breakpoints and stepping.
 */
public class Debugger {
    private int breakpointLine = -1;
    private String breakpointFunction = null;
    private boolean stepping = false;

    public void setBreakpointLine(int line) {
        this.breakpointLine = line;
    }
    public void setBreakpointFunction(String functionName) {
        this.breakpointFunction = functionName;
    }
    public void enableStepping(boolean enable) {
        this.stepping = enable;
    }

    public void checkBreakpoint(Statement stmt, Deque<String> callStack) {
        if (breakpointFunction != null && callStack.peek() != null && callStack.peek().contains(breakpointFunction)) {
            System.out.println("[Debugger] Breakpoint hit at function: " + breakpointFunction);
            printStack(callStack);
            waitForUser();
        }
        try {
            Field lineField = stmt.getClass().getDeclaredField("line");
            lineField.setAccessible(true);
            int line = (int) lineField.get(stmt);
            if (breakpointLine != -1 && line == breakpointLine) {
                System.out.println("[Debugger] Breakpoint hit at line: " + line);
                printStack(callStack);
                waitForUser();
            }
        } catch (Exception ignored) {}
        if (stepping) {
            System.out.println("[Debugger] Stepping at: " + callStack.peek());
            printStack(callStack);
            waitForUser();
        }
    }

    private void printStack(Deque<String> callStack) {
        System.out.println("[Debugger] Call stack:");
        for (String frame : callStack) {
            System.out.println("  at " + frame);
        }
    }

    private void waitForUser() {
        System.out.println("[Debugger] Press Enter to continue...");
        try {
            System.in.read();
        } catch (Exception ignored) {}
    }
}
