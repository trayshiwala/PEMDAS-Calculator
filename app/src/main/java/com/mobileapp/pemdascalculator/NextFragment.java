package com.mobileapp.pemdascalculator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Stack;

public class NextFragment extends Fragment {
    private static final ArrayList<String> equationHistory = new ArrayList<>();
    private ArrayAdapter<String> historyAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_next, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView solutionText = view.findViewById(R.id.solutionText);
        Spinner historySpinner = view.findViewById(R.id.historySpinner);
        Button solveAnother = view.findViewById(R.id.btn_solve_another);
        Button clearHistory = view.findViewById(R.id.btn_clear_history);

        ArrayList<String> spinnerItems = new ArrayList<>();
        spinnerItems.add("Your math history");
        spinnerItems.addAll(equationHistory);

        historyAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, spinnerItems);
        historySpinner.setAdapter(historyAdapter);

        Bundle args = getArguments();
        if (args != null) {
            String equation = args.getString("equation");
            if (equation != null && !equation.isEmpty()) {
                String processedEquation = equation.replace("÷", "/").replace("x", "*").replace("xⁿ", "^");
                String steps = evaluateWithSteps(processedEquation);
                solutionText.setText("Your equation: \n\n" + equation + "\n\n\n" + steps);

                if (!equationHistory.contains(equation)) {
                    equationHistory.add(equation);
                    spinnerItems.add(equation);
                    historyAdapter.notifyDataSetChanged();
                }
                historySpinner.setSelection(0);
            } else {
                solutionText.setText("Error: No equation provided.");
            }
        }

        historySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String selectedEquation = equationHistory.get(position - 1);
                    String steps = evaluateWithSteps(selectedEquation.replace("÷", "/").replace("x", "*").replace("xⁿ", "^"));
                    solutionText.setText("Your equation: \n\n" + selectedEquation + "\n\n\n" + steps);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        solveAnother.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_nextFragment_to_welcomeFragment));

        clearHistory.setOnClickListener(v -> {
            equationHistory.clear();
            spinnerItems.clear();
            spinnerItems.add("Your math history");
            historyAdapter.notifyDataSetChanged();
        });
    }

    private String evaluateWithSteps(String expression) {
        StringBuilder steps = new StringBuilder();
        try {
            steps.append("Step-by-step solution:\n");
            BigDecimal result = evaluate(expression.replaceAll("(\\d)(\\()", "$1*("), steps);
            steps.append("\nFinal Result: ").append(result.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
        } catch (Exception e) {
            steps.append("Error: Invalid equation");
        }
        return steps.toString();
    }

    private BigDecimal evaluate(String expression, StringBuilder steps) {
        Stack<BigDecimal> values = new Stack<>();
        Stack<Character> operators = new Stack<>();
        StringBuilder currentNumber = new StringBuilder();
        int stepNumber = 1;

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                currentNumber.append(c);
            } else if (c == '(') {
                if (currentNumber.length() > 0) {
                    values.push(new BigDecimal(currentNumber.toString()));
                    currentNumber.setLength(0);
                    operators.push('*');
                }
                operators.push(c);
            } else if (c == ')') {
                if (currentNumber.length() > 0) {
                    values.push(new BigDecimal(currentNumber.toString()));
                    currentNumber.setLength(0);
                }
                while (!operators.isEmpty() && operators.peek() != '(') {
                    stepNumber = calculateTopOperation(values, operators, steps, stepNumber);
                }
                operators.pop();
            } else if (isOperator(c)) {
                if (currentNumber.length() > 0) {
                    values.push(new BigDecimal(currentNumber.toString()));
                    currentNumber.setLength(0);
                }
                while (!operators.isEmpty() && precedence(c) <= precedence(operators.peek())) {
                    stepNumber = calculateTopOperation(values, operators, steps, stepNumber);
                }
                operators.push(c);
            }
        }

        if (currentNumber.length() > 0) {
            values.push(new BigDecimal(currentNumber.toString()));
        }

        while (!operators.isEmpty()) {
            stepNumber = calculateTopOperation(values, operators, steps, stepNumber);
        }

        return values.pop();
    }

    private int calculateTopOperation(Stack<BigDecimal> values, Stack<Character> operators, StringBuilder steps, int stepNumber) {
        BigDecimal b = values.pop();
        BigDecimal a = values.pop();
        char operator = operators.pop();
        BigDecimal result = applyOperation(a, b, operator);
        values.push(result);
        steps.append("\nStep ").append(stepNumber).append(": ")
                .append(a.stripTrailingZeros().toPlainString())
                .append(" ").append(operator).append(" ")
                .append(b.stripTrailingZeros().toPlainString())
                .append(" = ").append(result.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()).append("\n");
        return stepNumber + 1;
    }

    private BigDecimal applyOperation(BigDecimal a, BigDecimal b, char operator) {
        return switch (operator) {
            case '+' -> a.add(b);
            case '-' -> a.subtract(b);
            case '*' -> a.multiply(b);
            case '/' -> a.divide(b, 10, RoundingMode.HALF_UP);
            case '^' -> a.pow(b.intValue(), new MathContext(10, RoundingMode.HALF_UP));
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
}