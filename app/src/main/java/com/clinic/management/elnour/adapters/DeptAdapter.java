package com.clinic.management.elnour.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.clinic.management.elnour.R;
import com.clinic.management.elnour.models.PatientObject;
import com.clinic.management.elnour.models.SalaryObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DeptAdapter extends ArrayAdapter<PatientObject> {


    public static final String LOG_TAG = SalaryAdapter.class.getSimpleName(); // class name.
    private final Context mContext; // for the activity context that the Adapter work at.
    private ArrayList<Boolean> mItemChecked = new ArrayList<>();


    public DeptAdapter(Context context, ArrayList<PatientObject> patientObjects) {
        super(context, 0, patientObjects);

        mContext = context; // to determine the specific place that the adapter works in.
    }



    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        View listItemView = convertView;

        if (listItemView == null) {

            listItemView =  LayoutInflater.from(mContext).inflate(R.layout.item_dept, parent, false);

        }



        PatientObject patientObject = getItem(position);

        // determine the views from the inflated layout.
        TextView nameTextView = listItemView.findViewById(R.id.item_dept_patient_name);
        TextView sessionCostTextView = listItemView.findViewById(R.id.item_dept_session_cost);
        TextView deptTextView = listItemView.findViewById(R.id.item_dept_dept);


        nameTextView.setText(patientObject.getName());

        double sessionsCost = patientObject.getSessionCost();
        sessionCostTextView.setText("Sessions Cost : " + sessionsCost);


        double dept = patientObject.getDept();
        String deptFormat =  String.format("%.2f", dept);
        deptTextView.setText("Dept : " + deptFormat);




        return listItemView;


    }



}

