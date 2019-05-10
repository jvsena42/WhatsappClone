package com.app.whatsapp.whatsappclone.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.app.whatsapp.whatsappclone.R;
import com.app.whatsapp.whatsappclone.config.ConfiguracaoFirebase;
import com.app.whatsapp.whatsappclone.helper.Permissao;
import com.app.whatsapp.whatsappclone.helper.UsuarioFirebase;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfiguracoesActivity extends AppCompatActivity {

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private ImageButton imageButtonCamera, imageButtonGaleria;
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;
    private CircleImageView circleImageViewPerfil;
    private EditText editPerfilNome;
    private StorageReference storageReference;
    private String identificadorUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        //Configurações iniciais
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();

        //Validar permissoes
        Permissao.validarPermissoes(permissoesNecessarias,this,1);

        imageButtonCamera = findViewById(R.id.imageButtonCamera);
        imageButtonGaleria = findViewById(R.id.imageButtongaleria);
        circleImageViewPerfil = findViewById(R.id.circleImageViewFotoPerfil);
        editPerfilNome = findViewById(R.id.editPerfilNome);

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Configuracoes");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Recuperar dados do usuario
        FirebaseUser usuario = UsuarioFirebase.getUsuarioAtual();
        Uri url = usuario.getPhotoUrl();

        if (url !=null){
            Glide.with(ConfiguracoesActivity.this)
                    .load(url)
                    .into(circleImageViewPerfil);
        }else{
            circleImageViewPerfil.setImageResource(R.drawable.padrao);
        }

        editPerfilNome.setText(usuario.getDisplayName());


        imageButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent,SELECAO_CAMERA);
                }

            }
        });

        imageButtonGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                if (intent.resolveActivity( getPackageManager()) != null){
                    startActivityForResult(intent,SELECAO_GALERIA);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            Bitmap imagem = null;

            try{
               switch (requestCode){
                   case SELECAO_CAMERA:
                       imagem = (Bitmap) data.getExtras().get("data");
                       break;
                   case SELECAO_GALERIA:
                       Uri localImagemSelecionada = data.getData();
                       imagem = MediaStore.Images.Media.getBitmap(getContentResolver(),localImagemSelecionada);
                       break;
               }

               if (imagem != null){
                    circleImageViewPerfil.setImageBitmap(imagem);

                   //Recuperar dados da imagem para o firebase
                   ByteArrayOutputStream baos = new ByteArrayOutputStream();
                   imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos );
                   byte[] dadosImagem = baos.toByteArray();

                   //Salvar imagem no firebase
                   StorageReference imagemRef = storageReference
                           .child("imagens")
                           .child("perfil")
                           .child(identificadorUsuario + ".jpeg");

                   UploadTask uploadTask = imagemRef.putBytes( dadosImagem );
                   uploadTask.addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           Toast.makeText(ConfiguracoesActivity.this,
                                   "Erro ao fazer upload da imagem",
                                   Toast.LENGTH_SHORT).show();
                       }
                   }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                       @Override
                       public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                           Toast.makeText(ConfiguracoesActivity.this,
                                   "Sucesso ao fazer upload da imagem",
                                   Toast.LENGTH_SHORT).show();

                           taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener
                                   (new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri url) {
                                            atualizaFotoUsuario(url);
                                        }
                                    }
                                   );
                       }
                   });
               }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public void atualizaFotoUsuario(Uri url){
        //Atualizar url da foto no firebaseUser
        UsuarioFirebase.atualizarFotoUsuario(url);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int permissaoResultado : grantResults){
            if (permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }

    }

    private void alertaValidacaoPermissao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissoes Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
