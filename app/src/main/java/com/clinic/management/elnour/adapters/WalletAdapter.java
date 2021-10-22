package com.clinic.management.elnour.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.clinic.management.elnour.R;
import com.clinic.management.elnour.models.PatientObject;

import java.util.ArrayList;

public class WalletAdapter extends ArrayAdapter<PatientObject> {


    public static final String LOG_TAG = WalletAdapter.class.getSimpleName(); // class name.
    private final Context mContext; // for the activity context that the Adapter work at.
    private ArrayList<Boolean> mItemChecked = new ArrayList<>();


    public WalletAdapter(Context context, ArrayList<PatientObject> patientObjects) {
        super(context, 0, patientObjects);

        mContext = context; // to determine the specific place that the adapter works in.
    }



    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        View listItemView = convertView;

        if (listItemView == null) {

            listItemView =  LayoutInflater.from(mContext).inflate(R.layout.item_wallet, parent, false);

        }



        PatientObject patientObject = getItem(position);

        // determine the views from the inflated layout.
        TextView nameTextView = listItemView.findViewById(R.id.item_wallet_patient_name);
        TextView sessionCostTextView = listItemView.findViewById(R.id.item_wallet_session_cost);
        TextView walletTextView = listItemView.findViewById(R.id.item_wallet_wallet);


        nameTextView.setText(patientObject.getName());

        double sessionsCost = patientObject.getSessionCost();
        sessionCostTextView.setText("Sessions Cost : " + sessionsCost);


        double wallet = patientObject.getWallet();
        String deptFormat =  String.format("%.2f", wallet);
        walletTextView.setText("Wallet : " + deptFormat);




        return listItemView;


    }



}

