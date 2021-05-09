package com.example.skd.book_order.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.skd.book_order.Interface.ItemClickListener;
import com.example.skd.book_order.R;

/**
 * Created by 123456 on 2017/11/17.
 */

public class bookViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView book_name;
    public ImageView book_image;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public bookViewHolder(View itemView) {
        super(itemView);

        book_name =(TextView)itemView.findViewById(R.id.book_name);
        book_image = (ImageView)itemView.findViewById(R.id.book_image);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        itemClickListener.onClick(view, getAdapterPosition(), false);

    }

}
