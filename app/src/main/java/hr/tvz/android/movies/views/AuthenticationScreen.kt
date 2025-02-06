package hr.tvz.android.movies.views

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import hr.tvz.android.movies.R
import hr.tvz.android.movies.data.payload.LoginRequest
import hr.tvz.android.movies.data.payload.RegistrationRequest
import hr.tvz.android.movies.ui.theme.onPrimaryDark
import hr.tvz.android.movies.ui.theme.primaryContainerDarkMediumContrast
import hr.tvz.android.movies.ui.theme.primaryLightHighContrast
import hr.tvz.android.movies.view_model.AuthState
import hr.tvz.android.movies.view_model.AuthViewModel


@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val username by authViewModel.username.collectAsState()
    val password by authViewModel.password.collectAsState()
    val loginResponse by authViewModel.authState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Box(modifier = Modifier
        .fillMaxSize()
        .background(primaryLightHighContrast)) { }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .padding(28.dp)
                .alpha(0.7f)
                .clip(
                    CutCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    )
                )
                .background(onPrimaryDark)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                LoginHeader(headerString = "Movies authentication")
                Spacer(modifier = Modifier.height(20.dp))

                LoginFields(
                    username = username.text,
                    password = password.text,
                    onUsernameChange = { value -> authViewModel.setUsername(value) },
                    onPasswordChange = { value -> authViewModel.setPassword(value) },
                    onForgotPasswordClick = {},
                    isErrorUsername = username.errorMsg != null,
                    isErrorPassword = password.errorMsg != null,
                    errorLabelUsername = "Username cannot be empty",
                    errorLabelPassword = "Password cannot be empty"
                )

                LoginFooter(
                    onSignInClick = {
                        if(authViewModel.validateUsername() && authViewModel.validatePassword()) {
                            authViewModel.login(LoginRequest(username.text, password.text), context)
                        }
                    },
                    onSignUpClick = { navController.navigate(route = "movies/register") }
                )

                val googleSignInClient = remember {
                    GoogleSignIn.getClient(
                        context,
                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken("apps.googleusercontent.com")
                            .requestEmail()
                            .build()
                    )
                }
                googleSignInClient.revokeAccess()

                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    authViewModel.loginWithGoogle(task, context)
                }

                Button(
                    onClick = {
                        val signInIntent = googleSignInClient.signInIntent.apply {
                            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                            addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                        }
                        launcher.launch(signInIntent)
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Sign in with Google")
                }
            }
        }
    }
    when (loginResponse) {
        is AuthState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }

        is AuthState.LoggedIn -> {
            navController.navigate("movies/profile")
        }

        is AuthState.LoggedOut -> { }
        is AuthState.Error -> {
            val error = (loginResponse as AuthState.Error).errorResponse
            Toast.makeText(context,error,Toast.LENGTH_SHORT).show()
        }

        AuthState.RegistrationSuccess -> {}
    }
}

@Composable
fun RegistrationScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val email by authViewModel.email.collectAsState()
    val username by authViewModel.username.collectAsState()
    val password by authViewModel.password.collectAsState()
    val confirmPassword by authViewModel.confirmPassword.collectAsState()
    val registrationResponse by authViewModel.authState.collectAsState()
    //ToDO: val registrationResponse by viewModel.authState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Box(modifier = Modifier
        .fillMaxSize()
        .background(primaryLightHighContrast)) { }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .padding(28.dp)
                .alpha(0.7f)
                .clip(
                    CutCornerShape(
                        topStart = 10.dp,
                        topEnd = 10.dp,
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    )
                )
                .background(onPrimaryDark)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LoginHeader(headerString = "Movies authentication")
                Spacer(modifier = Modifier.height(20.dp))
                RegistrationFields(
                    email = email.text,
                    password = password.text,
                    confirmPassword = confirmPassword.text,
                    username = username.text,
                    onEmailChange = { value -> authViewModel.setEmail(value)},
                    onUsernameChange = { value -> authViewModel.setUsername(value)},
                    onPasswordChange = { value -> authViewModel.setPassword(value)},
                    onConfirmPasswordChange = { value -> authViewModel.setConfirmPassword(value)},
                    onForgotPasswordClick = {},
                    isErrorEmail = email.errorMsg != null,
                    isErrorUsername = username.errorMsg != null,
                    isErrorPassword = password.errorMsg != null,
                    errorLabelEmail = "Email cannot be empty",
                    errorLabelPassword = "Password cannot be empty",
                    errorLabelUsername = "Username cannot be empty"
                )
                RegistrationFooter(
                    onSignUpClick = {
                        if (authViewModel.validateEmail() && authViewModel.validatePassword()
                            && authViewModel.validateUsername()
                        ) {
                            authViewModel.signup(
                                RegistrationRequest(
                                    username.text,
                                    email.text,
                                    password.text,
                                ),
                                context
                            )
                        }
                    },
                    onSignInClick = { navController.navigate("movies/login") }
                )
                val googleSignInClient = remember {
                    GoogleSignIn.getClient(
                        context,
                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken("apps.googleusercontent.com")
                            .requestEmail()
                            .build()
                    )
                }
                googleSignInClient.revokeAccess()

                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    authViewModel.loginWithGoogle(task, context)
                }

                Button(
                    onClick = {
                        val signInIntent = googleSignInClient.signInIntent.apply {
                            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                            addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                        }
                        launcher.launch(signInIntent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        Color.White
                    ),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Sign in with Google")
                }
            }
        }
    }

    when(registrationResponse){
        is AuthState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }

        AuthState.RegistrationSuccess -> {
            navController.navigate("movies/login")
        }
        is AuthState.LoggedIn -> {
            navController.navigate("movies/profile")
            Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
        }

        is AuthState.LoggedOut -> { }
        is AuthState.Error -> {}

    }
}


