package com.example.translator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

import static com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage.EN;
import static com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage.HI;

public class MainActivity extends AppCompatActivity {

    EditText text;
    TextView englishText;
    Button change;
    boolean changed=false;
    ImageButton mic;
    private static final int RECORD_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        text = findViewById(R.id.enter_text);
        englishText = findViewById(R.id.translatedText);
        change=findViewById(R.id.change);

        mic=findViewById(R.id.mic);

        mic.setVisibility(View.VISIBLE);
        final SpeechRecognizer mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());

        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                //getting all the matches
                ArrayList<String> matches = bundle
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                //displaying the first match
                if (matches != null)
                    text.setText(matches.get(0));
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });



        mic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        mSpeechRecognizer.stopListening();
                        break;

                    case MotionEvent.ACTION_DOWN:
                        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                        text.setText("");
                        break;
                }
                return false;
            }
        });


        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(changed==false)
                {
                    mic.setVisibility(View.GONE);
                    changed=true;
                }
                else
                {
                    mic.setVisibility(View.VISIBLE);
                    changed=false;
                }
            }
        });
    }

    public void translate(View v) {
        if(text!=null && text.length()>0) {
            translateTextToEnglish(text.getText().toString());
        }
        else
        {
            Toast.makeText(this, "Enter Text Please", Toast.LENGTH_SHORT).show();
        }
    }

    public void translateText(FirebaseTranslator langTranslator) {
        //translate source text to english
        langTranslator.translate(text.getText().toString())
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {
                                englishText.setText(translatedText);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this,
                                        "Problem in translating the text entered",
                                        Toast.LENGTH_LONG).show();
                            }
                        });

    }

    public void downloadTranslatorAndTranslate(String langCode) {
        //get source language id from bcp code
        int sourceLanguage=0;
        if(langCode.equals("en") ) {
            sourceLanguage = FirebaseTranslateLanguage
                    .languageForLanguageCode(langCode);
        }
        else if(langCode.equals("hi"))
        {
            sourceLanguage = FirebaseTranslateLanguage
                    .languageForLanguageCode(langCode);
        }
        Log.i("TAG", "downloadTranslatorAndTranslate: "+sourceLanguage);
        FirebaseTranslatorOptions options ;
        //create translator for source and target languages
        if(!changed && sourceLanguage==11)              //here 11 is code of english language
        {
            options =
                    new FirebaseTranslatorOptions.Builder()
                            .setSourceLanguage(sourceLanguage)
                            .setTargetLanguage(HI)
                            .build();
            //download language models if needed
            final FirebaseTranslator langTranslator =
                    FirebaseNaturalLanguage.getInstance().getTranslator(options);

            //FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                   // .requireWifi()
                   // .build();

            langTranslator.downloadModelIfNeeded()//conditions)
                    .addOnSuccessListener(
                            new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void v) {
                                    Log.d("translator", "downloaded lang  model");
                                    //after making sure language models are available
                                    //perform translation
                                    translateText(langTranslator);
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this,
                                            "Problem in translating the text entered",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
        }
        else if(changed && sourceLanguage==22)  // here 22 is code of hindi language
        {
            options =
                    new FirebaseTranslatorOptions.Builder()
                            .setSourceLanguage(sourceLanguage)
                            .setTargetLanguage(EN)
                            .build();
            //download language models if needed
            final FirebaseTranslator langTranslator =
                    FirebaseNaturalLanguage.getInstance().getTranslator(options);

            //FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                   // .requireWifi()
                   // .build();
            langTranslator.downloadModelIfNeeded()//conditions)
                    .addOnSuccessListener(
                            new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void v) {
                                    Log.d("translator", "downloaded lang  model");
                                    //after making sure language models are available
                                    //perform translation
                                    translateText(langTranslator);
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this,
                                            "Problem in translating the text entered",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
        }
        else
        {
            Toast.makeText(this, "Enter Valid Language", Toast.LENGTH_SHORT).show();
        }

    }

    public void translateTextToEnglish(String text) {
        //First identify the language of the entered text
        FirebaseLanguageIdentification languageIdentifier =
                FirebaseNaturalLanguage.getInstance().getLanguageIdentification();
        languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@Nullable String languageCode) {
                                if (languageCode != "und") {
                                    Log.d("translator", "lang "+languageCode);
                                    //download translator for the identified language
                                    // and translate the entered text into english
                                    downloadTranslatorAndTranslate(languageCode);

                                } else {
                                    Toast.makeText(MainActivity.this,
                                            "Could not identify language of the text entered",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this,
                                        "Problem in identifying language of the text entered",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
    }


    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        RECORD_REQUEST_CODE);

            }
        }
    }
}
