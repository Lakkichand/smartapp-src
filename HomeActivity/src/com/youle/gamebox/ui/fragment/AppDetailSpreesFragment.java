package com.youle.gamebox.ui.fragment;


import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.util.CopyPasteUtils;

public class AppDetailSpreesFragment extends BaseFragment {
    @InjectView(R.id.copyBut)
    Button button;
    @InjectView(R.id.edit)
    EditText editText;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    protected int getViewId() {
        return R.layout.appdetail_packs_layout;
    }

    protected void loadData() {
        Toast.makeText(getActivity()," spress  loadData",Toast.LENGTH_LONG).show();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String string = button.getText().toString();
                CopyPasteUtils.setText(getActivity(),string);
            }
        });
                editText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                editText.setText(CopyPasteUtils.getText(getActivity()));
                return false;
            }
        });
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
