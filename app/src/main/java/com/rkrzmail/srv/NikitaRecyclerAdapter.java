package com.rkrzmail.srv;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.naa.data.Nson;
import com.naa.data.Utility;

import java.util.ArrayList;

public class NikitaRecyclerAdapter extends RecyclerView.Adapter<NikitaViewHolder> {
    Nson nson;
    int rlayout;
    public NikitaRecyclerAdapter(Nson nson, int rlayout){
        this.nson = nson;
        this.rlayout = rlayout;
    }
    public Nson getItem(){
        return nson;
    }
    @Override
    public final int getItemViewType(int position) {
        return position;
    }
    @Override
    public NikitaViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        NikitaViewHolder nikitaViewHolder = NikitaViewHolder.getInstance(viewGroup, rlayout);
        if (onitemClickListener!=null){
            nikitaViewHolder.itemView.setTag(String.valueOf(position));
            nikitaViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (onitemClickListener!=null){
                        onitemClickListener.onItemClick(nson, v , Utility.getInt(String .valueOf(v.getTag())));
                    }
                }
            });
        }
        return nikitaViewHolder;
    }
    @Override
    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {

    }
    @Override
    public int getItemCount() {
        return nson.size();
    }

    private OnItemClickListener onitemClickListener;

    public NikitaRecyclerAdapter setOnitemClickListener(OnItemClickListener onitemClickListener){
        this.onitemClickListener=onitemClickListener;
        return this;
    }

    public interface OnItemClickListener {
        void onItemClick(Nson parent, View view, int position);
    }

}
