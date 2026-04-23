package com.ncslab.block.stateflow;

import java.util.Map;

/**
 * Stateflow 动作代码轻量级表达式求值器。
 * <p>
 * 支持基本的算术、比较和逻辑运算，用于在 Java 快速仿真中执行 Stateflow 动作代码。
 * </p>
 * <p>支持的语法：</p>
 * <ul>
 *   <li>数字字面量：0, 1.5, -3</li>
 *   <li>变量名：var1, x（通过外部 Map 提供值）</li>
 *   <li>算术运算符：+, -, *, /, %, ^</li>
 *   <li>比较运算符：>, <, >=, <=, ==, !=</li>
 *   <li>逻辑运算符：&&, ||</li>
 *   <li>括号：( )</li>
 *   <li>一元负号</li>
 * </ul>
 */
public class SfExpressionEvaluator {

    private final String expr;
    private int pos;
    private final Map<String, Double> variables;

    public SfExpressionEvaluator(String expr, Map<String, Double> variables) {
        this.expr = expr != null ? expr : "";
        this.variables = variables;
        this.pos = 0;
    }

    /**
     * 求值表达式，返回数值结果。
     * 对于逻辑运算，true = 1.0，false = 0.0。
     */
    public double evaluate() {
        skipWhitespace();
        if (pos >= expr.length()) {
            return 0.0;
        }
        double result = parseOr();
        skipWhitespace();
        if (pos < expr.length()) {
            // 忽略尾部未解析的内容（可能有多余分号等）
        }
        return result;
    }

    /**
     * 求值条件表达式，返回布尔结果。
     */
    public boolean evaluateBoolean() {
        return evaluate() != 0.0;
    }

    // ===== 递归下降解析 =====

    // or_expr = and_expr ( "||" and_expr )*
    private double parseOr() {
        double result = parseAnd();
        while (true) {
            skipWhitespace();
            if (match("||")) {
                double right = parseAnd();
                result = (result != 0.0 || right != 0.0) ? 1.0 : 0.0;
            } else {
                break;
            }
        }
        return result;
    }

    // and_expr = equality_expr ( "&&" equality_expr )*
    private double parseAnd() {
        double result = parseEquality();
        while (true) {
            skipWhitespace();
            if (match("&&")) {
                double right = parseEquality();
                result = (result != 0.0 && right != 0.0) ? 1.0 : 0.0;
            } else {
                break;
            }
        }
        return result;
    }

    // equality_expr = comparison_expr ( ("==" | "!=") comparison_expr )*
    private double parseEquality() {
        double result = parseComparison();
        while (true) {
            skipWhitespace();
            if (match("==")) {
                double right = parseComparison();
                result = (Math.abs(result - right) < 1e-9) ? 1.0 : 0.0;
            } else if (match("!=")) {
                double right = parseComparison();
                result = (Math.abs(result - right) >= 1e-9) ? 1.0 : 0.0;
            } else {
                break;
            }
        }
        return result;
    }

    // comparison_expr = additive_expr ( (">" | "<" | ">=" | "<=") additive_expr )*
    private double parseComparison() {
        double result = parseAdditive();
        while (true) {
            skipWhitespace();
            if (match(">=")) {
                double right = parseAdditive();
                result = (result >= right) ? 1.0 : 0.0;
            } else if (match("<=")) {
                double right = parseAdditive();
                result = (result <= right) ? 1.0 : 0.0;
            } else if (match(">")) {
                double right = parseAdditive();
                result = (result > right) ? 1.0 : 0.0;
            } else if (match("<")) {
                double right = parseAdditive();
                result = (result < right) ? 1.0 : 0.0;
            } else {
                break;
            }
        }
        return result;
    }

    // additive_expr = multiplicative_expr ( ("+" | "-") multiplicative_expr )*
    private double parseAdditive() {
        double result = parseMultiplicative();
        while (true) {
            skipWhitespace();
            if (match("+")) {
                double right = parseMultiplicative();
                result = result + right;
            } else if (match("-")) {
                double right = parseMultiplicative();
                result = result - right;
            } else {
                break;
            }
        }
        return result;
    }

