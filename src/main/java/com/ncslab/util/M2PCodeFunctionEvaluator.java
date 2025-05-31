package com.ncslab.util;

import org.apache.commons.jexl3.*;
import java.util.HashMap;
import java.util.Map;

public class M2PCodeFunctionEvaluator {
    public static class UFunction {
        private double[] data;

        public void setData(double[] data) {
            this.data = data;
        }

        public double call(int index) {
            return data[index - 1];
        }
    }

    public static class Evaluator {
        private final JexlEngine jexl;
        private final JexlExpression compiledExpression;
        private final UFunction uFunction;
        private final JexlContext context;

        public Evaluator(String expression) {
            this.jexl = new JexlBuilder().create();
            this.uFunction = new UFunction();
            this.context = new MapContext();
            this.context.set("u", uFunction);

            // 预编译表达式（关键优化点）
            this.compiledExpression = jexl.createExpression(expression);
        }

        // 高性能计算方法
        public double evaluate(double[] inputData) {
            uFunction.setData(inputData);  // 更新数据（避免创建新对象）
            return Double.parseDouble(compiledExpression.evaluate(context).toString());
        }
    }

    // 使用示例：
    public static void main(String[] args) {
        String fixedExpression = "1 + u(1) + u(2)";
        Evaluator evaluator = new Evaluator(fixedExpression);

        // 模拟频繁调用（仅输入数据变化）
        for (int i = 0; i < 1000; i++) {
            double[] input = {i, i + 1};
            double result = evaluator.evaluate(input);
        }
    }
}
