package hsk.practice.myvoca.ui.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import hsk.practice.myvoca.R;

/**
 * Custom view which compares two values and show proper graphics.
 * Hard to explain in text, just see the QuizFragment!
 */
public class VersusView extends LinearLayout {

    private int BAR_HEIGHT = 50;
    private int leftValue = 0;
    private int rightValue = 0;

    TextView leftTextView;
    TextView rightTextView;

    View leftBar;
    View rightBar;

    public VersusView(Context context) {
        super(context);
        init(context);
    }

    public VersusView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.versus_view, this, true);

        leftTextView = findViewById(R.id.count_left);
        rightTextView = findViewById(R.id.count_right);

        leftBar = findViewById(R.id.left_bar);
        rightBar = findViewById(R.id.right_bar);
    }

    public void setValues(int left, int right) {
        setLeftValue(left);
        setRightValue(right);
    }

    public void setLeftValue(int value) {
        if (value < 0) {
            return;
        }
        leftValue = value;
        leftTextView.setText(Integer.toString(leftValue));
        refreshView();
    }

    public void setRightValue(int value) {
        if (value < 0) {
            return;
        }
        rightValue = value;
        rightTextView.setText(Integer.toString(rightValue));
        refreshView();
    }

    public void refreshView() {
        // Weights should be set oppositely to get correct result
        leftBar.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, BAR_HEIGHT, rightValue));
        rightBar.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, BAR_HEIGHT, leftValue));
    }
}