    // multiplicative_expr = power_expr ( ("*" | "/" | "%") power_expr )*
    private double parseMultiplicative() {
        double result = parsePower();
        while (true) {
            skipWhitespace();
            if (match("*")) {
                double right = parsePower();
                result = result * right;
            } else if (match("/")) {
                double right = parsePower();
                result = result / right;
            } else if (match("%")) {
                double right = parsePower();
                result = result % right;
            } else {
                break;
            }
        }
        return result;
    }

    // power_expr = unary_expr ( "^" unary_expr )*
    private double parsePower() {
        double result = parseUnary();
        while (true) {
            skipWhitespace();
            if (match("^")) {
                double right = parseUnary();
                result = Math.pow(result, right);
            } else {
                break;
            }
        }
        return result;
    }

    // unary_expr = ("-" | "+") unary_expr | primary
    private double parseUnary() {
        skipWhitespace();
        if (match("-")) {
            return -parseUnary();
        }
        if (match("+")) {
            return parseUnary();
        }
        return parsePrimary();
    }

    // primary = NUMBER | VARIABLE | "(" expression ")"
    private double parsePrimary() {
        skipWhitespace();
        if (match("(")) {
            double result = parseOr();
            skipWhitespace();
            if (!match(")")) {
                // 忽略缺少的右括号，尽量继续
            }
            return result;
        }

        // 尝试解析数字
        String numberStr = readNumber();
        if (numberStr != null && !numberStr.isEmpty()) {
            try {
                return Double.parseDouble(numberStr);
            } catch (NumberFormatException e) {
                //  fallback
            }
        }

        // 解析变量名
        String varName = readIdentifier();
        if (varName != null && !varName.isEmpty()) {
            Double val = variables != null ? variables.get(varName) : null;
            if (val != null) {
                return val;
            }
            // 变量不存在时返回 0
            return 0.0;
        }

        // 无法解析，返回 0
        if (pos < expr.length()) {
            pos++;
        }
        return 0.0;
    }

    // ===== 辅助方法 =====

    private void skipWhitespace() {
        while (pos < expr.length() && Character.isWhitespace(expr.charAt(pos))) {
            pos++;
        }
    }

    private boolean match(String op) {
        skipWhitespace();
        if (expr.startsWith(op, pos)) {
            pos += op.length();
            return true;
        }
        return false;
    }

    private String readNumber() {
        skipWhitespace();
        int start = pos;
        boolean hasDot = false;
        boolean hasExp = false;
        while (pos < expr.length()) {
            char c = expr.charAt(pos);
            if (c >= '0' && c <= '9') {
                pos++;
            } else if (c == '.' && !hasDot && !hasExp) {
                hasDot = true;
                pos++;
                // 允许前导小数点如 .5
                if (pos < expr.length() && expr.charAt(pos) >= '0' && expr.charAt(pos) <= '9') {
                    // 继续读取数字
                }
            } else if ((c == 'e' || c == 'E') && !hasExp) {
                hasExp = true;
                pos++;
                // 允许指数符号
                if (pos < expr.length() && (expr.charAt(pos) == '+' || expr.charAt(pos) == '-')) {
                    pos++;
                }
            } else {
                break;
            }
        }
        if (start == pos) {
            return null;
        }
        // 如果只读取了一个点，不算数字
        if (pos == start + 1 && expr.charAt(start) == '.') {
            pos = start;
            return null;
        }
        return expr.substring(start, pos);
    }

    private String readIdentifier() {
        skipWhitespace();
        int start = pos;
        while (pos < expr.length()) {
            char c = expr.charAt(pos);
            if (Character.isLetterOrDigit(c) || c == '_') {
                pos++;
            } else {
                break;
            }
        }
        if (start == pos) {
            return null;
        }
        return expr.substring(start, pos);
    }
}
