package ru.progmatik.meterssender.adapters;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.progmatik.meterssender.R;

public class CustomAutoCompleteAdapter extends BaseAdapter implements Filterable {
    /*private ArrayMap<String,String> inputData, outputData;
    private final Context mContext;
    private Dialog dialog;
    private List<String> listData;*/

    private List<String> inputData, outputData;
    private final Context mContext;
    private Dialog dialog;
    private int i = 1;

    private Filter mFilter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null) {
                List<String> suggestions = new ArrayList<String>() {
                };




                for (String set : inputData) {
                    if (i == 1) {
                        if (set.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            suggestions.add(set);
                        }
                    } else {
                        if (set.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                            suggestions.add(set);
                        }
                    }
                }
                results.values = suggestions;
                results.count = suggestions.size();

            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null && results.count > 0) {
                //outputData = (ArrayMap<String,String>) results.values;
                outputData = (ArrayList<String>) results.values;
                Collections.sort(outputData.subList(1, outputData.size())); // сортируем
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    };

    public CustomAutoCompleteAdapter(Context context, List<String> inputData, Dialog dialog) {
        this.mContext = context;
        this.inputData = inputData;
        this.dialog = dialog;
    }

    public CustomAutoCompleteAdapter(Context context, List<String> inputData, Dialog dialog, int i) {
        this.mContext = context;
        this.inputData = inputData;
        this.dialog = dialog;
        this.i = i;
    }


    @Override
    public int getCount() {
        return outputData.size();
    }

    @Override
    public String getItem(int position) {
        //String res = outputData.keyAt(position);
        String res = outputData.get(position);
        return res;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.fragment_settings_address_dialog_row,parent, false);
        }

        ((TextView) convertView.findViewById(R.id.tvaddress_row))
                .setText(getItem(position));
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }
}
