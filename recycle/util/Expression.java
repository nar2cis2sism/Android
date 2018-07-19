package engine.android.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * 表达式计算工具
 * 
 * @author Daimon
 * @version 3.0
 * @since 4/16/2012
 */

public final class Expression {

    public static final String LEFT_BRACKET     = "﹙";
    public static final String RIGHT_BRACKET    = "﹚";
    public static final String ADD              = "＋";
    public static final String SUB              = "－";
    public static final String MUL              = "×";
    public static final String DIV              = "÷";
    public static final String PLUS             = "﹢";
    public static final String MINUS            = "﹣";
    public static final String EQU              = "＝";

    /***** 错误类型 *****/

    private static final int SYNTAX_ERROR       = 0;		// 语法错误

    private static final int PARAM_ERROR        = 1;		// 参数错误

    private static final int DIVISION_ERROR     = 2;	    // 除法错误

    /***** 错误提示 *****/

    private static final String[] ERROR_MESSAGE = { 
        "Syntax Error", 
        "Param Error",
        "Division by Zero" };

    private HashMap<String, String> varList;                // 变量表

    private String expression;                              // 表达式串

    private int leftBracketLevel;                           // 左括号层级

    private double result;                                  // 计算结果

    public Expression() {
        varList = new HashMap<String, String>();
    }

    public Expression(String exp) {
        this();
        setExpression(exp);
    }

    /**
     * 设置表达式
     */

    public void setExpression(String exp) {
        exp = exp.replaceAll(LEFT_BRACKET, "(");
        exp = exp.replaceAll(RIGHT_BRACKET, ")");
        exp = exp.replaceAll(ADD, "+");
        exp = exp.replaceAll(SUB, "-");
        exp = exp.replaceAll(MUL, "*");
        exp = exp.replaceAll(DIV, "/");
        exp = exp.replaceAll(PLUS, "+");
        exp = exp.replaceAll(MINUS, "-");
        expression = exp;
        getVar();
    }

    /**
     * 分析变量
     */

    private void getVar() {
        LinkedList<String> list = new LinkedList<String>();
        int pos = 0;
        String element = "";
        char c;
        while (pos < expression.length())
        {
            c = expression.charAt(pos);
            if (isLetter(c))
            {
                element += c;
                pos++;
                continue;
            }
            else if (Character.isDigit(c))
            {
                if (element.length() > 0)
                {
                    element += c;
                }

                pos++;
                continue;
            }

            if (element.length() > 0)
            {
                list.add(element);
                element = "";
            }

            pos++;
        }

        if (element.length() > 0)
        {
            list.add(element);
            element = "";
        }

        varList.clear();
        for (String var : list)
        {
            if (!varList.containsKey(var))
            {
                varList.put(var, "");
            }
        }
    }

    /**
     * 计算表达式返回结果
     */

    public double calculate() throws Exception {
        leftBracketLevel = 0;
        parseExpression(expression);
        return result;
    }

    /**
     * 递归解析表达式
     */

