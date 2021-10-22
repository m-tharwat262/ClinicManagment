package com.clinic.management.elnour.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.clinic.management.elnour.R;
import com.clinic.management.elnour.models.EmployeeObject;
import com.clinic.management.elnour.models.SalaryObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SalaryAdapter extends ArrayAdapter<SalaryObject> {


    public static final String LOG_TAG = SalaryAdapter.class.getSimpleName(); // class name.
    private final Context mContext; // for the activity context that the Adapter work at.
    private ArrayList<Boolean> mItemChecked = new ArrayList<>();


    public SalaryAdapter(Context context, ArrayList<SalaryObject> salaryObjects) {
        super(context, 0, salaryObjects);

        mContext = context; // to determine the specific place that the adapter works in.
    }



    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        View listItemView = convertView;

        if (listItemView == null) {

            listItemView =  LayoutInflater.from(mContext).inflate(R.layout.item_salary, parent, false);

        }



        SalaryObject salaryObject = getItem(position);

        // determine the views from the inflated layout.
        TextView nameTextView = listItemView.findViewById(R.id.item_salary_name);
        TextView sessionsTextView = listItemView.findViewById(R.id.item_salary_sessions);
        TextView salaryTextView = listItemView.findViewById(R.id.item_salary_salary);
        TextView dateTextView = listItemView.findViewById(R.id.item_salary_date);
        CheckBox checkBox = listItemView.findViewById(R.id.item_salary_check_box);


        nameTextView.setText(salaryObject.getName());

        String sessionNumbersFormat = String.format("%.2f", salaryObject.getSessionsNumber());
        String employeeSessions = mContext.getResources().getString(R.string.employee_sessions, sessionNumbersFormat);
        sessionsTextView.setText(employeeSessions);

        double salary = salaryObject.getSalary();
        String balance = mContext.getResources().getString(R.string.employee_balance, String.format("%.2f", salary));
        salaryTextView.setText(balance);


        String date = getSalaryDate(salaryObject.getGotSalaryDate());
        dateTextView.setText(date);



        final int currentPosition = position;

        // to know when the user click on the checkBox.
        checkBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // handle clicking on the checkBox and link it to the array (itemChecked).
                if (mItemChecked.get(currentPosition)) {
                    // if current checkbox is checked (true) before, make it not checked (false).
                    mItemChecked.set(currentPosition, false);
                } else {
                    // if current checkbox is not checked (false), make it checked (true).
                    mItemChecked.set(currentPosition, true);
                }

            }
        });

        // (important) set the checkbox state for all item base on the arrayList (itemChecked).
        // without it scrolling in the ListView will change the selected items.
        checkBox.setChecked(mItemChecked.get(position));




        return listItemView;
    }


    public void addItem(SalaryObject salaryObject) {

        this.add(salaryObject);
        mItemChecked.add(salaryObject.isGotSalary());

    }

    public void insertItem(SalaryObject salaryObject, int position) {

        insert(salaryObject, position);
        mItemChecked.remove(position);
        mItemChecked.add(position, salaryObject.isGotSalary());
        notifyDataSetChanged();

    }

    public ArrayList<Boolean> getItemChecked() {

        return mItemChecked;

    }


    private String getSalaryDate(long unixTime) {

        if (unixTime == -1) {

            return mContext.getString(R.string.not_receive_his_salary);

        } else {

            Date dateObject = new Date(unixTime * 1000);
            SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd, yyyy");
            return dateFormat.format(dateObject);

        }

    }


}
