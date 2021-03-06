package com.example.listview;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.listview.controller.PersonDao;
import com.example.listview.model.Person;

import java.io.Serializable;

public class MainActivity extends AppCompatActivity {

    public static final String PERSON_LIST_KEY = "PersonList";

    private int currentPersonId = 0;
    private EditText surnameEditText;
    private EditText nameEditText;


    // declare a launcher to call an intent to start
    // an execution process ActivityResultContracts
    ActivityResultLauncher<Intent> intentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // reference to UI components
        surnameEditText = findViewById(R.id.surnameEditText);
        nameEditText = findViewById(R.id.nameEditText);

        // create a launcher with a call back subscription
        intentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    onSecondActivityResult(result);
                }
            });
    }

    private void onSecondActivityResult(ActivityResult result) {
        // Check whether result is OK
        if (result.getResultCode() == Activity.RESULT_OK) {
            // retrieve the intent return by second Activity
            Intent data = result.getData();

            // get person in intent
            Person person = (Person) data.getSerializableExtra(SecondActivity.PERSON_KEY);
            currentPersonId = person.getId();

            // reload from db can not bind with this : Activity
//             Person personFromDao = PersonDao.findById(currentPersonId, this);
            surnameEditText.setText(person.getSurname());
            nameEditText.setText(person.getName());

            // log & Toast
            Log.d("App", "Return person" + person.toString());
            Toast.makeText(getBaseContext(), person.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void onClickSubmit(View view) {
        // get info from UI
        String surname = surnameEditText.getText().toString();
        String name = nameEditText.getText().toString();

        // add to temporal stock, id sera ajout?? lors que
        // rajoute dans la db
        Person person = new Person(surname, name);
        PersonDao.save(person, this);
        currentPersonId = person.getId();

        // launch second activity
        launchSecondActivity();
    }

    private void launchSecondActivity() {
        // Cr??e intent (message) pour demarrer second activity
        Intent intent = new Intent(this, SecondActivity.class);

        // on stock persons dans intent ?? envoyer SecondActivity
        intent.putExtra(PERSON_LIST_KEY, (Serializable) PersonDao.getAll(this));

        // start second activity with message
        // startActivity(intent);
        intentLauncher.launch(intent); // launch with a call back to process return data
    }

    public void onClickModify(View view) {
        if (currentPersonId == 0) return;

        // get info from UI
        String surname = surnameEditText.getText().toString();
        String name = nameEditText.getText().toString();

        // get current Person by id
        Person person = PersonDao.findById(currentPersonId, this);
        if (person == null) return;

        // update Pojo
        person.setName(name);
        person.setSurname(surname);

        // save to db
        PersonDao.save(person, this);

        // launch second activity
        launchSecondActivity();
    }

    public void onClickDelete(View view) {

        if (currentPersonId == 0) return;

        // get current Person by id
        Person person = PersonDao.findById(currentPersonId, this);

        // save to db
        PersonDao.delete(person.getId(), this);
        currentPersonId = 0;

        // launch second activity
        launchSecondActivity();
    }

    public void onClickShowAll(View view) {
        launchSecondActivity();
    }
}