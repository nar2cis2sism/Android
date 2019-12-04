//package demo.activity.example;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextUtils;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.View.OnTouchListener;
//import android.widget.EditText;
//
//import demo.android.R;
//import engine.android.util.Expression;
//import engine.android.util.Util;
//
//public class CalculatorActivity extends Activity implements OnClickListener {
//    
//    EditText et;
//    
//    Expression exp = new Expression();
//    
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.calculator);
//
//        et = (EditText) findViewById(R.id.et);
//        et.setOnTouchListener(new OnTouchListener() {
//            
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return true;
//            }
//        });
//    }
//
//    @Override
//    public void onClick(View v) {
//        Editable edit = et.getText();
//        int where = et.getSelectionStart();
//        String input = null;
//        
//        switch (v.getId()) {
//        case R.id.left_bracket:
//            input = Expression.LEFT_BRACKET;
//            break;
//        case R.id.right_bracket:
//            input = Expression.RIGHT_BRACKET;
//            break;
//        case R.id.clean:
//            edit.clear();
//            break;
//        case R.id.backspace:
//            if (where > 0)
//            {
//                edit.delete(where - 1, where);
//            }
//            
//            break;
//            
//        case R.id.num7:
//            input = "7";
//            break;
//        case R.id.num8:
//            input = "8";
//            break;
//        case R.id.num9:
//            input = "9";
//            break;
//        case R.id.div:
//            input = Expression.DIV;
//            break;
//            
//        case R.id.num4:
//            input = "4";
//            break;
//        case R.id.num5:
//            input = "5";
//            break;
//        case R.id.num6:
//            input = "6";
//            break;
//        case R.id.mul:
//            input = Expression.MUL;
//            break;
//            
//        case R.id.num1:
//            input = "1";
//            break;
//        case R.id.num2:
//            input = "2";
//            break;
//        case R.id.num3:
//            input = "3";
//            break;
//        case R.id.sub:
//            input = Expression.SUB;
//            break;
//            
//        case R.id.num0:
//            input = "0";
//            break;
//        case R.id.dot:
//            input = ".";
//            break;
//        case R.id.add:
//            input = Expression.ADD;
//            break;
//        case R.id.equ:
//            if (where != et.length())
//            {
//                return;
//            }
//            
//            try {
//                exp.setExpression(edit.toString());
//                double result = exp.calculate();
//                input = "\n" + Expression.EQU + Util.formatNumber(result, "0.#");
//            } catch (Exception e) {
//                input = "\n" + Expression.EQU + e.getMessage();
//            }
//            
//            break;
//
//        case R.id.minus:
//            input = "-";
//            int index = 1;
//            char c = 0;
//            while (where - index >= 0 && Character.isDigit(c = edit.charAt(where - index)))
//            {
//                index++;
//            }
//            
//            if (index != 1 && c != 0)
//            {
//                if (String.valueOf(c).equals(input))
//                {
//                    edit.delete(where - index, where - index + 1);
//                }
//                else
//                {
//                    edit.insert(where - index + 1, input);
//                }
//                
//                return;
//            }
//            
//            break;
//        case R.id.moveLeft:
//            if (where > 0)
//            {
//                et.setSelection(where - 1);
//            }
//            
//            break;
//        case R.id.moveRight:
//            if (where < et.length())
//            {
//                et.setSelection(where + 1);
//            }
//            
//            break;
//
//        default:
//            break;
//        }
//        
//        if (!TextUtils.isEmpty(input))
//        {
//            edit.insert(where, input);
//        }
//    }
//}