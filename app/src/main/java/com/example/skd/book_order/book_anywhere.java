package com.example.skd.book_order;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class book_anywhere extends AppCompatActivity {
//TextView txtSlogan;
Button btnSignIn;
Button btnSignUp;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn= (Button)  findViewById(R.id.btnSignIn);
        btnSignUp=(Button) findViewById(R.id.btnSignUp);
       // txtSlogan= (TextView) findViewById(R.id.txtSlogan);
       // Typeface face = Typeface.createFromAsset(getAssets(),"fonts/Nabila.ttf");
      //  txtSlogan.setTypeface(face);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signin = new Intent(book_anywhere.this, SignIn.class);
                startActivity(signin);
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signup = new Intent(book_anywhere.this, SignUp.class);
                startActivity(signup);
            }
        });

    }
}
