package com.mobileapp.pemdascalculator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Stack;

public class NextFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_next, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView solutionText = view.findViewById(R.id.solutionText);
        Bundle args = getArguments();
        if (args != null) {
            String equation = args.getString("equation");
            String steps = evaluateWithSteps(equation);
            solutionText.setText("Your equation: " + equation + "\n" + steps);
        }
    }

    private String evaluateWithSteps(String expression) {
        StringBuilder steps = new StringBuilder();
        try {
            steps.append("Step-by-step solution:\n");
            double result = evaluate(expression.replace("X", "*"), steps);
            steps.append("Final Result: ").append(result % 1 == 0 ? (int) result : result);
        } catch (Exception e) {
            steps.append("Error: Invalid equation");
        }
        return steps.toString();
    }

    private double evaluate(String expression, StringBuilder steps) {
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();
        StringBuilder currentNumber = new StringBuilder();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                currentNumber.append(c);
            } else if (c == '(') {
                operators.push(c);
            } else if (c == ')') {
                if (currentNumber.length() > 0) {
                    values.push(Double.parseDouble(currentNumber.toString()));
                    currentNumber.setLength(0);
                }
                while (!operators.isEmpty() && operators.peek() != '(') {
                    calculateTopOperation(values, operators, steps);
                }
                operators.pop();
            } else if (isOperator(c)) {
                if (currentNumber.length() > 0) {
                    values.push(Double.parseDouble(currentNumber.toString()));
                    currentNumber.setLength(0);
                }
                while (!operators.isEmpty() && precedence(c) <= precedence(operators.peek())) {
                    calculateTopOperation(values, operators, steps);
                }
                operators.push(c);
            }
        }

        if (currentNumber.length() > 0) {
            values.push(Double.parseDouble(currentNumber.toString()));
        }

        while (!operators.isEmpty()) {
            calculateTopOperation(values, operators, steps);
        }

        return values.pop();
    }

    private void calculateTopOperation(Stack<Double> values, Stack<Character> operators, StringBuilder steps) {
        double b = values.pop();
        double a = values.pop();
        char operator = operators.pop();
        double result = applyOperation(a, b, operator);
        values.push(result);
        steps.append("Step ").append(steps.length() + 1).append(": ")
                .append((a % 1 == 0 ? (int) a : a))
                .append(" ").append(operator).append(" ")
                .append((b % 1 == 0 ? (int) b : b))
                .append(" = ").append(result % 1 == 0 ? (int) result : result).append("\n");
    }

    private double applyOperation(double a, double b, char operator) {
        return switch (operator) {
            case '+' -> a + b;
            case '-' -> a - b;
            case 'x' -> a * b;
            case '/' -> a / b;
            case '^' -> Math.pow(a, b);
            default -> throw new IllegalArgumentException("Invalid operator");
        };
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == 'x' || c == '/' || c == '^';
    }

    private int precedence(char operator) {
        return switch (operator) {
            case '+', '-' -> 1;
            case 'x', '/' -> 2;
            case '^' -> 3;
            default -> -1;
        };
    }
}