package com.example.skd.book_order;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.skd.book_order.Common.Common;
import com.example.skd.book_order.Database.Database;
import com.example.skd.book_order.Model.book;
import com.example.skd.book_order.Model.Order;
import com.example.skd.book_order.Model.Request;
import com.example.skd.book_order.Model.Receipt;
import com.example.skd.book_order.ViewHolder.CartAdapter;
import com.example.skd.book_order.ViewHolder.CartAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.skd.book_order.Cart.inventory;
import static com.example.skd.book_order.Cart.inventoryList;
import static com.example.skd.book_order.Cart.requestList;
import static com.example.skd.book_order.Cart.requestId;
import static com.example.skd.book_order.Cart.total;

//this thread updates the inventoryList from firebase
class IventoryListThread implements Runnable
{
    DatabaseReference books = FirebaseDatabase.getInstance().getReference("books");

    public void run()
    {
        books.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Cart.inventoryList.clear();
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Cart.inventory = singleSnapshot.getValue(book.class);
                    Cart.inventoryList.add(Cart.inventory);
                }
                System.out.println(Cart.inventoryList.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}


public class Cart extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    TextView txtTotalPrice;
    Button btnPlace;
    float totalPrice;

    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    //name the threads
    Thread inventorylistthread = new Thread(new IventoryListThread());
    Thread kitchenthread = new Thread(new KitchenThread());

    //name the variables in static, so they can be accessed and updated by the inventorylistthread
    static List<List<Order>> orderList = new ArrayList<>();
    static List<book> inventoryList = new ArrayList<>();
    static book inventory;
    static List<String> requestId  = new ArrayList<>();
    //The orderList is for inventoryList, the requestList is for the KitchenThread
    static List<Request> requestList = new ArrayList<>();
    static float total;

    //partial request flag
    private boolean partial = false;

    //unavailable book information
    static String unavailablebooknames="";
    static float unavailablebookprice=0;

    //The executor can makes inventorylistthread running in interval, which is 1 hour
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //thread running in 1 hour interval
        executor.scheduleAtFixedRate(inventorylistthread, 0, 60, TimeUnit.MINUTES);

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests =  database.getReference("Requests");

        //Init
        recyclerView = findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        txtTotalPrice = findViewById(R.id.total);
        btnPlace = findViewById(R.id.btnPlaceOrder);

