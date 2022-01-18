package calculator;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static java.lang.Character.isDigit;
import static java.lang.Character.isWhitespace;

public class Calculator extends JFrame {

    public static final char ADDITION = '+';    //'\u002B' Did not pass test. Why??
    public static final char SUBTRACTION = '-';     //'\u2212' Did not pass tests.  Why???
    public static final char MULTIPLICATION = '\u00D7';
    public static final char DIVISION = '\u00F7';
    public static final char LEFT_BRACKET = '(';
    public static final char RIGHT_BRACKET = ')';
    public static final char SQRT = '\u221A';
    public static final char SQR = '\u00B2';
    public static final char PWR = '\u005E';
    public static final char PLUS_MINUS = '\u00B1';
    public static final List<Character> BIN_OPERATORS = new ArrayList<>(
            Arrays.asList(ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION, PWR));
    public static final List<Character> UNA_OPERATORS = new ArrayList<>(
            Arrays.asList(SQRT));
    public final String X_POW_Y = "X" + Character.toString('\u02B8');
    public final String X_POW_2 = "X" + Character.toString(SQR);
    private static final Color BACKGROUND = new Color(152, 149, 149, 255);
    private static final Color OP_BTN_BG = new Color(180, 177, 177, 255);
    private ArithmeticExpression ae = new ArithmeticExpression();
    private String oldAe = "";

