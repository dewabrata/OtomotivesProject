package com.rkrzmail.srv;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@SuppressLint("AppCompatCustomView")
public class MultiSelectionSpinner extends Spinner implements
        OnMultiChoiceClickListener {

    public interface OnMultipleItemsSelectedListener {
        void selectedIndices(List<Integer> indices);

        void selectedStrings(List<String> strings);
    }

    private OnMultipleItemsSelectedListener listener;
    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    String[] _items = null;
    boolean[] mSelectionBool = null;
    boolean[] mSelectionAtStartBool = null;
    boolean isDiabledAll = false;
    boolean isAll = false;
    String _itemsAtStart = null;

    ArrayAdapter<String> simple_adapter;

    public MultiSelectionSpinner(Context context) {
        super(context);

        simple_adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item);
        super.setAdapter(simple_adapter);
    }

    public MultiSelectionSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        simple_adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item);

        super.setAdapter(simple_adapter);
    }

    public void setListener(OnMultipleItemsSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (mSelectionBool != null && which < mSelectionBool.length) {
            mSelectionBool[which] = isChecked;
            simple_adapter.clear();
            simple_adapter.add(buildSelectedItemString());
        } else {
            throw new IllegalArgumentException(
                    "Argument 'which' is out of bounds.");
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean performClick() {
        if(_items == null || _items.length == 0){
            MDToast.makeText(getContext(), "Item Tidak Tersedia", Toast.LENGTH_SHORT, MDToast.TYPE_WARNING).show();
            return true;
        }
        builder.setMultiChoiceItems(_items, mSelectionBool, this);
        _itemsAtStart = getSelectedItemsAsString();
        if(!isDiabledAll){
            builder.setPositiveButton("Pilih", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.arraycopy(mSelectionBool, 0, mSelectionAtStartBool, 0, mSelectionBool.length);
                    listener.selectedIndices(getSelectedIndices());
                    listener.selectedStrings(getSelectedStrings());
                }
            });
            builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        if(simple_adapter.getCount() == 0){
                            simple_adapter.clear();
                            simple_adapter.add(_itemsAtStart);
                            System.arraycopy(mSelectionAtStartBool, 0, mSelectionBool, 0, mSelectionAtStartBool.length);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        builder.create();
        builder.show();
        return true;
    }


    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        throw new RuntimeException(
                "setAdapter is not supported by MultiSelectSpinner.");
    }

    public void setTittle(String tittle){
        if(builder != null){
            builder.setTitle(tittle);
        }
    }

    public void setItems(String[] items) {
        _items = items;
        mSelectionBool = new boolean[_items.length];
        mSelectionAtStartBool = new boolean[_items.length];
        simple_adapter.clear();
       // simple_adapter.add(_items[0]);
        Arrays.fill(mSelectionBool, false);
      //  mSelectionBool[0] = true;
       // mSelectionAtStartBool[0] = true;
    }

    public void setItems(List<String> items) {
        _items = items.toArray(new String[items.size()]);
        mSelectionBool = new boolean[_items.length];
        mSelectionAtStartBool = new boolean[_items.length];
        simple_adapter.clear();
        //simple_adapter.add(_items[0]);
        Arrays.fill(mSelectionBool, false);
      //  mSelectionBool[0] = true;
    }

    public void setItems(List<String> items, boolean isLoad) {
        _items = items.toArray(new String[items.size()]);
        mSelectionBool = new boolean[_items.length];
        mSelectionAtStartBool = new boolean[_items.length];
        simple_adapter.clear();
        Arrays.fill(mSelectionBool, false);
        if(isLoad){
            simple_adapter.add(_items[0]);
            mSelectionBool[0] = true;
        }
    }


    public void setItems(List<String> items, List<String> listChecked) {
        _items = items.toArray(new String[0]);
        mSelectionBool = new boolean[_items.length];
        mSelectionAtStartBool = new boolean[_items.length];
        if(listChecked != null){
            for (int j = 0; j < _items.length;j++) {
                for (int i = 0; i < listChecked.size(); i++) {
                    String item = listChecked.get(i);
                    if (item.equals(_items[j])) {
                        mSelectionBool[j] = true;
                      // mSelectionAtStartBool[j] = true;
                    }
                }
            }
        }else{
            Arrays.fill(mSelectionBool, false);
          //  mSelectionBool[0] = true;
           // mSelectionAtStartBool[0] = true;
        }

        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
        builder.setMultiChoiceItems(_items, mSelectionBool, this);
    }

    public void setSelection(String[] selection) {
        for (int i = 0; i < mSelectionBool.length; i++) {
            mSelectionBool[i] = false;
            mSelectionAtStartBool[i] = false;
        }
        for (String cell : selection) {
            for (int j = 0; j < _items.length; ++j) {
                if (_items[j].equals(cell)) {
                    mSelectionBool[j] = true;
                    mSelectionAtStartBool[j] = true;
                }
            }
        }
        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }

    public boolean isAll(boolean isAll){
        this.isAll = isAll;
        return this.isAll;
    }

    public void setDisabled(){
        isDiabledAll = true;
        simple_adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item){
            @Override
            public boolean isEnabled(int position) {
                return false;
            }
        };
    }

    public void setSelection(List<String> selection) {
        for (String sel : selection) {
            for (int j = 0; j < _items.length; ++j) {
                if (_items[j].contains(sel)) {
                    mSelectionBool[j] = true;
                }
            }
        }
        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }

    public void setSelectionMatch(List<String> itemSet, List<String> loadFrom) {
        for (int i = 0; i < itemSet.size(); i++) {
            mSelectionBool[i] = loadFrom != null && loadFrom.contains(itemSet.get(i));
            //mSelectionAtStartBool[i] = loadFrom != null && loadFrom.contains(itemSet.get(i));
        }
        //simple_adapter.add(buildSelectedItemString());
    }

    public void setSelection(int[] selectedIndices) {
        for (int i = 0; i < mSelectionBool.length; i++) {
            mSelectionBool[i] = false;
            mSelectionAtStartBool[i] = false;
        }
        for (int index : selectedIndices) {
            if (index >= 0 && index < mSelectionBool.length) {
                mSelectionBool[index] = true;
                mSelectionAtStartBool[index] = true;
            } else {
                throw new IllegalArgumentException("Index " + index
                        + " is out of bounds.");
            }
        }
        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }

    public List<String> getSelectedStrings() {
        List<String> selection = new LinkedList<>();
        for (int i = 0; i < _items.length; ++i) {
            if (mSelectionBool[i]) {
                selection.add(_items[i]);
            }
        }
        return selection;
    }

    public List<Integer> getSelectedIndices() {
        List<Integer> selection = new LinkedList<>();
        for (int i = 0; i < _items.length; ++i) {
            if (mSelectionBool[i]) {
                selection.add(i);
            }
        }
        return selection;
    }

    private String buildSelectedItemString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < _items.length; ++i) {
            if (mSelectionBool[i]) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;

                sb.append(_items[i]);
            }
        }
        return sb.toString();
    }

    public String getSelectedItemsAsString() {
        try {
            StringBuilder sb = new StringBuilder();
            boolean foundOne = false;
            for (int i = 0; i < _items.length; ++i) {
                if (mSelectionBool[i] && !_items[i].equals("--PILIH--")) {
                    foundOne = true;
                    sb.append(_items[i]).append(", ");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
