package com.rkrzmail.srv;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

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

    String[] _items = null;
    boolean[] mSelectionBool = null;
    boolean[] mSelectionAtStartBool = null;
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

    @Override
    public boolean performClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Pilih");
        builder.setMultiChoiceItems(_items, mSelectionBool, this);
        _itemsAtStart = getSelectedItemsAsString();
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
                    simple_adapter.clear();
                    simple_adapter.add(_itemsAtStart);
                    System.arraycopy(mSelectionAtStartBool, 0, mSelectionBool, 0, mSelectionAtStartBool.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        builder.show();
        return true;
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        throw new RuntimeException(
                "setAdapter is not supported by MultiSelectSpinner.");
    }

    public void setItems(String[] items) {
        _items = items;
        mSelectionBool = new boolean[_items.length];
        mSelectionAtStartBool = new boolean[_items.length];
        simple_adapter.clear();
        simple_adapter.add(_items[0]);
        Arrays.fill(mSelectionBool, false);
        mSelectionBool[0] = true;
        mSelectionAtStartBool[0] = true;
    }

    public void setItems(List<String> items) {
        _items = items.toArray(new String[items.size()]);
        mSelectionBool = new boolean[_items.length];
        mSelectionAtStartBool = new boolean[_items.length];
        simple_adapter.clear();
        simple_adapter.add(_items[0]);
        Arrays.fill(mSelectionBool, false);
        mSelectionBool[0] = true;
    }

    public void setSelection(String[] selection, boolean isSelect) {
        for (int i = 0; i < mSelectionBool.length; i++) {
            mSelectionBool[i] = false;
            mSelectionAtStartBool[i] = false;
        }
        for (String cell : selection) {
            for (int j = 0; j < _items.length; ++j) {
                if (_items[j].equals(cell)) {
                    mSelectionBool[j] = isSelect;
                    mSelectionAtStartBool[j] = isSelect;
                }
            }
        }
        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }

    public void setSelection(List<String> selection, boolean isSelect) {
        for (int i = 0; i < mSelectionBool.length; i++) {
            mSelectionBool[i] = false;
            mSelectionAtStartBool[i] = false;
        }
        for (String sel : selection) {
            for (int j = 0; j < _items.length; ++j) {
                if (_items[j].equals(sel)) {
                    mSelectionBool[j] = isSelect;
                    mSelectionAtStartBool[j] = isSelect;
                }
            }
        }
        simple_adapter.clear();
        simple_adapter.add(buildSelectedItemString());
    }

    public void setSelectionMatch(List<String> itemSet, List<String> loadFrom) {
        for (int i = 0; i < itemSet.size(); i++) {
            mSelectionBool[i] = loadFrom != null && loadFrom.contains(itemSet.get(i));
            mSelectionAtStartBool[i] = loadFrom != null && loadFrom.contains(itemSet.get(i));
        }
        //simple_adapter.add(buildSelectedItemString());
    }

    public void setSelection(List<Integer> selectedIndices) {
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
                if (mSelectionBool[i]) {
                    if (foundOne) {
                        sb.append(", ");
                    }
                    foundOne = true;
                    sb.append(_items[i]);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
