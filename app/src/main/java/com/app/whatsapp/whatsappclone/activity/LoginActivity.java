package com.app.whatsapp.whatsappclone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.app.whatsapp.whatsappclone.R;
import com.app.whatsapp.whatsappclone.config.ConfiguracaoFirebase;
import com.app.whatsapp.whatsappclone.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText campoEmail,campoSenha;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();

        campoEmail = findViewById(R.id.editLoginEmail);
        campoSenha = findViewById(R.id.editLoginSenha);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        if (usuarioAtual != null){
            abrirTelaPrincipal();
        }

    }

    public void logarUsuario(Usuario usuario){
        autenticacao.signInWithEmailAndPassword(usuario.getEmail(),usuario.getSenha()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                abrirTelaPrincipal();

                }else {
                    String excessao = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        excessao = "Usuário não cadastrado!";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excessao = "E-mail e senha não correspondem!";
                    }catch (Exception e){
                        excessao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this,excessao,Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void validarAutenticacaoUsuario(View view){

        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        if (!textoEmail.isEmpty() || !textoSenha.isEmpty()){
            Usuario usuario = new Usuario();
            usuario.setEmail(textoEmail);
            usuario.setSenha(textoSenha);
            logarUsuario(usuario);
        }else {
            Toast.makeText(LoginActivity.this,"Preencha todos os campos!",Toast.LENGTH_LONG).show();
        }
    }

    public void abrirTelaCadastro(View view){
        Intent intent = new Intent(LoginActivity.this,CadastroActivity.class);
        startActivity(intent);
    }

    public void abrirTelaPrincipal(){
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
    }
}
