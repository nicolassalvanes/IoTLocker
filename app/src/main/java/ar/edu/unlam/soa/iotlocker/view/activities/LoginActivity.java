package ar.edu.unlam.soa.iotlocker.view.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ar.edu.unlam.soa.iotlocker.R;
import ar.edu.unlam.soa.iotlocker.helper.HttpHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private Boolean processingFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void attemptLogin() {
        if (processingFlag) {
            return;
        }

        // Reset errors.
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String password = mPasswordView.getText().toString();

        // Check for a valid password, if the user entered one.
        int passwordLength = getResources().getInteger(R.integer.password_length);
        if (TextUtils.isEmpty(password)
                || password.length()!=passwordLength
                || !TextUtils.isDigitsOnly(password)) {

            mPasswordView.setError(getString(R.string.error_invalid_password));
            mPasswordView.requestFocus();

        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            processingFlag = true;
            showProgress(true);
            try {
                new HttpHelper(getApplicationContext()).doPost(
                        getString(R.string.service_url).concat(getString(R.string.admin_validate_path)),
                        password,
                        new HttpHelper.HttpHelperCallback() {
                            @Override
                            public void onDataAvailable(String data) {
                                processValidationResponse(data);
                            }
                        }
                );
            } catch (HttpHelper.HttpException e) {
                Log.e(this.getClass().getName(), "Exception during connection", e);
            }
        }

    }

    private void processValidationResponse(String data) {
        showProgress(false);

        if (data.contains("true")) {
            Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.correct_password),
                    Toast.LENGTH_SHORT
            ).show();

            startActivity( new Intent(LoginActivity.this, MainActivity.class) );
            finish();
        } else {
            processingFlag=false;
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            mPasswordView.requestFocus();
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}

