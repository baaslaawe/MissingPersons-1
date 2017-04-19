package ke.co.debechlabs.missingpersons;

import android.content.Intent;
import android.lib.recaptcha.ReCaptcha;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import ke.co.debechlabs.missingpersons.Config.Config;

import static com.basgeekball.awesomevalidation.ValidationStyle.BASIC;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener, ReCaptcha.OnShowChallengeListener, ReCaptcha.OnVerifyAnswerListener{
    private static final String TAG = "SignUpActivity";
    private ReCaptcha   reCaptcha;
    private ProgressBar progressBar, completeProgressBar;
    private EditText etxAnswer, etxName, etxEmail, etxPhone, etxPassword;
    private Button btnVerify, btnReload, btnComplete;
    private LinearLayout recaptachaLayout;
    private AwesomeValidation mAwesomeValidation;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("User Registration");

        auth = FirebaseAuth.getInstance();

        mAwesomeValidation = new AwesomeValidation(BASIC);

        mAwesomeValidation.addValidation(this, R.id.input_name, "[a-zA-Z\\s]+", R.string.err_name);
        mAwesomeValidation.addValidation(this, R.id.input_email, android.util.Patterns.EMAIL_ADDRESS, R.string.err_email);
        mAwesomeValidation.addValidation(this, R.id.input_phonenumber, RegexTemplate.TELEPHONE, R.string.err_phone);

//        String regexPassword = "(?=.*[a-z])(?=.*[A-Z])(?=.*[\\d])(?=.*[~`!@#\\$%\\^&\\*\\(\\)\\-_\\+=\\{\\}\\[\\]\\|\\;:\"<>,./\\?]).{8,}";
//        mAwesomeValidation.addValidation(this, R.id.input_password, "/^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]*$/", R.string.empty_password);
        mAwesomeValidation.addValidation(this, R.id.input_confirm_password, R.id.input_password, R.string.err_password_confirmation);

        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        this.etxEmail = (EditText) findViewById(R.id.input_email);
        this.etxName = (EditText) findViewById(R.id.input_name);
        this.etxPassword = (EditText) findViewById(R.id.input_password);
        this.etxPhone = (EditText) findViewById(R.id.input_phonenumber);

        this.reCaptcha = (ReCaptcha)findViewById(R.id.recaptchaCode);
        this.progressBar = (ProgressBar)findViewById(R.id.progress);
        this.completeProgressBar = (ProgressBar) findViewById(R.id.complete_progressBar);
        this.etxAnswer = (EditText)findViewById(R.id.input_recaptcha_response);
        this.btnVerify = (Button) findViewById(R.id.verify);
        this.btnReload = (Button) findViewById(R.id.reload);
        this.recaptachaLayout = (LinearLayout) findViewById(R.id.recaptchaLayout);
        this.btnComplete = (Button) findViewById(R.id.completeRegistration);

        this.btnComplete.setVisibility(View.GONE);
        this.showChallenge();

        btnVerify.setOnClickListener(this);
        btnReload.setOnClickListener(this);
        btnComplete.setOnClickListener(this);
    }


    private void showChallenge() {
        this.progressBar.setVisibility(View.VISIBLE);
        this.reCaptcha.setVisibility(View.GONE);
        this.reCaptcha.setLanguageCode("en");
        this.reCaptcha.showChallengeAsync(Config.recaptcha_public_key, this);
    }

    @Override
    public void onChallengeShown(boolean shown) {
        this.progressBar.setVisibility(View.GONE);

        if (shown) {
            // If a CAPTCHA is shown successfully, displays it for the user to enter the words
            this.reCaptcha.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, R.string.show_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAnswerVerified(boolean success) {
        if (success) {
            Toast.makeText(this, R.string.verification_success, Toast.LENGTH_SHORT).show();
            this.recaptachaLayout.setVisibility(View.GONE);
            this.btnComplete.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, R.string.verification_failed, Toast.LENGTH_SHORT).show();
            this.progressBar.setVisibility(View.GONE);
        }
    }

    private void verifyAnswer() {
        if (TextUtils.isEmpty(this.etxAnswer.getText())) {
            Toast.makeText(this, R.string.instruction, Toast.LENGTH_SHORT).show();
        } else {
            // Displays a progress bar while submitting the answer for verification
            this.progressBar.setVisibility(View.VISIBLE);
            this.reCaptcha.verifyAnswerAsync(Config.recaptcha_private_key, this.etxAnswer.getText().toString(), this);
        }
    }

    private void firebaseSignUp(){
        if(mAwesomeValidation.validate()){
            this.completeProgressBar.setVisibility(View.VISIBLE);

            String email = etxEmail.getText().toString();
            String phone = etxPhone.getText().toString();
            final String name = etxName.getText().toString();
            String password = etxPassword.getText().toString();

//            System.out.println(email + " " + password);
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Toast.makeText(SignUpActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                            completeProgressBar.setVisibility(View.GONE);
                            if (!task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, "Authentication failed." + task.getException(),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SignUpActivity.this, "Successfully signed up", Toast.LENGTH_SHORT).show();
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                                user.updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "User profile updated.");
                                        }
                                    }
                                });
                                startActivity(new Intent(SignUpActivity.this, DashboardActivity.class));
                                finish();
                            }
                        }
                    });
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.verify:
                this.verifyAnswer();
                break;

            case R.id.reload:
                this.showChallenge();
                break;
            case R.id.completeRegistration:
                this.firebaseSignUp();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.completeProgressBar.setVisibility(View.GONE);
    }
}