    private void parseExpression(String exp) throws Exception {
        int leftPos = -1;
        int rightPos = -1;
        int level = 0;
        int pos = 0;
        char c;
        while (pos < exp.length())
        {
            c = exp.charAt(pos);
            if (c == '(')
            {
                if (++leftBracketLevel > level)
                {
                    level = leftBracketLevel;
                    leftPos = pos;
                    rightPos = exp.indexOf(')', leftPos);
                    if (rightPos == -1)
                    {
                        handleError(SYNTAX_ERROR);
                    }
                }
            }
            else if (c == ')')
            {
                leftBracketLevel--;
            }

            pos++;
        }

        if (leftBracketLevel != 0)
        {
            handleError(SYNTAX_ERROR);
        }

        StringBuilder sb = new StringBuilder();
        if (leftPos > -1)
        {
            sb.append(exp.substring(leftPos + 1, rightPos));
        }
        else
        {
            sb.append(exp);
        }

        LinkedList<String> list = new LinkedList<String>();
        pos = 0;
        String element = "";
        while (pos < sb.length())
        {
            c = sb.charAt(pos);
            if (c == ' ')
            {
                pos++;
                continue;
            }
            else if (isDelimiter(c))
            {
                if (element.length() > 0)
                {
                    list.add(element);
                    element = String.valueOf(c);
                }
                else
                {
                    element = String.valueOf(c);
                    pos++;
                    continue;
                }
            }
            else if (isLetter(c) || Character.isDigit(c) || c == '.')
            {
                element += c;
                pos++;
                continue;
            }

            if (element.length() > 0)
            {
                list.add(element);
                element = "";
            }

            pos++;
        }

        if (element.length() > 0)
        {
            list.add(element);
            element = "";
        }

        if (!list.isEmpty())
        {
            result = Double.parseDouble(list.getFirst());
        }

        // 根据优先级进行运算
        int index = -1;
        int index1 = -1;
        int index2 = -1;
        String x1 = "";
        String x2 = "";
        String op;
        double x3 = 0;
        String[] ops = { "*", "/", "+", "-" };
        for (int i = 0; i < ops.length;)
        {
            index = -1;
            index1 = list.indexOf(ops[i]);
            index2 = list.indexOf(ops[i + 1]);
            op = ops[i];
            if (index1 != -1)
            {
                index = index1;
            }

            if (index2 != -1)
            {
                if (index == -1 || index > index2)
                {
                    index = index2;
                    op = ops[i + 1];
                }
            }

            if (index == -1)
            {
                i += 2;
                continue;
            }
            else if (index > 0 && index < list.size() - 1)
            {
                x1 = list.get(index - 1);
                x2 = list.get(index + 1);
                x3 = parseExpression(x1, x2, op);
                result = x3;
                // 替换
                list.set(index, String.valueOf(x3));
                // 删除
                list.remove(index + 1);
                list.remove(index - 1);
            }
            else
            {
                handleError(PARAM_ERROR);
            }
        }

        if (leftPos > -1)
        {
            exp = exp.substring(0, leftPos) + list.get(0)
                    + exp.substring(rightPos + 1, exp.length());
            if (exp.length() > 0)
            {
                parseExpression(exp);
            }
        }
    }

    private double parseExpression(String exp1, String exp2, String operation)
            throws Exception {
        BigDecimal x1 = null;
        BigDecimal x2 = null;
        try {
            if (isLetter(exp1.charAt(0)))
            {
                x1 = new BigDecimal(getParameter(exp1));
            }
            else
            {
                x1 = new BigDecimal(exp1);
            }

            if (isLetter(exp2.charAt(0)))
            {
                x2 = new BigDecimal(getParameter(exp2));
            }
            else
            {
                x2 = new BigDecimal(exp2);
            }
        } catch (Exception e) {
            handleError(PARAM_ERROR);
        }

        // 四则小运算
        double result = 0;
        if (operation.equals("+"))
        {
            result = x1.add(x2).doubleValue();
        }
        else if (operation.equals("-"))
        {
            result = x1.subtract(x2).doubleValue();
        }
        else if (operation.equals("*"))
        {
            result = x1.multiply(x2).doubleValue();
        }
        else if (operation.equals("/"))
        {
            if (x2.doubleValue() != 0)
            {
                result = x1.divide(x2, 20, BigDecimal.ROUND_DOWN).doubleValue();
            }
            else
            {
                handleError(DIVISION_ERROR);
            }
        }

        return result;
    }

    private void handleError(int errorType) throws Exception {
        // 遇到异常情况时，根据错误类型，取得错误提示信息并封装在异常中抛出去
        throw new Exception(ERROR_MESSAGE[errorType]);
    }

    /**
     * 对表达式中的参数进行赋值
     */

    public void setParameter(String param, String value) {
        if (varList.containsKey(param))
        {
            varList.put(param, value);
        }
    }

    private String getParameter(String param) {
        return varList.get(param);
    }

    private boolean isLetter(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private boolean isDelimiter(char c) {
        return "+-*/".indexOf(c) != -1;
    }
}