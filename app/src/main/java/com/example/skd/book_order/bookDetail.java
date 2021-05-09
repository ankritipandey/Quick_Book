package com.example.skd.book_order;

import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.skd.book_order.Database.Database;
import com.example.skd.book_order.Model.book;
import com.example.skd.book_order.Model.Order;
import com.example.skd.book_order.ViewHolder.bookViewHolder;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class booksDetail extends AppCompatActivity {

    TextView book_name, book_price, book_description;
    ImageView book_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart;
    ElegantNumberButton numberButton;
    FirebaseDatabase database;
    DatabaseReference books;
    book currentbook;

    String bookId = "";
    FirebaseRecyclerAdapter<book, bookViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        //Firebase
        database = FirebaseDatabase.getInstance();
        books = database.getReference("books");

        //Init View
        numberButton = (ElegantNumberButton) findViewById(R.id.number_button);
        btnCart = (FloatingActionButton) findViewById(R.id.btnCart);

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Database(getBaseContext()).addToCart(new Order(
                        bookId,
                        currentbook.getName(),
                        numberButton.getNumber(),
                        currentbook.getPrice(),
                        currentbook.getDiscount()
                ));


            }
        });

        book_description = (TextView) findViewById(R.id.book_description);
        book_name = (TextView) findViewById(R.id.book_name);
        book_price = (TextView) findViewById(R.id.book_price);
        book_image = (ImageView) findViewById(R.id.img_book);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        //Get book ID from Intent
        if(getIntent() != null)
            bookId = getIntent().getStringExtra("bookId");
        if(!bookId.isEmpty()){
            getDetailbook(bookId);
        }

    }

    private void getDetailbook(String bookId) {
        books.child(bookId).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                currentbook = dataSnapshot.getValue(book.class);

                //Set Image
                Picasso.with(getBaseContext()).load(currentbook.getImage()).into(book_image);

                collapsingToolbarLayout.setTitle(currentbook.getName());

                book_price.setText(currentbook.getPrice());

                book_name.setText(currentbook.getName());

                book_description.setText(currentbook.getDescription());

            }

            @Override
            public void onCancelled(DatabaseError databaseError){

            }
        });

    }
}