@Composable
fun RegistrationFields(
    email: String,
    password: String,
    username: String,
    confirmPassword: String,
    onEmailChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onForgotPasswordClick: () -> Unit,
    isErrorEmail: Boolean,
    isErrorUsername: Boolean,
    isErrorPassword: Boolean,
    errorLabelEmail: String,
    errorLabelPassword: String,
    errorLabelUsername: String
) {
    Column {
        TextField(
            value = username,
            label = "Username",
            placeholder = "Enter your username",
            onValueChange = onUsernameChange,
            leadingIcon = {
                Icon(Icons.Default.AccountBox , contentDescription = "Email", tint = Color.White)
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            isError = isErrorUsername,
            errorLabel = errorLabelUsername
        )

        TextField(
            value = email,
            label = "Email",
            placeholder = "Enter your email address",
            onValueChange = onEmailChange,
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = "Email", tint = Color.White)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = isErrorEmail,
            errorLabel = errorLabelEmail
        )

        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            value = password,
            label = "Password",
            placeholder = "Enter your password",
            onValueChange = onPasswordChange,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Go
            ),
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Password", tint = Color.White)
            },
            isError = isErrorPassword,
            errorLabel = errorLabelPassword
        )

        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            value = confirmPassword,
            label = "Confirm Password",
            placeholder = "Confirm password",
            onValueChange = onConfirmPasswordChange,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Go
            ),
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Confirm Password", tint = Color.White)
            },
            isError = isErrorPassword,
            errorLabel = errorLabelPassword
        )

        TextButton(onClick = onForgotPasswordClick, modifier = Modifier.align(Alignment.End)) {
            Text(text = "Forgot Password?")
        }

    }
}
@Composable
fun RegistrationFooter(
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = onSignUpClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Sign Up")
        }
        TextButton(onClick = onSignInClick) {
            Text(
                text = "Already have an account? Sign in here.",
                color = primaryContainerDarkMediumContrast
            )
        }
    }
}

@Composable
fun LoginHeader(headerString: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = headerString,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = "Sign in to continue",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}
@Composable
fun LoginFields(
    password: String,
    username: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onForgotPasswordClick: () -> Unit,
    isErrorUsername: Boolean,
    isErrorPassword: Boolean,
    errorLabelPassword: String,
    errorLabelUsername: String
) {
    Column {
        TextField(
            value = username,
            label = "Username",
            placeholder = "Enter your username",
            onValueChange = onUsernameChange,
            leadingIcon = {
                Icon(Icons.Default.AccountBox , contentDescription = "Email", tint = Color.White)
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            isError = isErrorUsername,
            errorLabel = errorLabelUsername
        )

        Spacer(modifier = Modifier.height(10.dp))

        TextField(
            value = password,
            label = "Password",
            placeholder = "Enter your password",
            onValueChange = onPasswordChange,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Go
            ),
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Password", tint = Color.White)
            },
            isError = isErrorPassword,
            errorLabel = errorLabelPassword
        )

        TextButton(onClick = onForgotPasswordClick, modifier = Modifier.align(Alignment.End)) {
            Text(text = "Forgot Password?", color = primaryContainerDarkMediumContrast)
        }

    }
}
@Composable
fun LoginFooter(
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = onSignInClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Sign In")
        }
        TextButton(onClick = onSignUpClick) {
            Text(
                text = "Don't have an account? Sign up here.",
                color = primaryContainerDarkMediumContrast
            )
        }
    }
}

@Composable
fun TextField(
    value: String,
    label: String,
    placeholder: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    errorLabel : String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(text = label)
        },
        placeholder = {
            Text(text = placeholder)
        },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.White,
            focusedBorderColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedTextColor = Color.White,
        ),
    )

    if(isError && errorLabel.isNotEmpty()){
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = errorLabel, color = Color.Red, fontSize = 11.sp)
    }
}