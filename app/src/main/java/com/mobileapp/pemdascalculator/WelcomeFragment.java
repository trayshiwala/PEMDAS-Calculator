package com.mobileapp.pemdascalculator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class WelcomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        EditText equationInput = view.findViewById(R.id.equationInput);

        int[] buttonIds = {
                R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9,
                R.id.btn_plus, R.id.btn_minus, R.id.btn_multiply, R.id.btn_divide,
                R.id.btn_open_parenthesis, R.id.btn_close_parenthesis, R.id.btn_exponent
        };

        for (int id : buttonIds) {
            view.findViewById(id).setOnClickListener(v -> {
                Button button = (Button) v;
                equationInput.append(button.getText());
            });
        }

        view.findViewById(R.id.btn_clear).setOnClickListener(v -> equationInput.setText(""));
        view.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            String currentText = equationInput.getText().toString();
            if (!currentText.isEmpty()) {
                equationInput.setText(currentText.substring(0, currentText.length() - 1));
            }
        });

        view.findViewById(R.id.btn_equals).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("equation", equationInput.getText().toString());
            Navigation.findNavController(view).navigate(R.id.action_welcomeFragment_to_nextFragment, bundle);
        });
    }
}