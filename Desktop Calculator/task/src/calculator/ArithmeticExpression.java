package calculator;

import java.util.ArrayDeque;
import java.util.Deque;

import static java.lang.Character.isDigit;

public class ArithmeticExpression {

    private String expression;

    public ArithmeticExpression(String expression) {
        this.expression = expression;
    }

    public ArithmeticExpression() {
        this.expression = "";
    }

    public String getExpression() {return this.expression;}

    public String setExpression(String expression) {
        this.expression = expression;
        return expression;
    }

    public String append(char c) {
        int length;
        StringBuilder sb = new StringBuilder(expression);
        char prev;
        if (expression == null) {
            length = 0;
        } else {
            length = expression.length();
        }
        if (length > 0) {
            prev = expression.charAt(length - 1);
        } else {
            prev = ' ';
        }
        if ((length == 0) && Calculator.isOperator(c)) {
            //The equation must not start with an operator
            Calculator.beep(1, 1);
        } else if (length > 0 && Calculator.isOperator(c) && Calculator.isOperator(prev)) {
            //Operator replaces old operator
            Calculator.beep(1, 1);
            sb.setCharAt(sb.length() - 1, c);
        } else if (Calculator.isOperator(c) && (isDigit(prev) || prev == '.')) {
            //Add zeros to numbers that start or ends with dot
            StringBuilder sbNum = new StringBuilder();
            for (int i = length -1; i >= 0; i--) {
                if (isDigit(expression.charAt(i)) || expression.charAt(i) == '.' ) {
                    sbNum.insert(0, expression.charAt(i));
                } else {
                    break;
                }
            }
            if (sbNum.charAt(0) == '.') {
                sb.insert(length - sbNum.length(), '0');
                Calculator.beep(1,1);
            } else if (sbNum.charAt(sbNum.length() - 1) == '.') {
                sb.append('0');
            }
            sb.append(c);
        } else if (c == '.' && length > 0 && prev == '.') {
            Calculator.beep(1, 1);
        } else {
            sb.append(c);
        }
        expression = sb.toString();
        return expression;

    }

    public String clear() {
        expression = "";
        return "";
    }

    public String delete() {
        if (expression != null & expression.length() > 0) {
            expression = expression.substring(0,expression.length() - 1);
        } else {
            expression = "";
        }
        return expression;
    }

    public boolean isEmpty() {
        if (expression == null || expression.length() == 0) {
            expression = "";
            return true;
        } else {
            return false;
        }
    }

    public char getLastChar() {
        return expression.charAt(expression.length() - 1);
    }

    public double evaluate() {
        Deque<Double> numbersStack = new ArrayDeque<>();
        Deque<Character> operatorStack = new ArrayDeque<>();
        insertZeroBeforeOperator();
        char[] chars = expression.toCharArray();
        boolean hasDecimals = false;
        boolean isValid = true;

        for (int i = 0; i < chars.length; i++) {
            if (Character.isWhitespace(chars[i])) //Skip over white spaces
                continue;

            if (Character.isDigit(chars[i]) || chars[i] == '.') {
                hasDecimals = false;
                StringBuilder sb = new StringBuilder();
                while (i < chars.length && (Character.isDigit(chars[i]) || (!hasDecimals && chars[i] == '.'))) {
                    hasDecimals = hasDecimals || chars[i] == '.';
                    sb.append(chars[i++]);
                }
                numbersStack.offerFirst(Double.parseDouble(sb.toString()));
                if (!(i == chars.length)) {
                    i--;
                    continue;
                } else {
                    break;
                }


            } else if (chars[i] == Calculator.LEFT_BRACKET) {
                operatorStack.offerFirst(chars[i]);
            } else if (chars[i] == Calculator.RIGHT_BRACKET) {
                while (operatorStack.peekFirst() != Calculator.LEFT_BRACKET) {
                    if (Calculator.isBinOperator(operatorStack.peekFirst())) {
                        numbersStack.offerFirst(applyOperator(operatorStack.pollFirst(), numbersStack.pollFirst(), numbersStack.pollFirst()));
                    } else {
                        numbersStack.offerFirst(applyOperator(operatorStack.pollFirst(), numbersStack.pollFirst(), 0));

                    }
                }
                operatorStack.pollFirst();
            } else if (Calculator.isOperator(chars[i])) {
                while (!operatorStack.isEmpty() && hasPrecedence(chars[i], operatorStack.peekFirst())) {
                    if (Calculator.isBinOperator(operatorStack.peekFirst())) {
                        numbersStack.offerFirst(applyOperator(operatorStack.pollFirst(), numbersStack.pollFirst(), numbersStack.pollFirst()));
                    } else {
                        numbersStack.offerFirst(applyOperator(operatorStack.pollFirst(), numbersStack.pollFirst(), 0));
                    }
                }
                operatorStack.offerFirst(chars[i]);
            }
        }
        /*
        System.out.println("Finished for loop");
        System.out.println(numbersStack.toString());
        System.out.println(operatorStack.toString());
        */

        while (!operatorStack.isEmpty()) {
            if (Calculator.isBinOperator(operatorStack.peekFirst())) {
                numbersStack.offerFirst(applyOperator(operatorStack.pollFirst(), numbersStack.pollFirst(), numbersStack.pollFirst()));
            } else {
                numbersStack.offerFirst(applyOperator(operatorStack.pollFirst(), numbersStack.pollFirst(), 0));

            }
        }

        return numbersStack.pollFirst();
    }

