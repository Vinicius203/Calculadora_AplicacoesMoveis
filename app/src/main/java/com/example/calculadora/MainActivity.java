package com.example.calculadora;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvResult, tvHistory;
    private final StringBuilder currentInput = new StringBuilder();
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvResult = findViewById(R.id.tv_result);
        tvHistory = findViewById(R.id.tv_history);
        scrollView = findViewById(R.id.sv_history);
        Button btnClearHistory = findViewById(R.id.btn_clear_history);

        // Permite scroll no histórico
        tvHistory.setMovementMethod(new ScrollingMovementMethod());

        setupButtons();

        btnClearHistory.setOnClickListener(v -> clearHistory());
    }

    private void setupButtons() {
        findViewById(R.id.btn_7).setOnClickListener(v -> updateInput("7"));
        findViewById(R.id.btn_8).setOnClickListener(v -> updateInput("8"));
        findViewById(R.id.btn_9).setOnClickListener(v -> updateInput("9"));
        findViewById(R.id.btn_4).setOnClickListener(v -> updateInput("4"));
        findViewById(R.id.btn_5).setOnClickListener(v -> updateInput("5"));
        findViewById(R.id.btn_6).setOnClickListener(v -> updateInput("6"));
        findViewById(R.id.btn_1).setOnClickListener(v -> updateInput("1"));
        findViewById(R.id.btn_2).setOnClickListener(v -> updateInput("2"));
        findViewById(R.id.btn_3).setOnClickListener(v -> updateInput("3"));
        findViewById(R.id.btn_0).setOnClickListener(v -> updateInput("0"));
        findViewById(R.id.btn_add).setOnClickListener(v -> updateInput("+"));
        findViewById(R.id.btn_subtract).setOnClickListener(v -> updateInput("-"));
        findViewById(R.id.btn_multiply).setOnClickListener(v -> updateInput("*"));
        findViewById(R.id.btn_divide).setOnClickListener(v -> updateInput("/"));
        findViewById(R.id.btn_clear).setOnClickListener(v -> clearInput());
        findViewById(R.id.btn_equals).setOnClickListener(v -> evaluateExpression());
    }

    private void updateInput(String input) {
        currentInput.append(input);
        tvResult.setText(currentInput.toString());
    }

    private void clearInput() {
        currentInput.setLength(0);
        tvResult.setText("0");
    }

    private void evaluateExpression() {
        String input = currentInput.toString();

        if (!isValidExpression(input)) {
            tvResult.setText(getString(R.string.error_invalid_input));
            return;
        }
        // Não permite divisão por zero.
        if (isDivisionByZero(input)) {
            tvResult.setText(getString(R.string.error_division_by_zero));
            return;
        }

        try {
            String result = calculateResult(input);
            updateHistory(input + " = " + result);
            tvResult.setText(result);

            // Armazena o resultado atual para a próxima operação
            currentInput.setLength(0);
            currentInput.append(result);

        } catch (Exception e) {
            tvResult.setText(getString(R.string.error_unexpected));
        }
    }

    // RegEx para validar a operação
    private boolean isValidExpression(String input) {
        return input.matches("^[0-9+\\-*/.]+$") && !input.matches(".*[+\\-*/]{2,}.*");
    }

    private boolean isDivisionByZero(String input) {
        return input.contains("/0");
    }

    private String calculateResult(String input) {
        try {
            Expression expression = new ExpressionBuilder(input).build();
            double result = expression.evaluate();
            return String.valueOf(result);
        } catch (ArithmeticException e) {
            return "Erro aritmético";
        } catch (Exception e) {
            return "Erro";
        }
    }

    private void updateHistory(String operation) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            db.historyDao().insert(new HistoryEntry(operation));

            // Busca o histórico atualizado
            List<HistoryEntry> historyList = db.historyDao().getAllHistory();

            runOnUiThread(() -> {
                StringBuilder formattedHistory = new StringBuilder();
                for (HistoryEntry entry : historyList) {
                    formattedHistory.append(entry.getOperation()).append("\n");
                }
                tvHistory.setText(formattedHistory.toString());

                // Rolagem automática
                scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
            });
        }).start();
    }

    private void clearHistory() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            db.historyDao().clearHistory();

            // Atualizar a UI após limpar
            runOnUiThread(() -> {
                tvHistory.setText("");
            });
        }).start();
    }
}
