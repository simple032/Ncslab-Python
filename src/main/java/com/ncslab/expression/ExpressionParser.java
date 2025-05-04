package com.ncslab.expression;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;

import java.util.HashMap;
import java.util.Map;

// 表达式解析器
class ExpressionParser {
    private final JexlEngine jexl;
    private final Map<String, JexlExpression> expressionCache = new HashMap<>();

    public ExpressionParser() {
        this.jexl = new JexlBuilder().create();
    }

    public JexlExpression parseExpression(String expression) {
        if (expressionCache.containsKey(expression)) {
            return expressionCache.get(expression);
        }
        JexlExpression jexlExpression = jexl.createExpression(expression);
        expressionCache.put(expression, jexlExpression);
        return jexlExpression;
    }
}