    private static double applyOperator(char operator, double b, double a) {
        switch (operator) {
            case Calculator.ADDITION:
                return a + b;
            case Calculator.SUBTRACTION:
                return a - b;
            case Calculator.MULTIPLICATION:
                return a * b;
            case Calculator.DIVISION:
                if (b == 0) throw new UnsupportedOperationException("Cannot divide by zero");
                return a / b;
            case Calculator.SQRT:
                return Math.sqrt(b);
            case Calculator.PWR:
                return Math.pow(a, b);
            default:
                throw new UnsupportedOperationException("Unknown operation: " + operator);
        }
    }

    private static boolean hasPrecedence(char op1, char op2) {
        if (op2 == Calculator.LEFT_BRACKET || op2 == Calculator.RIGHT_BRACKET) {
            return false;
        } else if (Calculator.operatorPriority(op1) < Calculator.operatorPriority(op2)) {
            return false;
        } else {
            return true;
        }
    }

    private int countLeftBrackets() {
        int count = 0;
        char[] chars = expression.toCharArray();
        for (char c : chars) {
            if (c == Calculator.LEFT_BRACKET) count++;
        }
        return count;
    }

    private int countRightBrackets() {
        int count = 0;
        char[] chars = expression.toCharArray();
        for (char c : chars) {
            if (c == Calculator.RIGHT_BRACKET) count++;
        }
        return count;
    }

    public boolean countLeftRightBracketsEqual() {
        return (countLeftBrackets() == countRightBrackets());
    }

    private int countOperators() {
        int count = 0;
        char[] chars = expression.toCharArray();
        for(char c: chars) {
            if (Calculator.isOperator(c)) count++;
        }
        return count;
    }

    private String insertZeroBeforeOperator() {
        /*This method insert zero before operators at the beginning of the expression
        or following a left bracket
         */
        StringBuilder sb = new StringBuilder();
        char[] chars = expression.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i == 0 && Calculator.isOperator(chars[i])) {
                sb.append('0');
                sb.append(chars[i]);
            } else if (Calculator.isOperator(chars[i]) && chars[i-1] == Calculator.LEFT_BRACKET) {
                sb.append('0');
                sb.append(chars[i]);
            } else {
                sb.append(chars[i]);
            }
        }
        return  expression = sb.toString();
    }

    public String appendBracket() {
        if (countLeftBrackets() == countRightBrackets()) {
            expression = expression + Character.toString(Calculator.LEFT_BRACKET);
        } else if (getLastChar() == Calculator.LEFT_BRACKET || Calculator.isOperator(getLastChar())) {
            expression  = expression + Character.toString(Calculator.LEFT_BRACKET);
        } else {
            expression  = expression + Character.toString(Calculator.RIGHT_BRACKET);
        }
        return expression;
    }

    public String appendSQRT() {
        expression = expression + Character.toString(Calculator.SQRT) + Character.toString(Calculator.LEFT_BRACKET);
        return expression;
    }

    public String negate() {
        int length;
        String leftBracketMinus = Character.toString(Calculator.LEFT_BRACKET) + Character.toString(Calculator.SUBTRACTION);
        if (expression == null) {
            expression = "";
            length = 0;
        } else {
            length = expression.length();
        }
        if (length == 0) {
            expression = leftBracketMinus;
        } else if (length >= 2 && expression.substring(length-2).equals(leftBracketMinus)) {
            expression = expression.substring(0, length - 2);
        } else if (length >= 2 && expression.substring(0, 2).equals(leftBracketMinus)) {
            expression = expression.substring(2);
        } else if (countOperators() == 0) {
            expression = leftBracketMinus + expression;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(Calculator.LEFT_BRACKET);
            sb.append('0');
            sb.append(Calculator.SUBTRACTION);
            sb.append(Calculator.LEFT_BRACKET);
            sb.append(expression);
            sb.append(Calculator.RIGHT_BRACKET);
            sb.append(Calculator.RIGHT_BRACKET);
            expression = sb.toString();        }
        return expression;
    }


}
