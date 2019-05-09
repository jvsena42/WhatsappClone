package com.app.whatsapp.whatsappclone.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.app.whatsapp.whatsappclone.R;
import com.app.whatsapp.whatsappclone.config.ConfiguracaoFirebase;
import com.app.whatsapp.whatsappclone.helper.Base64Custom;
import com.app.whatsapp.whatsappclone.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private EditText campoNome, campoEmail, campoSenha;
    private FirebaseAuth autentiacao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        campoNome = findViewById(R.id.editNome);
        campoEmail = findViewById(R.id.editEmail);
        campoSenha = findViewById(R.id.editSenha);
    }

    public void cadastrarUsuario(final Usuario usuario){
        autentiacao = ConfiguracaoFirebase.getFirebaseAuth();
        autentiacao.createUserWithEmailAndPassword(
            usuario.getEmail(),usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    finish();

                    try{
                        String indentificadorUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                        usuario.setId(indentificadorUsuario);
                        usuario.salvar();
                    }catch (Exception e){
                       e.printStackTrace();
                    }

                }else {
                    String excessao = "";
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthWeakPasswordException e){
                        excessao = "Digite uma senha mais forte!";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        excessao = "Digite um e-mail válido";
                    }catch (FirebaseAuthUserCollisionException e){
                        excessao = "Usuário já cadastrado";
                    }catch (Exception e){
                        excessao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastroActivity.this,excessao,Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void validarCadastroUsuario(View view){
        String textoNome = campoNome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        if (!textoNome.isEmpty() || !textoEmail.isEmpty() || !textoSenha.isEmpty()){
            Usuario usuario = new Usuario();
            usuario.setNome(textoNome);
            usuario.setEmail(textoEmail);
            usuario.setSenha(textoSenha);
            cadastrarUsuario(usuario);
        }else {
            Toast.makeText(CadastroActivity.this,"Preencha todos os campos!",Toast.LENGTH_LONG).show();
        }
    }
}
