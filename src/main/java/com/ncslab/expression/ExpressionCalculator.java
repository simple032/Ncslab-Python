package com.ncslab.expression;

import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import java.util.HashMap;
import java.util.Map;

// 表达式计算器
public class ExpressionCalculator {
    private static final ExpressionParser parser = new ExpressionParser();
    private static final Map<String, Object> variables = new HashMap<String, Object>(){
        {
            put("pi", Math.PI);
//            put("e", Math.E);
        }
    };

    static public Object calculateExpression(String expression) {
        return calculateExpression(expression, variables);
    }

    static public Object calculateExpression(String expression, Map<String, Object> variables) {
        JexlExpression jexlExpression = parser.parseExpression(expression);
        MapContext context = new MapContext(variables);
        return jexlExpression.evaluate(context);
    }
}
