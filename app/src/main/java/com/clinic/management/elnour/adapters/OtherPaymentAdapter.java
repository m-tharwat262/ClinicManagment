package com.clinic.management.elnour.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.clinic.management.elnour.R;
import com.clinic.management.elnour.models.PatientObject;
import com.clinic.management.elnour.models.PaymentObject;

import java.util.ArrayList;

public class OtherPaymentAdapter extends ArrayAdapter<PaymentObject> {


    public static final String LOG_TAG = OtherPaymentAdapter.class.getSimpleName(); // class name.
    private final Context mContext; // for the activity context that the Adapter work at.


    public OtherPaymentAdapter(Context context, ArrayList<PaymentObject> paymentObjects) {
        super(context, 0, paymentObjects);

        mContext = context; // to determine the specific place that the adapter works in.
    }



    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        View listItemView = convertView;

        if (listItemView == null) {

            listItemView =  LayoutInflater.from(mContext).inflate(R.layout.item_other_payment, parent, false);

        }



        PaymentObject paymentObject = getItem(position);

        // determine the views from the inflated layout.
        TextView nameTextView = listItemView.findViewById(R.id.item_other_payment_item_name);
        TextView itemsNumberTextView = listItemView.findViewById(R.id.item_other_payment_number_of_items);
        TextView itemsCostTextView = listItemView.findViewById(R.id.item_other_payment_items_cost);


        nameTextView.setText(paymentObject.getName());


        double itemsNumber = paymentObject.getItemsNumber();
        itemsNumberTextView.setText("Items Number : " + itemsNumber);


        double itemCost = paymentObject.getCost();
        double allItemsCost = itemCost * itemsNumber;
        itemsCostTextView.setText("All Cost : " + allItemsCost);




        return listItemView;


    }



}

