package com.clinic.management.elnour.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.clinic.management.elnour.R;
import com.clinic.management.elnour.models.EmployeeObject;
import com.clinic.management.elnour.models.SessionObject;

import java.util.ArrayList;

public class SessionAdapter extends ArrayAdapter<SessionObject> {


    public static final String LOG_TAG = SessionAdapter.class.getSimpleName(); // class name.
    private final Context mContext; // for the activity context that the Adapter work at.


    public SessionAdapter(Context context, ArrayList<SessionObject> sessionObjects) {
        super(context, 0, sessionObjects);

        mContext = context; // to determine the specific place that the adapter works in.
    }



    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        View listItemView = convertView;

        if (listItemView == null) {


//            LayoutInflater.from(mContext).inflate(R.layout.downloads_item, null);

            listItemView =  LayoutInflater.from(mContext).inflate(R.layout.item_employee, parent, false);


        }



        SessionObject sessionObject = getItem(position);

        // determine the views from the inflated layout.
        TextView doctorNameTextView = listItemView.findViewById(R.id.item_employee_name);
        TextView patientNameTextView = listItemView.findViewById(R.id.item_patient_name);
        TextView sessionsTextView = listItemView.findViewById(R.id.item_employee_sessions);


        doctorNameTextView.setText(sessionObject.getDoctorName());
        patientNameTextView.setText(sessionObject.getPatientName());
        patientNameTextView.setVisibility(View.VISIBLE);

        String sessionNumbersFormat = String.format("%.2f", sessionObject.getSessionsNumber());
        String employeeSessions = mContext.getResources().getString(R.string.employee_sessions, sessionNumbersFormat);
        sessionsTextView.setText(employeeSessions);


        return listItemView;
    }

}
