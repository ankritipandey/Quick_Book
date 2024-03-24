package com.example.skd.book_order;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.skd.book_order.Interface.ItemClickListener;
import com.example.skd.book_order.Model.book;
import com.example.skd.book_order.ViewHolder.bookViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class bookList extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference bookList;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    String categoryId = "";
    FirebaseRecyclerAdapter<book, bookViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        //Firebase
        database = FirebaseDatabase.getInstance();
        bookList = database.getReference("books");

        recyclerView = (RecyclerView) findViewById(R.id.recycler_book);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Get Intent here
        if(getIntent() != null)
            categoryId = getIntent().getStringExtra("CategoryId");
        if(!categoryId.isEmpty() && categoryId != null){
            loadListbook(categoryId);
        }


    }

    private void loadListbook(String categoryID){
        adapter = new FirebaseRecyclerAdapter<book, bookViewHolder>(book.class,
                R.layout.book_item,
                bookViewHolder.class,
                bookList.orderByChild("MenuId").equalTo(categoryId)) { //like Select * from books where MenuID =
            @Override
            protected void populateViewHolder(bookViewHolder viewHolder, book model, int position) {
                viewHolder.book_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.book_image);

                final book local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //start new activity
                        Intent bookDetail =  new Intent(bookList.this, booksDetail.class);
                        bookDetail.putExtra("bookId", adapter.getRef(position).getKey());
                        startActivity(bookDetail);
                    }
                });
            }
        };
        //Set Adapter
        Log.d("TAG", ""+adapter.getItemCount());
        recyclerView.setAdapter(adapter);
    }
}
