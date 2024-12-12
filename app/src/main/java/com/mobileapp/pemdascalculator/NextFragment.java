package com.mobileapp.pemdascalculator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
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
            solutionText.setText("Your equation: \n\n" + equation + "\n\n\n" + steps);
        }

        Button solveAnother = view.findViewById(R.id.btn_solve_another);
        solveAnother.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nextFragment_to_welcomeFragment);
        });
    }

    private String evaluateWithSteps(String expression) {
        StringBuilder steps = new StringBuilder();
        try {
            steps.append("Step-by-step solution:\n");
            double result = evaluate(expression.replace("X", "*").replaceAll("(\\d)(\\()", "$1*("), steps);
            steps.append("\nFinal Result: ").append(formatNumber(result));
        } catch (Exception e) {
            steps.append("Error: Invalid equation");
        }
        return steps.toString();
    }

    private double evaluate(String expression, StringBuilder steps) {
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();
        StringBuilder currentNumber = new StringBuilder();
        int stepNumber = 1;

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                currentNumber.append(c);
            } else if (c == '(') {
                if (currentNumber.length() > 0) {
                    values.push(Double.parseDouble(currentNumber.toString()));
                    currentNumber.setLength(0);
                    operators.push('*');
                }
                operators.push(c);
            } else if (c == ')') {
                if (currentNumber.length() > 0) {
                    values.push(Double.parseDouble(currentNumber.toString()));
                    currentNumber.setLength(0);
                }
                while (!operators.isEmpty() && operators.peek() != '(') {
                    stepNumber = calculateTopOperation(values, operators, steps, stepNumber);
                }
                operators.pop();
            } else if (isOperator(c)) {
                if (currentNumber.length() > 0) {
                    values.push(Double.parseDouble(currentNumber.toString()));
                    currentNumber.setLength(0);
                }
                while (!operators.isEmpty() && precedence(c) <= precedence(operators.peek())) {
                    stepNumber = calculateTopOperation(values, operators, steps, stepNumber);
                }
                operators.push(c);
            }
        }

        if (currentNumber.length() > 0) {
            values.push(Double.parseDouble(currentNumber.toString()));
        }

        while (!operators.isEmpty()) {
            stepNumber = calculateTopOperation(values, operators, steps, stepNumber);
        }

        return values.pop();
    }

    private int calculateTopOperation(Stack<Double> values, Stack<Character> operators, StringBuilder steps, int stepNumber) {
        double b = values.pop();
        double a = values.pop();
        char operator = operators.pop();
        double result = applyOperation(a, b, operator);
        values.push(result);
        steps.append("\nStep ").append(stepNumber).append(": ")
                .append(formatNumber(a))
                .append(" ").append(operator).append(" ")
                .append(formatNumber(b))
                .append(" = ").append(formatNumber(result)).append("\n");
        return stepNumber + 1;
    }

    private double applyOperation(double a, double b, char operator) {
        return switch (operator) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> a / b;
            case '^' -> Math.pow(a, b);
            default -> throw new IllegalArgumentException("Invalid operator");
        };
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }

    private int precedence(char operator) {
        return switch (operator) {
            case '+', '-' -> 1;
            case '*', '/' -> 2;
            case '^' -> 3;
            default -> -1;
        };
    }

    private String formatNumber(double number) {
        return (number % 1 == 0) ? String.valueOf((int) number) : String.valueOf(number);
    }
}