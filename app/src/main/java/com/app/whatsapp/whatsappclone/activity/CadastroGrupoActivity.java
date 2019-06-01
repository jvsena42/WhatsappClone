package com.app.whatsapp.whatsappclone.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.app.whatsapp.whatsappclone.R;
import com.app.whatsapp.whatsappclone.adapter.GrupoSelecionadoAdapter;
import com.app.whatsapp.whatsappclone.config.ConfiguracaoFirebase;
import com.app.whatsapp.whatsappclone.model.Grupo;
import com.app.whatsapp.whatsappclone.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CadastroGrupoActivity extends AppCompatActivity {

    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private TextView textoTotalParticipantes;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private RecyclerView recyclerMembrosSelecionados;
    private CircleImageView imageGrupo;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageReference;
    private Grupo grupo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_grupo);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Novo Grupo");
        toolbar.setSubtitle("Defina o nome");
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fabAvancarCadastro);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configuracoes iniciais
        textoTotalParticipantes = findViewById(R.id.textTotalParticipantes);
        recyclerMembrosSelecionados = findViewById(R.id.recyclerMembrosGrupo);
        imageGrupo = findViewById(R.id.imageGrupo);
        grupo = new Grupo();

        storageReference = ConfiguracaoFirebase.getFirebaseStorage();

        //Evento de clique imagem do grupo
        imageGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                if (intent.resolveActivity( getPackageManager()) != null){
                    startActivityForResult(intent,SELECAO_GALERIA);
                }
            }
        });


        //Configurar recyclerView
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMembrosSelecionados,getApplicationContext());

        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );

        recyclerMembrosSelecionados.setLayoutManager(layoutManagerHorizontal);
        recyclerMembrosSelecionados.setHasFixedSize(true);
        recyclerMembrosSelecionados.setAdapter(grupoSelecionadoAdapter);



        //Recuperar lista de membros passada
        if (getIntent().getExtras() != null){
            List<Usuario> membros = (List<Usuario>) getIntent().getExtras().getSerializable("membros");
            listaMembrosSelecionados.addAll(membros);

            textoTotalParticipantes.setText("Participantes:" + listaMembrosSelecionados.size());

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            Bitmap imagem = null;

            try {

                Uri localImagemSelecionada = data.getData();
                imagem = MediaStore.Images.Media.getBitmap(getContentResolver(),localImagemSelecionada);

                if (imagem != null){
                    imageGrupo.setImageBitmap(imagem);

                    //Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos );
                    byte[] dadosImagem = baos.toByteArray();

                    //Salvar imagem no firebase
                    StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("grupos")
                            .child(grupo.getId()+ ".jpeg");

                    //códigos atualizados para o UploadTask das novas bibliotecas
                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CadastroGrupoActivity.this,
                                    "Erro ao fazer upload!", Toast.LENGTH_SHORT).show();
                        }

                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(CadastroGrupoActivity.this,
                                    "Sucesso ao fazer upload!", Toast.LENGTH_SHORT).show();

                            taskSnapshot.getStorage().getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {

                                            //converter para string diretamente
                                            grupo.setFoto(uri.toString());
                                        }
                                    });
                        }
                    });

                }
            }catch (Exception e){
            e.printStackTrace();
        }

    }

    }
}
