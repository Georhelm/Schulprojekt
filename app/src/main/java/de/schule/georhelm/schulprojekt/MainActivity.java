package de.schule.georhelm.schulprojekt;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.prefs = this.getSharedPreferences("users", MODE_PRIVATE);

        SharedPreferences.Editor editor = this.prefs.edit();

        editor.putString("username", "Ich");
        editor.putString("password", "pass");
        editor.apply();
    }

    public void tryLogin(View v){
        EditText userField = this.findViewById(R.id.inputUser);
        String user = userField.getText().toString();

        EditText passwordField = this.findViewById(R.id.inputPassword);
        String password = passwordField.getText().toString();

        if (user.equals(this.prefs.getString("username", null)) && password.equals(this.prefs.getString("password", null))){
            Toast.makeText(this, "logged in", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "wrong login", Toast.LENGTH_LONG).show();
        }
    }
}