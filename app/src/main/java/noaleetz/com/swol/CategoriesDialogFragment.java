package noaleetz.com.swol;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesDialogFragment extends android.support.v4.app.DialogFragment implements AdapterView.OnItemClickListener {

    @BindView(R.id.list)
    ListView list;
    String[] categories = new String[]{"Bike", "Cardio","Class","Dance","Game","Gym","High Intensity Interval Training","Hike","Meditation","Run","Swim","Weight"};

    List<String> listOfString = Arrays.asList(categories);
    ArrayList<String> categoriesArrayList = new ArrayList(listOfString);

    String [] catArray = categoriesArrayList.toArray(categories);



    Unbinder unbinder;
    CategoryDialogListener listener = (CategoryDialogListener) getTargetFragment();




    public CategoriesDialogFragment() {
        // Required empty public constructor
    }

    public interface CategoryDialogListener {
        void onFinishCategoryDialog(String inputText);
    }
    public void setCallBack(CategoriesDialogFragment.CategoryDialogListener categoryDialogListener) {
        listener = categoryDialogListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_categories_dialog, null, false);
        unbinder = ButterKnife.bind(this, view);

//        getDialog().getWindow().setLayout(300,500);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(),android.R.layout.simple_list_item_multiple_choice,categories);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);


    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Toast.makeText(getActivity(), catArray[i], Toast.LENGTH_SHORT)
                .show();


//        Intent intent = new Intent()
//                .putExtra("category", catArray[i]);
//        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);

//        listener.onFinishCategoryDialog(catArray[i]);

//        dismiss();
    }
}
