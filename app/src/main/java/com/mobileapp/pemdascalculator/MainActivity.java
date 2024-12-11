package com.mobileapp.pemdascalculator;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.NavController;
import androidx.navigation.ui.NavigationUI;
import android.view.View;
import android.widget.Button;
import java.util.ArrayList;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    private TextView tvDisplay, tvHistory;
    private StringBuilder inputExpression;
    private ArrayList<String> calculationHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDisplay = findViewById(R.id.tvDisplay);
        tvHistory = findViewById(R.id.tvHistory);
        inputExpression = new StringBuilder();
        calculationHistory = new ArrayList<>();

        findViewById(R.id.btn1).setOnClickListener(this::appendInput);
        findViewById(R.id.btn2).setOnClickListener(this::appendInput);
        findViewById(R.id.btn3).setOnClickListener(this::appendInput);
        findViewById(R.id.btn4).setOnClickListener(this::appendInput);
        findViewById(R.id.btn5).setOnClickListener(this::appendInput);
        findViewById(R.id.btn6).setOnClickListener(this::appendInput);
        findViewById(R.id.btn7).setOnClickListener(this::appendInput);
        findViewById(R.id.btn8).setOnClickListener(this::appendInput);
        findViewById(R.id.btn9).setOnClickListener(this::appendInput);
        findViewById(R.id.btn0).setOnClickListener(this::appendInput);
        findViewById(R.id.btnPlus).setOnClickListener(this::appendInput);
        findViewById(R.id.btnMinus).setOnClickListener(this::appendInput);
        findViewById(R.id.btnMultiply).setOnClickListener(this::appendInput);
        findViewById(R.id.btnDivide).setOnClickListener(this::appendInput);
        findViewById(R.id.btnEquals).setOnClickListener(view -> calculateResult());
        findViewById(R.id.btnClear).setOnClickListener(view -> clearInput());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    private void appendInput(View view) {
        Button button = (Button) view;
        inputExpression.append(button.getText().toString());
        tvDisplay.setText(inputExpression.toString());
    }

    private void calculateResult() {
        try {
            String equation = inputExpression.toString();
            double result = evaluateExpression(equation);
            calculationHistory.add(equation + " = " + result);
            updateHistory();
            tvDisplay.setText(String.valueOf(result));
            inputExpression = new StringBuilder(String.valueOf(result));
        } catch (Exception e) {
            tvDisplay.setText("Error");
        }
    }

    private void updateHistory() {
        StringBuilder historyBuilder = new StringBuilder();
        for (String record : calculationHistory) {
            historyBuilder.append(record).append("\n");
        }
        tvHistory.setText(historyBuilder.toString());
    }

    private void clearInput() {
        inputExpression.setLength(0);
        tvDisplay.setText("");
        calculationHistory.clear();
        tvHistory.setText("");
    }

    private double evaluateExpression(String expression) {
        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                StringBuilder number = new StringBuilder();
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.'))
                    number.append(expression.charAt(i++));
                i--;
                numbers.push(Double.parseDouble(number.toString()));
            } else if (c == '(') {
                operators.push(c);
            } else if (c == ')') {
                while (operators.peek() != '(')
                    numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
                operators.pop();
            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                while (!operators.isEmpty() && hasPrecedence(c, operators.peek()))
                    numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
                operators.push(c);
            }
        }
        while (!operators.isEmpty())
            numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
        return numbers.pop();
    }

    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') return false;
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) return false;
        return true;
    }

    private double applyOperation(char op, double b, double a) {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/':
                if (b == 0) throw new UnsupportedOperationException("Cannot divide by zero");
                return a / b;
        }
        return 0;
    }
}