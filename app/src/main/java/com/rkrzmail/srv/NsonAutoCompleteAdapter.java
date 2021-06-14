package com.rkrzmail.srv;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.naa.data.Nson;
import com.rkrzmail.oto.R;

import java.util.ArrayList;
import java.util.List;

public class NsonAutoCompleteAdapter extends BaseAdapter implements Filterable {

    private static final int MAX_RESULTS = 10;
    private Context mContext;
    private Nson resultList = new Nson();

    public NsonAutoCompleteAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Nson getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       return getView(position, convertView,parent);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    Nson books = onFindNson(mContext, constraint.toString());
                    String s = Thread.currentThread().getName();
                    // Assign the data to the FilterResults
                    filterResults.values = books;
                    filterResults.count = books.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                String s = Thread.currentThread().getName();
                if (results != null && results.count > 0) {
                    resultList = (Nson) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }};
    }

    /**
     * Returns a search result for the given book title.
     */
    public Nson onFindNson(Context context, String bookTitle) {
        // GoogleBooksProtocol is a wrapper for the Google Books API
        /*GoogleBooksProtocol protocol = new GoogleBooksProtocol(context, MAX_RESULTS);
        return protocol.findBooks(bookTitle);*/
        return Nson.newObject();
    }

}