        //When the "Place Order" button clicked
        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update invetoryList immediately first
                executor.scheduleAtFixedRate(inventorylistthread, 0, 60, TimeUnit.MINUTES);
                if(!checkavailability(cart)) {

                    //Create new Request
                    showAlertDialog();

                }else {

                    //Show user the "Partial order or cancel order options" dialog,
                    System.out.println("book Unavailble");
                    AlertDialog.Builder alertPartialDialog = new AlertDialog.Builder(Cart.this);
                    alertPartialDialog.setTitle(unavailablebooknames + " is unavailable");
                    unavailablebooknames="";
                    alertPartialDialog.setMessage("Do you accept partial order ?");

                    alertPartialDialog.setPositiveButton("YES", new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            partial = true;
                            showAlertDialog();
                        }
                    });

                    alertPartialDialog.show();

                    //If user choose Partial order, then do showAlertDialog() again, and set the partial flag to true in order to set this request partially
                    /*showAlertDialog();
                    partial = true;*/

                }
            }
        });

        loadListbook();

    }

    //Find out whether the books in order contains unavailable book
    private boolean checkavailability(List<Order> cart){
        boolean partial = false;
        if(cart.size()==0){
            //Cart is empty, do nothing
            partial = true;
        }
        for(Order order : cart){
            for(book book: inventoryList){
                /*System.out.println("SH-----------IT"+book.getbookId());
                System.out.println("FU-----------CK"+order.getProductId());
                System.out.println("DA-----------MN"+book.getAvailabilityFlag());*/
                if(book.getbookId().equals(order.getProductId())){
                    if(book.getAvailabilityFlag().equals("0")){
                        //if the availabilityFlag of this book is "0"
                        partial = true;
                        unavailablebookprice += Integer.parseInt(book.getPrice());
                        unavailablebookprice = (float) (unavailablebookprice*1.36);
                        unavailablebooknames += book.getName();
                    }
                }
            }


        }
        return partial;
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("Enter your address");
        System.out.println("email address ");
        final EditText edtAddress = new EditText(Cart.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        edtAddress.setLayoutParams(lp);
        alertDialog.setView(edtAddress);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        edtAddress.getText().toString(),
                        txtTotalPrice.getText().toString(),
                        cart
                );

                if(partial) {
                    request.setPartial(true);
                    //add the request to top of the requestList if it's partial request
                    requestList.add(0,request);

                    //default partial is false, set it back to false to check next request
                    partial = false;

                    //cut the price of unavailable book
                    //keep track of totalprice using global variable
                    //txtTotalPrice is in currency format unable to parse
                    Locale locale = new Locale("en","US");
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                    request.setTotal(fmt.format(totalPrice - unavailablebookprice));
                    unavailablebookprice = 0;



                }else {
                    requestList.add(request);
                }

                //Submit to Firebase
                //We will using System.Current
                requestId.add(String.valueOf(System.currentTimeMillis()));
                requests.child(requestId.get(requestId.size()-1)).setValue(request);

                //run the kitchenthread
                kitchenthread.start();

                //Delete the cart
                new Database(getBaseContext()).cleanCart();

                Toast.makeText(Cart.this, "Thank you, Order placed", Toast.LENGTH_SHORT).show();
                finish();

//                Code to show notification
                Intent intent = new Intent();
                PendingIntent pendingIntent = PendingIntent.getActivity(Cart.this,0,intent,0);
                Notification.Builder notificationBuilder = new Notification.Builder(Cart.this)
                        .setTicker("Order Placed").setContentTitle("Order Placed")
                        .setContentText("Your order is processing now").setSmallIcon(R.drawable.logo)
                        .setContentIntent(pendingIntent);
                Notification notification = notificationBuilder.build();
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                assert nm != null;
                nm.notify(0,notification);
            }
        });


        /*alertDialog.setPositiveButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });*/

        alertDialog.show();
        //                This code is to show notifications in android status bar when user places order
        // notification is selected



    }

    //this thread cooks requests
    class KitchenThread implements Runnable{

        @Override
        public void run() {
            while (true) {
                while (requestList.size() > 0) {
                    DatabaseReference requests = FirebaseDatabase.getInstance().getReference("Requests");
                    requests.child(requestId.get(0)).child("status").setValue("1");
                    System.out.println("The chef is working on requests!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                    Intent intent = new Intent();
                    PendingIntent pendingIntent = PendingIntent.getActivity(Cart.this,0,intent,0);
                    Notification.Builder notificationBuilder = new Notification.Builder(Cart.this)
                            .setTicker("Order Accepted").setContentTitle("Order Accepted")
                            .setContentText("The Chef is working on your order").setSmallIcon(R.drawable.logo)
                            .setContentIntent(pendingIntent);
                    Notification notification = notificationBuilder.build();
                    notification.flags = Notification.FLAG_AUTO_CANCEL;
                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    assert nm != null;
                    nm.notify(0,notification);

                    try {
                        //cooking time: 180 sec
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.println("Order Prepared!");

                    Intent intent1 = new Intent();
                    PendingIntent pendingIntent1 = PendingIntent.getActivity(Cart.this,0,intent1,0);
                    Notification.Builder notificationBuilder1 = new Notification.Builder(Cart.this)
                            .setTicker("Order Prepared").setContentTitle("Order Prepared")
                            .setContentText("Your order is prepared").setSmallIcon(R.drawable.logo)
                            .setContentIntent(pendingIntent1);
                    Notification notification1 = notificationBuilder1.build();
                    notification1.flags = Notification.FLAG_AUTO_CANCEL;
                    NotificationManager nm1 = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    assert nm1 != null;
                    nm1.notify(0,notification1);

                    requests.child(requestId.get(0)).child("status").setValue("2");
                    try {
                        //packaging time: 180 sec
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Order Packaged!");

                    Intent intent2 = new Intent();
                    PendingIntent pendingIntent2 = PendingIntent.getActivity(Cart.this,0,intent2,0);
                    Notification.Builder notificationBuilder2 = new Notification.Builder(Cart.this)
                            .setTicker("Order Packaged").setContentTitle("Order Packaged")
                            .setContentText("Your order is packaged and ready to pick up!").setSmallIcon(R.drawable.logo)
                            .setContentIntent(pendingIntent2);
                    Notification notification2 = notificationBuilder2.build();
                    notification2.flags = Notification.FLAG_AUTO_CANCEL;
                    NotificationManager nm2 = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    assert nm2 != null;
                    nm2.notify(0,notification2);

                    requests.child(requestId.get(0)).child("status").setValue("3");

                    //The first request finished, generate receipt, then remove the finished request, working on next request in list
                    Looper.prepare();
                    Toast.makeText(Cart.this,GenerateReceipt(requestList.get(0)),Toast.LENGTH_SHORT).show();
                    requestList.remove(0);
                    requestId.remove(0);

                    Looper.loop();

                }
                System.out.println("there are no requests yet, what a terrible day!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        }

        public String GenerateReceipt(Request request){
            Receipt receipt = new Receipt();
            receipt.items = request.getbooks();
            receipt.totalcost = request.getTotal();
            String message="---------book Ready! Thank you!---------\n";
            for(Order i:receipt.items){
                message+=i.getProductName()+" :"+i.getQuanlity()+"\n";
            }
            message+="Total: "+total;
            return message;


        }
    }


    private void loadListbook() {
        cart = new Database(this).getCarts();
        orderList.add(cart);
        adapter = new CartAdapter(cart,this);
        recyclerView.setAdapter(adapter);

        //Calculate total price
        total = 0;
        for(Order order:cart)
            total+=(float) (Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuanlity()));
        Locale locale = new Locale("en","US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);


        // add tax, profit to total, do we need to show the tax and profit on the app??
        float tax= (float) (total*0.06);
        float profit = (float) (total*0.3);
        total+=tax+profit;

        totalPrice =total;

        txtTotalPrice.setText(fmt.format(total));

    }

    //Delete item


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        System.out.println("I CAME HERE?");
        System.out.println(item.getTitle());
        if(item.getTitle().equals(Common.DELETE)){
            System.out.println("I CAME HERE YAY");
            System.out.println(item.getItemId());
            System.err.println(item.getOrder());
            deletebookItem(item.getOrder());
        }
        return super.onContextItemSelected(item);
    }

    private void deletebookItem(int ord) {
        cart = new Database(this).getCarts();
        String order1 = cart.get(ord).getProductId();
        System.out.println("The order is " +order1);
        for(Order order:cart){
            System.err.println("The order id is " + order.getProductId());
            if(order.getProductId().equals(order1)){
                System.err.println(order.getProductName());
                System.err.println("I CAME HERE FUCK YEAAAAAHHHHHH");
                new Database(getBaseContext()).removeFromCart(order1);
            }
        }
        loadListbook();
    }

}