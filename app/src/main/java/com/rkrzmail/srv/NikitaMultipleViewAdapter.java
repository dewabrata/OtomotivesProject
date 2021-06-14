package com.rkrzmail.srv;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.naa.data.Nson;
import com.naa.data.Utility;

import java.util.ArrayList;

public class NikitaMultipleViewAdapter extends RecyclerView.Adapter<NikitaViewHolder> {

    public static final int ITEM_VIEW_1 = 1;
    public static final int ITEM_VIEW_2 = 2;
    Nson nson;
    int rlayout;
    int rlayout2;


    public NikitaMultipleViewAdapter(Nson nson, int rlayout, int rlayout2) {
        this.nson = nson;
        this.rlayout = rlayout;
        this.rlayout2 = rlayout2;
    }

    public Nson getItem() {
        return nson;
    }

    @Override
    public final int getItemViewType(int position) {
        if(!nson.get("PART").asString().isEmpty() || !nson.get("JASA_LAIN").asString().isEmpty()){
            if(nson.get("PART").get(position).get("PARENT VIEW TYPE") != null && nson.get(position).get("PARENT VIEW TYPE").asInteger() == ITEM_VIEW_1) {
                return ITEM_VIEW_1;
            }else if (nson.get("JASA_LAIN").get(position).get("PARENT VIEW TYPE") != null && nson.get(position).get("PARENT VIEW TYPE").asInteger() == ITEM_VIEW_2){
                return ITEM_VIEW_2;
            }else{
                return position;
            }
        }else{
            if(nson.get(position).get("PARENT VIEW TYPE") != null && nson.get(position).get("PARENT VIEW TYPE").asInteger() == ITEM_VIEW_1) {
                return ITEM_VIEW_1;
            }else if (nson.get(position).get("PARENT VIEW TYPE") != null && nson.get(position).get("PARENT VIEW TYPE").asInteger() == ITEM_VIEW_2){
                return ITEM_VIEW_2;
            }else{
                return position;
            }
        }

    }

    @Override
    public NikitaViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        NikitaViewHolder nikitaViewHolder;
        if(viewType == ITEM_VIEW_1){
            nikitaViewHolder = NikitaViewHolder.getInstance(viewGroup, rlayout);
            return nikitaViewHolder;
        }else if (viewType == ITEM_VIEW_2){
            nikitaViewHolder = NikitaViewHolder.getInstance(viewGroup, rlayout2);
            return nikitaViewHolder;
        }else{
            nikitaViewHolder = NikitaViewHolder.getInstance(viewGroup, rlayout);
            return nikitaViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {

    }


    @Override
    public int getItemCount() {
        return nson.size();
    }

    private OnItemClickListener onitemClickListener;

    public NikitaMultipleViewAdapter setOnitemClickListener(OnItemClickListener onitemClickListener) {
        this.onitemClickListener = onitemClickListener;
        return this;
    }

    public interface OnItemClickListener {
        void onItemClick(Nson parent, View view, int position);
    }
}
