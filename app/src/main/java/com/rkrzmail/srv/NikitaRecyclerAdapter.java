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

public class NikitaRecyclerAdapter extends RecyclerView.Adapter<NikitaViewHolder>  {
    public static final int VIEW_1 = 1;
    public static final int VIEW_2 = 2;
    Nson nson;
    Nson nson2;
    int rlayout;
    int rlayout2;
    boolean isView2 = false;
    String parent1;
    String parent2;

    public NikitaRecyclerAdapter(Nson nson, int rlayout) {
        this.nson = nson;
        this.rlayout = rlayout;
    }

    public NikitaRecyclerAdapter(Nson nson, Nson nson2, int rlayout, int rlayout2, String parent1, String parent2) {
        this.nson = nson;
        this.rlayout = rlayout;
        this.rlayout2 = rlayout2;
        this.nson2 = nson2;
        this.parent1 = parent1;
        this.parent2 = parent2;
    }

    public Nson getItem() {
        return nson ;
    }

    public Nson getItem2(){
        return nson2;
    }

    @Override
    public final int getItemViewType(int position) {
        if(!nson.get(position).get(parent1).asString().isEmpty()){
            return VIEW_1;
        }
        if(!nson2.get(position).get(parent2).asString().isEmpty()){
            return VIEW_2;
        }
        return -1;
    }

    @Override
    public NikitaViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int position) {
        NikitaViewHolder nikitaViewHolder;
        if(position == VIEW_2){
            nikitaViewHolder = NikitaViewHolder.getInstance(viewGroup, rlayout2);
        }else{
            nikitaViewHolder = NikitaViewHolder.getInstance(viewGroup, rlayout);
        }
//
//        if (onitemClickListener != null) {
//            nikitaViewHolder.itemView.setTag(String.valueOf(position));
//            nikitaViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    if (onitemClickListener != null) {
//                        onitemClickListener.onItemClick(nson, v, Utility.getInt(String.valueOf(v.getTag())));
//                    }
//                }
//            });
//        }
        return nikitaViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {

    }


    @Override
    public int getItemCount() {
        return nson.size() + nson2.size();
    }

    private OnItemClickListener onitemClickListener;

    public NikitaRecyclerAdapter setOnitemClickListener(OnItemClickListener onitemClickListener) {
        this.onitemClickListener = onitemClickListener;
        return this;
    }

    public interface OnItemClickListener {
        void onItemClick(Nson parent, View view, int position);
    }
}
