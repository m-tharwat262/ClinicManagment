package com.clinic.management.elnour.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.clinic.management.elnour.R;
import com.clinic.management.elnour.models.EmployeeObject;
import com.clinic.management.elnour.models.PatientObject;

import java.util.ArrayList;

public class PatientAdapter extends ArrayAdapter<PatientObject> {


    public static final String LOG_TAG = PatientAdapter.class.getSimpleName(); // class name.
    private final Context mContext; // for the activity context that the Adapter work at.


    public PatientAdapter(Context context, ArrayList<PatientObject> patientObjects) {
        super(context, 0, patientObjects);

        mContext = context; // to determine the specific place that the adapter works in.
    }



    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View listItemView = convertView;

        if (listItemView == null) {

            listItemView =  LayoutInflater.from(mContext).inflate(R.layout.item_employee, parent, false);

        }



        PatientObject patientObject = getItem(position);

        // determine the views from the inflated layout.
        TextView nameTextView = listItemView.findViewById(R.id.item_employee_name);
        TextView sessionsTextView = listItemView.findViewById(R.id.item_employee_sessions);


        nameTextView.setText(patientObject.getName());
        String sessionsNumberFormat = String.format("%.2f", patientObject.getAllSessionsNumber());
        String patientSessions = mContext.getResources().getString(R.string.employee_sessions, sessionsNumberFormat);
        sessionsTextView.setText(patientSessions);


        return listItemView;
    }

}