    public Calculator()  {
        Logger logger = Logger.getLogger(Calculator.class.getName());
        try {
            Handler handler = new FileHandler("calclog");
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, "Could not create log file");
        };

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 400);
        setTitle("Calculator");
        setLayout(new BorderLayout());

        JLabel lblEquation = new JLabel("", SwingConstants.RIGHT);
        lblEquation.setName("EquationLabel");
        lblEquation.setPreferredSize(new Dimension(200, 20));
        lblEquation.setVisible(true);
        lblEquation.setFont(new Font("Ariel", Font.PLAIN, 12));
        lblEquation.setForeground(Color.green.darker());
        lblEquation.setHorizontalTextPosition(SwingConstants.RIGHT);

        JLabel lblResult = new JLabel("0", SwingConstants.RIGHT);
        lblResult.setName("ResultLabel");
        lblResult.setPreferredSize(new Dimension(200, 20));
        lblResult.setFont(new Font("Ariel", Font.BOLD, 24));
        lblResult.setVisible(true);
        lblResult.setHorizontalTextPosition(SwingConstants.RIGHT);


        //North panel
        JPanel pnlNorth = new JPanel(new GridLayout(2, 1, 1, 30));
        Border northPanelBorder = BorderFactory.createEmptyBorder(20, 20, 20, 20);
        pnlNorth.setBorder(northPanelBorder);
        pnlNorth.add(lblResult);
        pnlNorth.add(lblEquation);

        add(pnlNorth, BorderLayout.NORTH);

        //Keypad panel, centre
        GridLayout gl = new GridLayout(6, 4, 5, 5);
        JPanel pnlKeyPad = new JPanel(gl);
        Border keyPadBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        pnlKeyPad.setBorder(keyPadBorder);
        pnlKeyPad.setPreferredSize(new Dimension(230, 200));
        pnlKeyPad.setVisible(true);

        //Action listener for buttons that appends to equation label
        ActionListener appendEquation = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton button = (JButton) e.getSource();
                oldAe = ae.getExpression();
                lblEquation.setText(ae.append(button.getText().charAt(0))); ;
                lblEquation.setForeground(Color.green.darker());
                logger.info("Button " + button.getName() + " Equation label " + lblEquation.getText());
            }
        };

        ActionListener appendSQRT = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                oldAe = ae.getExpression();
                lblEquation.setText(ae.appendSQRT());
            }
        };

        ActionListener evaluateExpression = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (ae.isEmpty()) {
                    beep(1,1);
                    JOptionPane.showMessageDialog(Calculator.this, "Please enter an equation first!");
                } else if (isOperator(ae.getLastChar())) {
                    beep(1, 1);
                    //JOptionPane.showMessageDialog(Calculator.this, "Operator at end of equation is not allowed!" );
                    lblEquation.setForeground(Color.RED.darker());
                } else if (!ae.countLeftRightBracketsEqual()) {
                    beep(1, 1);
                    //JOptionPane.showMessageDialog(Calculator.this, "Number of left and right brackets must be equal!" );
                    lblEquation.setForeground(Color.RED.darker());
                } else {
                    try {
                        lblResult.setText(dbl2str(ae.evaluate()));
                        logger.info("Evaluate, result = " + lblResult.getText());
                    } catch (NumberFormatException nfe) {
                        beep(1, 1);
                        JOptionPane.showMessageDialog(Calculator.this, "Incorrect number");
                    } catch (UnsupportedOperationException uoe) {
                        beep(1, 1);
                        lblEquation.setForeground(Color.RED.darker());
                        //JOptionPane.showMessageDialog(Calculator.this, uoe.getMessage());
                    } catch (Exception ex) {
                        beep(1, 1);
                        JOptionPane.showMessageDialog(Calculator.this, ex.getMessage());
                        ex.printStackTrace();
                    }
                }
                }

        };

        ActionListener clear = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lblResult.setText("0");
                lblEquation.setText(ae.clear());
                lblEquation.setForeground(Color.GREEN.darker());
                logger.info("Button clear.  Label equation " + lblEquation.getText());
            }
        };

        ActionListener delete = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lblEquation.setForeground(Color.GREEN.darker());
                String s = lblEquation.getText();
                lblEquation.setText(ae.delete());
                logger.info("Button delete " + " Label Equation " + lblEquation.getText() );
            }
        };

        ActionListener appendBracket = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                oldAe = ae.getExpression();
                lblEquation.setText(ae.appendBracket());
                logger.info("Button Brackets Label Equation: " + lblEquation.getText() );
            }
        };

        ActionListener square = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                oldAe = ae.getExpression();
                ae.append(PWR);
                ae.append(LEFT_BRACKET);
                ae.append('2');
                lblEquation.setText(ae.append(RIGHT_BRACKET));
                logger.info("Button Square Label Equation: " + lblEquation.getText() );
            }
        };

        ActionListener power = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                oldAe = ae.getExpression();
                ae.append(PWR);
                lblEquation.setText(ae.append(LEFT_BRACKET));
                logger.info("Button Power Label Equation: " + lblEquation.getText() );
            }
        };

        ActionListener negate = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                oldAe = ae.getExpression();
                lblEquation.setText(ae.negate());
                logger.info("Button Negate Label Equation: " + lblEquation.getText() );
            }
        };

        ActionListener clearEntry = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String previous = ae.getExpression();
                lblEquation.setText(ae.setExpression(oldAe));
                oldAe = previous;
                logger.info("Button CE Label Equation: " + lblEquation.getText() );
            }
        };


        //Digit buttons for keypad
        JButton btn0 = new JButton("0");
        btn0.setName("Zero");
        btn0.setVisible(true);
        btn0.addActionListener(appendEquation);

        JButton btn1 = new JButton("1");
        btn1.setName("One");
        btn1.setVisible(true);
        btn1.addActionListener(appendEquation);

        JButton btn2 = new JButton("2");
        btn2.setName("Two");
        btn2.setVisible(true);
        btn2.addActionListener(appendEquation);

        JButton btn3 = new JButton("3");
        btn3.setName("Three");
        btn3.setVisible(true);
        btn3.addActionListener(appendEquation);

        JButton btn4 = new JButton("4");
        btn4.setName("Four");
        btn4.setVisible(true);
        btn4.addActionListener(appendEquation);

        JButton btn5 = new JButton("5");
        btn5.setName("Five");
        btn5.setVisible(true);
        btn5.addActionListener(appendEquation);

        JButton btn6 = new JButton("6");
        btn6.setName("Six");
        btn6.setVisible(true);
        btn6.addActionListener(appendEquation);

        JButton btn7 = new JButton("7");
        btn7.setName("Seven");
        btn7.setVisible(true);
        btn7.addActionListener(appendEquation);

        JButton btn8 = new JButton("8");
        btn8.setName("Eight");
        btn8.setVisible(true);
        btn8.addActionListener(appendEquation);

        JButton btn9 = new JButton("9");
        btn9.setName("Nine");
        btn9.setVisible(true);
        btn9.addActionListener(appendEquation);

        //Operation buttons for keypad
        JButton btnAdd = new JButton(Character.toString(ADDITION) );
        btnAdd.setName("Add");
        btnAdd.setVisible(true);
        btnAdd.setBackground(OP_BTN_BG);
        btnAdd.addActionListener(appendEquation);

        JButton btnSubtract = new JButton(Character.toString(SUBTRACTION));
        btnSubtract.setName("Subtract");
        btnSubtract.setVisible(true);
        btnSubtract.setBackground(OP_BTN_BG);
        btnSubtract.addActionListener(appendEquation);

        JButton btnMultiply = new JButton(Character.toString(MULTIPLICATION));
        btnMultiply.setName("Multiply");
        btnMultiply.setVisible(true);
        btnMultiply.setBackground(OP_BTN_BG);
        btnMultiply.addActionListener(appendEquation);

        JButton btnDivide = new JButton(Character.toString(DIVISION));
        btnDivide.setName("Divide");
        btnDivide.setVisible(true);
        btnDivide.setBackground(OP_BTN_BG);
        btnDivide.addActionListener(appendEquation);

        JButton btnEquals = new JButton("=");
        btnEquals.setName("Equals");
        btnEquals.setVisible(true);
        btnEquals.setBackground(new Color(151, 167, 201, 255));
        btnEquals.addActionListener(evaluateExpression);

        JButton btnDot = new JButton(".");
        btnDot.setVisible(true);
        btnDot.setName("Dot");
        btnDot.addActionListener(appendEquation);

        JButton btnClear = new JButton("C");
        btnClear.setName("Clear");
        btnClear.addActionListener(clear);
        btnClear.setVisible(true);
        btnClear.setBackground(OP_BTN_BG);

        JButton btnDelete = new JButton("Del");
        btnDelete.setName("Delete");
        btnDelete.addActionListener(delete);
        btnDelete.setVisible(true);
        btnDelete.setBackground(OP_BTN_BG);

        JButton btnBrackets = new JButton(Character.toString(LEFT_BRACKET) +
                Character.toString(RIGHT_BRACKET));
        btnBrackets.setName("Parentheses");
        btnBrackets.addActionListener(appendBracket);
        btnBrackets.setVisible(true);
        btnBrackets.setEnabled(true);
        btnBrackets.setBackground(OP_BTN_BG);

        JButton btnCE = new JButton("CE");
        btnCE.addActionListener(clearEntry);
        btnCE.setVisible(true);
        btnCE.setEnabled(true);
        btnCE.setBackground(OP_BTN_BG);

        JButton btnSqrt = new JButton(Character.toString(SQRT));
        btnSqrt.setName("SquareRoot");
        btnSqrt.addActionListener(appendSQRT);
        btnSqrt.setVisible(true);
        btnSqrt.setEnabled(true);
        btnSqrt.setBackground(OP_BTN_BG);

        JButton btnPowY = new JButton(X_POW_Y);
        btnPowY.setName("PowerY");
        btnPowY.addActionListener(power);
        btnPowY.setVisible(true);
        btnPowY.setEnabled(true);
        btnPowY.setBackground(OP_BTN_BG);

        JButton btnPow2 = new JButton(X_POW_2);
        btnPow2.setName("PowerTwo");
        btnPow2.addActionListener(square);
        btnPow2.setVisible(true);
        btnPow2.setEnabled(true);
        btnPow2.setBackground(OP_BTN_BG);

        JButton btnPlusMinus = new JButton(Character.toString(PLUS_MINUS));
        btnPlusMinus.setName("PlusMinus");
        btnPlusMinus.addActionListener(negate);
        btnPlusMinus.setVisible(true);
        btnPlusMinus.setEnabled(true);




        pnlKeyPad.add(btnBrackets);
        pnlKeyPad.add(btnCE);
        pnlKeyPad.add(btnClear);
        pnlKeyPad.add(btnDelete);
        pnlKeyPad.add(btnPow2);
        pnlKeyPad.add(btnPowY);
        pnlKeyPad.add(btnSqrt);
        pnlKeyPad.add(btnDivide);
        pnlKeyPad.add(btn7);
        pnlKeyPad.add(btn8);
        pnlKeyPad.add(btn9);
        pnlKeyPad.add(btnMultiply);
        pnlKeyPad.add(btn4);
        pnlKeyPad.add(btn5);
        pnlKeyPad.add(btn6);
        pnlKeyPad.add(btnSubtract);
        pnlKeyPad.add(btn1);
        pnlKeyPad.add(btn2);
        pnlKeyPad.add(btn3);
        pnlKeyPad.add(btnAdd);
        pnlKeyPad.add(btnPlusMinus);
        pnlKeyPad.add(btn0);
        pnlKeyPad.add(btnDot);
        pnlKeyPad.add(btnEquals);

        add(pnlKeyPad, BorderLayout.CENTER);

        setLocationRelativeTo(null);  //The window will open in the centre of the screen
        setBackground(BACKGROUND);
        setVisible(true);


    }

    private static String dbl2str(double number) {
        boolean negative = number < 0;
        double pos;
        String s;
        if (negative) {
            pos = (-1) * number;
        } else {
            pos = number;
        }
        if (pos % 1 == 0 && pos <= Integer.MAX_VALUE) {
            int n = (int) pos;
            s = Integer.toString(n);
        } else {
            s = Double.toString(pos);
        }
        if (negative) {
            return "-" + s;
        } else {
            return s;
        }
    }

    public static void beep(int times, long interval) {
        try {
        for (int i = 0; i < times; i++) {
            Toolkit.getDefaultToolkit().beep();
            Thread.sleep(interval);
        }
        } catch (InterruptedException ie) {
            JOptionPane.showMessageDialog(null, ie.getMessage());
        }
    }

    public static boolean isOperator(char c) {
        return Calculator.BIN_OPERATORS.contains((Character) c) || Calculator.UNA_OPERATORS.contains((Character) c);
    }

    public static boolean isBinOperator(char c) {
        return Calculator.BIN_OPERATORS.contains((Character) c);
    }

    public static boolean isUnaOperator(char c) {
        return Calculator.UNA_OPERATORS.contains((Character) c);
    }

    public static int operatorPriority(char operator) {
        if (isUnaOperator(operator)) {
            return 0; //Highest priority
        } else if (operator == Calculator.PWR) {
            return 1;
        } else if (operator == Calculator.MULTIPLICATION || operator == Calculator.DIVISION) {
            return 2;
        } else if (operator == Calculator.ADDITION || operator == Calculator.SUBTRACTION) {
            return 3;
        } else {
            return 4;
        }
    }


}
